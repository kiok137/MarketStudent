package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单业务层
 * 订单状态流转：0-待审核 -> 1-已审核 -> 2-已收货；任意未收货状态可取消为 3-已取消
 */
@Service
public class PurchaseOrderService {
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;

    @Resource
    private PurchaseOrderDetailMapper purchaseOrderDetailMapper;

    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private StockInMapper stockInMapper;

    public Integer count() {
        return Math.toIntExact(purchaseOrderMapper.selectCount(null));
    }

    /**
     * 某一天的采购金额合计（仪表盘趋势图）
     */
    public BigDecimal getAmountByDate(LocalDate date) {
        if (date == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .ge(PurchaseOrder::getCreatedAt, start)
                        .lt(PurchaseOrder::getCreatedAt, end)
                        .ne(PurchaseOrder::getStatus, 3)
        );
        return orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Page<PurchaseOrder> getOrdersByPage(String orderNo, Integer supplierId, Integer status,
                                               Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(PurchaseOrder::getOrderNo, orderNo.trim());
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getOrderId);

        Page<PurchaseOrder> page = purchaseOrderMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillOrderNames(page.getRecords());
        return page;
    }

    public PurchaseOrder getOrderById(Integer orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("采购订单不存在");
        }
        List<PurchaseOrderDetail> details = purchaseOrderDetailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().eq(PurchaseOrderDetail::getOrderId, orderId)
        );
        fillDetailNames(details);
        order.setDetailList(details);

        Supplier supplier = supplierMapper.selectById(order.getSupplierId());
        if (supplier != null) {
            order.setSupplierName(supplier.getSupplierName());
        }
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                order.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        }
        return order;
    }

    @Transactional
    public void createOrder(PurchaseOrder order) {
        if (order.getSupplierId() == null || supplierMapper.selectById(order.getSupplierId()) == null) {
            throw new ServiceException("供应商不存在");
        }
        List<PurchaseOrderDetail> details = order.getDetailList();
        if (details == null || details.isEmpty()) {
            throw new ServiceException("请至少添加一条采购明细");
        }
        for (PurchaseOrderDetail detail : details) {
            if (detail.getProductId() == null || productMapper.selectById(detail.getProductId()) == null) {
                throw new ServiceException("采购明细中存在无效的商品");
            }
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new ServiceException("采购数量必须大于0");
            }
            if (detail.getPrice() == null || detail.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("采购单价必须大于0");
            }
            detail.setAmount(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }

        BigDecimal total = details.stream()
                .map(PurchaseOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        order.setOrderId(null);
        order.setTotalAmount(total);
        if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            order.setOrderNo("CG" + now.format(NO_FORMATTER));
        }
        if (isOrderNoExists(order.getOrderNo(), null)) {
            throw new ServiceException("订单编号已存在");
        }
        if (order.getUserId() == null) {
            User current = JwtTokenUtils.getCurrentUser();
            if (current != null) {
                order.setUserId(current.getUserId());
            }
        }
        order.setStatus(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        if (purchaseOrderMapper.insert(order) <= 0) {
            throw new ServiceException("采购订单创建失败");
        }

        for (PurchaseOrderDetail detail : details) {
            detail.setDetailId(null);
            detail.setOrderId(order.getOrderId());
            detail.setReceivedQuantity(0);
            detail.setCreatedAt(now);
            detail.setUpdatedAt(now);
            if (purchaseOrderDetailMapper.insert(detail) <= 0) {
                throw new ServiceException("采购明细保存失败");
            }
        }
    }

    @Transactional
    public void approveOrder(Integer orderId) {
        PurchaseOrder order = requireOrder(orderId);
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new ServiceException("只有待审核的订单可以审核");
        }
        PurchaseOrder update = new PurchaseOrder();
        update.setOrderId(orderId);
        update.setStatus(1);
        update.setApprovedAt(LocalDateTime.now());
        update.setUpdatedAt(LocalDateTime.now());
        if (purchaseOrderMapper.updateById(update) <= 0) {
            throw new ServiceException("订单审核失败");
        }
    }

    /**
     * 确认收货：状态置为已收货，同时把明细数量写入库存并生成入库记录
     */
    @Transactional
    public void receiveOrder(Integer orderId) {
        PurchaseOrder order = requireOrder(orderId);
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new ServiceException("只有已审核的订单可以收货");
        }
        List<PurchaseOrderDetail> details = purchaseOrderDetailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().eq(PurchaseOrderDetail::getOrderId, orderId)
        );
        if (details.isEmpty()) {
            throw new ServiceException("该订单没有采购明细，无法收货");
        }

        LocalDateTime now = LocalDateTime.now();
        User current = JwtTokenUtils.getCurrentUser();

        for (PurchaseOrderDetail detail : details) {
            Inventory inventory = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, detail.getProductId())
            );
            if (inventory == null) {
                inventory = new Inventory();
                inventory.setProductId(detail.getProductId());
                inventory.setQuantity(detail.getQuantity());
                inventory.setMinQuantity(10);
                inventory.setMaxQuantity(1000);
                inventory.setUpdatedAt(now);
                inventoryMapper.insert(inventory);
            } else {
                Inventory update = new Inventory();
                update.setInventoryId(inventory.getInventoryId());
                update.setQuantity(inventory.getQuantity() + detail.getQuantity());
                update.setUpdatedAt(now);
                inventoryMapper.updateById(update);
            }

            PurchaseOrderDetail detailUpdate = new PurchaseOrderDetail();
            detailUpdate.setDetailId(detail.getDetailId());
            detailUpdate.setReceivedQuantity(detail.getQuantity());
            detailUpdate.setUpdatedAt(now);
            purchaseOrderDetailMapper.updateById(detailUpdate);

            StockIn stockIn = new StockIn();
            stockIn.setStockInNo("RK" + now.format(NO_FORMATTER) + detail.getDetailId());
            stockIn.setOrderId(orderId);
            stockIn.setProductId(detail.getProductId());
            stockIn.setQuantity(detail.getQuantity());
            stockIn.setBatchNo("B" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            stockIn.setUserId(current != null ? current.getUserId() : order.getUserId());
            stockIn.setRemark("采购订单 " + order.getOrderNo() + " 自动入库");
            stockIn.setCreatedAt(now);
            stockIn.setUpdatedAt(now);
            stockInMapper.insert(stockIn);
        }

        PurchaseOrder update = new PurchaseOrder();
        update.setOrderId(orderId);
        update.setStatus(2);
        update.setReceivedAt(now);
        update.setUpdatedAt(now);
        if (purchaseOrderMapper.updateById(update) <= 0) {
            throw new ServiceException("订单收货失败");
        }
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        PurchaseOrder order = requireOrder(orderId);
        if (order.getStatus() != null && order.getStatus() == 2) {
            throw new ServiceException("已收货的订单不能取消");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            throw new ServiceException("订单已经是取消状态");
        }
        PurchaseOrder update = new PurchaseOrder();
        update.setOrderId(orderId);
        update.setStatus(3);
        update.setUpdatedAt(LocalDateTime.now());
        if (purchaseOrderMapper.updateById(update) <= 0) {
            throw new ServiceException("订单取消失败");
        }
    }

    @Transactional
    public void deleteOrder(Integer orderId) {
        PurchaseOrder order = requireOrder(orderId);
        if (order.getStatus() != null && order.getStatus() == 2) {
            throw new ServiceException("已收货的订单不能删除");
        }
        purchaseOrderDetailMapper.delete(
                new LambdaQueryWrapper<PurchaseOrderDetail>().eq(PurchaseOrderDetail::getOrderId, orderId)
        );
        if (purchaseOrderMapper.deleteById(orderId) <= 0) {
            throw new ServiceException("订单删除失败");
        }
    }

    private PurchaseOrder requireOrder(Integer orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("采购订单不存在");
        }
        return order;
    }

    private boolean isOrderNoExists(String orderNo, Integer excludeId) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getOrderNo, orderNo);
        if (excludeId != null) {
            wrapper.ne(PurchaseOrder::getOrderId, excludeId);
        }
        Long count = purchaseOrderMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private void fillOrderNames(List<PurchaseOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Map<Integer, String> supplierNames = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(Supplier::getSupplierId, Supplier::getSupplierName, (a, b) -> a));
        Map<Integer, String> userNames = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), (a, b) -> a));
        orders.forEach(order -> {
            order.setSupplierName(supplierNames.get(order.getSupplierId()));
            order.setUsername(userNames.get(order.getUserId()));
        });
    }

    private void fillDetailNames(List<PurchaseOrderDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        List<Integer> productIds = details.stream()
                .map(PurchaseOrderDetail::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return;
        }
        Map<Integer, Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds)
        ).stream().collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        for (PurchaseOrderDetail detail : details) {
            Product product = products.get(detail.getProductId());
            if (product != null) {
                detail.setProductName(product.getProductName());
                detail.setProductUnit(product.getUnit());
            }
        }
    }

    /**
     * 采购明细的批量查询（供其它模块复用）
     */
    public List<PurchaseOrderDetail> getDetailsByOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return purchaseOrderDetailMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderDetail>().in(PurchaseOrderDetail::getOrderId, orderIds)
        );
    }
}

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 库存业务层：库存查询、库存预警、入库/出库单据与库存数量联动
 */
@Service
public class InventoryService {
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private StockInMapper stockInMapper;

    @Resource
    private StockOutMapper stockOutMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;

    /**
     * 库存预警数量：库存低于或等于最低库存量的商品数
     */
    public Integer countLowInventory() {
        List<Inventory> inventories = inventoryMapper.selectList(null);
        return (int) inventories.stream()
                .filter(i -> i.getQuantity() != null && i.getMinQuantity() != null
                        && i.getQuantity() <= i.getMinQuantity())
                .count();
    }

    /**
     * 某个分类下的库存总量（仪表盘对比图）
     */
    public Integer getStockByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return 0;
        }
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>().eq(Category::getCategoryName, categoryName.trim())
        );
        if (category == null) {
            return 0;
        }
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, category.getCategoryId())
        );
        if (products.isEmpty()) {
            return 0;
        }
        List<Integer> productIds = products.stream().map(Product::getProductId).collect(Collectors.toList());
        List<Inventory> inventories = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>().in(Inventory::getProductId, productIds)
        );
        return inventories.stream()
                .map(Inventory::getQuantity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public Page<Inventory> getInventoryByPage(String productName, Boolean lowStock,
                                              Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (productName != null && !productName.trim().isEmpty()) {
            List<Integer> productIds = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().like(Product::getProductName, productName.trim())
            ).stream().map(Product::getProductId).collect(Collectors.toList());
            if (productIds.isEmpty()) {
                // 没有匹配商品时返回空页
                Page<Inventory> empty = new Page<>(currentPage == null ? 1 : currentPage,
                        pageSize == null ? 10 : pageSize);
                empty.setRecords(List.of());
                empty.setTotal(0);
                return empty;
            }
            wrapper.in(Inventory::getProductId, productIds);
        }
        if (Boolean.TRUE.equals(lowStock)) {
            wrapper.apply("quantity <= min_quantity");
        }
        wrapper.orderByAsc(Inventory::getInventoryId);

        Page<Inventory> page = inventoryMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillInventoryProductInfo(page.getRecords());
        return page;
    }

    public List<Inventory> getAllInventory() {
        List<Inventory> list = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>().orderByAsc(Inventory::getInventoryId)
        );
        fillInventoryProductInfo(list);
        return list;
    }

    @Transactional
    public void updateThreshold(Integer inventoryId, Integer minQuantity, Integer maxQuantity) {
        Inventory existing = inventoryMapper.selectById(inventoryId);
        if (existing == null) {
            throw new ServiceException("库存记录不存在");
        }
        if (minQuantity != null && minQuantity < 0) {
            throw new ServiceException("最低库存量不能小于0");
        }
        if (maxQuantity != null && maxQuantity < 0) {
            throw new ServiceException("最高库存量不能小于0");
        }
        int min = minQuantity != null ? minQuantity : existing.getMinQuantity();
        int max = maxQuantity != null ? maxQuantity : existing.getMaxQuantity();
        if (max < min) {
            throw new ServiceException("最高库存量不能小于最低库存量");
        }

        Inventory update = new Inventory();
        update.setInventoryId(inventoryId);
        update.setMinQuantity(min);
        update.setMaxQuantity(max);
        update.setUpdatedAt(LocalDateTime.now());
        if (inventoryMapper.updateById(update) <= 0) {
            throw new ServiceException("库存阈值更新失败");
        }
    }

    // ==================== 入库管理 ====================

    public Page<StockIn> getStockInByPage(String stockInNo, Integer productId,
                                          Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        if (stockInNo != null && !stockInNo.trim().isEmpty()) {
            wrapper.like(StockIn::getStockInNo, stockInNo.trim());
        }
        if (productId != null) {
            wrapper.eq(StockIn::getProductId, productId);
        }
        wrapper.orderByDesc(StockIn::getStockInId);

        Page<StockIn> page = stockInMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillStockInNames(page.getRecords());
        return page;
    }

    @Transactional
    public void createStockIn(StockIn stockIn) {
        if (stockIn.getProductId() == null || productMapper.selectById(stockIn.getProductId()) == null) {
            throw new ServiceException("商品不存在");
        }
        if (stockIn.getQuantity() == null || stockIn.getQuantity() <= 0) {
            throw new ServiceException("入库数量必须大于0");
        }
        if (stockIn.getOrderId() != null && purchaseOrderMapper.selectById(stockIn.getOrderId()) == null) {
            throw new ServiceException("关联的采购订单不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (stockIn.getStockInNo() == null || stockIn.getStockInNo().trim().isEmpty()) {
            stockIn.setStockInNo("RK" + now.format(NO_FORMATTER));
        }
        if (stockIn.getUserId() == null) {
            User current = JwtTokenUtils.getCurrentUser();
            if (current != null) {
                stockIn.setUserId(current.getUserId());
            }
        }
        if (stockIn.getUserId() == null) {
            throw new ServiceException("请先登录后再进行入库操作");
        }
        stockIn.setStockInId(null);
        stockIn.setCreatedAt(now);
        stockIn.setUpdatedAt(now);
        if (stockInMapper.insert(stockIn) <= 0) {
            throw new ServiceException("入库单保存失败");
        }

        changeInventoryQuantity(stockIn.getProductId(), stockIn.getQuantity(), now);
    }

    // ==================== 出库管理 ====================

    public Page<StockOut> getStockOutByPage(String stockOutNo, Integer productId,
                                            Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        if (stockOutNo != null && !stockOutNo.trim().isEmpty()) {
            wrapper.like(StockOut::getStockOutNo, stockOutNo.trim());
        }
        if (productId != null) {
            wrapper.eq(StockOut::getProductId, productId);
        }
        wrapper.orderByDesc(StockOut::getStockOutId);

        Page<StockOut> page = stockOutMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillStockOutNames(page.getRecords());
        return page;
    }

    @Transactional
    public void createStockOut(StockOut stockOut) {
        if (stockOut.getProductId() == null || productMapper.selectById(stockOut.getProductId()) == null) {
            throw new ServiceException("商品不存在");
        }
        if (stockOut.getQuantity() == null || stockOut.getQuantity() <= 0) {
            throw new ServiceException("出库数量必须大于0");
        }
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, stockOut.getProductId())
        );
        if (inventory == null || inventory.getQuantity() < stockOut.getQuantity()) {
            int current = inventory == null ? 0 : inventory.getQuantity();
            throw new ServiceException("库存不足，当前库存为 " + current);
        }

        LocalDateTime now = LocalDateTime.now();
        if (stockOut.getStockOutNo() == null || stockOut.getStockOutNo().trim().isEmpty()) {
            stockOut.setStockOutNo("CK" + now.format(NO_FORMATTER));
        }
        if (stockOut.getUserId() == null) {
            User current = JwtTokenUtils.getCurrentUser();
            if (current != null) {
                stockOut.setUserId(current.getUserId());
            }
        }
        if (stockOut.getUserId() == null) {
            throw new ServiceException("请先登录后再进行出库操作");
        }
        stockOut.setStockOutId(null);
        stockOut.setCreatedAt(now);
        stockOut.setUpdatedAt(now);
        if (stockOutMapper.insert(stockOut) <= 0) {
            throw new ServiceException("出库单保存失败");
        }

        changeInventoryQuantity(stockOut.getProductId(), -stockOut.getQuantity(), now);
    }

    // ==================== 内部方法 ====================

    /**
     * 调整库存数量，delta 为正表示入库，为负表示出库
     */
    private void changeInventoryQuantity(Integer productId, int delta, LocalDateTime now) {
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, productId)
        );
        if (inventory == null) {
            Inventory created = new Inventory();
            created.setProductId(productId);
            created.setQuantity(Math.max(delta, 0));
            created.setMinQuantity(10);
            created.setMaxQuantity(1000);
            created.setUpdatedAt(now);
            inventoryMapper.insert(created);
            return;
        }
        int newQuantity = inventory.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new ServiceException("库存不足，当前库存为 " + inventory.getQuantity());
        }
        Inventory update = new Inventory();
        update.setInventoryId(inventory.getInventoryId());
        update.setQuantity(newQuantity);
        update.setUpdatedAt(now);
        if (inventoryMapper.updateById(update) <= 0) {
            throw new ServiceException("库存更新失败");
        }
    }

    private void fillInventoryProductInfo(List<Inventory> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Integer> productIds = list.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return;
        }
        Map<Integer, Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds)
        ).stream().collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        list.forEach(inventory -> {
            Product product = products.get(inventory.getProductId());
            if (product != null) {
                inventory.setProductName(product.getProductName());
                inventory.setBarcode(product.getBarcode());
                inventory.setUnit(product.getUnit());
            }
        });
    }

    private void fillStockInNames(List<StockIn> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Integer, String> productNames = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getProductName, (a, b) -> a));
        Map<Integer, String> userNames = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), (a, b) -> a));
        Map<Integer, String> orderNos = purchaseOrderMapper.selectList(null).stream()
                .collect(Collectors.toMap(PurchaseOrder::getOrderId, PurchaseOrder::getOrderNo, (a, b) -> a));

        list.forEach(item -> {
            item.setProductName(productNames.get(item.getProductId()));
            item.setUsername(userNames.get(item.getUserId()));
            item.setOrderNo(orderNos.get(item.getOrderId()));
        });
    }

    private void fillStockOutNames(List<StockOut> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Integer, String> productNames = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getProductName, (a, b) -> a));
        Map<Integer, String> userNames = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), (a, b) -> a));

        list.forEach(item -> {
            item.setProductName(productNames.get(item.getProductId()));
            item.setUsername(userNames.get(item.getUserId()));
        });
    }
}

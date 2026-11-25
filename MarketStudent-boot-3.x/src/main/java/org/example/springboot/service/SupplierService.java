package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.PurchaseOrder;
import org.example.springboot.entity.Supplier;
import org.example.springboot.entity.SupplierEvaluation;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PurchaseOrderMapper;
import org.example.springboot.mapper.SupplierEvaluationMapper;
import org.example.springboot.mapper.SupplierMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 供应商业务层
 */
@Service
public class SupplierService {
    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private SupplierEvaluationMapper supplierEvaluationMapper;

    @Resource
    private PurchaseOrderMapper purchaseOrderMapper;

    public Integer count() {
        return Math.toIntExact(supplierMapper.selectCount(null));
    }

    public Supplier getSupplierById(Integer supplierId) {
        Supplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        return supplier;
    }

    /**
     * 全部启用的供应商（下拉框用）
     */
    public List<Supplier> getAllSuppliers() {
        return supplierMapper.selectList(
                new LambdaQueryWrapper<Supplier>()
                        .eq(Supplier::getStatus, 1)
                        .orderByAsc(Supplier::getSupplierId)
        );
    }

    public Page<Supplier> getSuppliersByPage(String supplierName, Integer status,
                                             Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (supplierName != null && !supplierName.trim().isEmpty()) {
            wrapper.like(Supplier::getSupplierName, supplierName.trim());
        }
        if (status != null) {
            wrapper.eq(Supplier::getStatus, status);
        }
        wrapper.orderByAsc(Supplier::getSupplierId);

        Page<Supplier> page = supplierMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);

        List<Supplier> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Integer> ids = records.stream().map(Supplier::getSupplierId).toList();
            List<SupplierEvaluation> evaluations = supplierEvaluationMapper.selectList(
                    new LambdaQueryWrapper<SupplierEvaluation>().in(SupplierEvaluation::getSupplierId, ids)
            );
            Map<Integer, List<SupplierEvaluation>> grouped = evaluations.stream()
                    .collect(Collectors.groupingBy(SupplierEvaluation::getSupplierId));
            records.forEach(supplier -> {
                List<SupplierEvaluation> list = grouped.get(supplier.getSupplierId());
                supplier.setEvaluations(list);
                if (list != null && !list.isEmpty()) {
                    double avg = list.stream()
                            .filter(e -> e.getTotalScore() != null)
                            .mapToInt(SupplierEvaluation::getTotalScore)
                            .average().orElse(0);
                    supplier.setAverageScore(Math.round(avg * 10) / 10.0);
                } else {
                    supplier.setAverageScore(0.0);
                }
            });
        }
        return page;
    }

    @Transactional
    public void createSupplier(Supplier supplier) {
        if (isNameExists(supplier.getSupplierName(), null)) {
            throw new ServiceException("供应商名称已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        supplier.setSupplierId(null);
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);
        if (supplierMapper.insert(supplier) <= 0) {
            throw new ServiceException("供应商创建失败");
        }
    }

    @Transactional
    public void updateSupplier(Supplier supplier) {
        if (supplier.getSupplierId() == null) {
            throw new ServiceException("供应商ID不能为空");
        }
        Supplier existing = supplierMapper.selectById(supplier.getSupplierId());
        if (existing == null) {
            throw new ServiceException("供应商不存在");
        }
        if (isNameExists(supplier.getSupplierName(), supplier.getSupplierId())) {
            throw new ServiceException("供应商名称已存在");
        }
        supplier.setCreatedAt(existing.getCreatedAt());
        supplier.setUpdatedAt(LocalDateTime.now());
        if (supplierMapper.updateById(supplier) <= 0) {
            throw new ServiceException("供应商更新失败");
        }
    }

    @Transactional
    public void updateStatus(Integer supplierId, Integer status) {
        Supplier existing = supplierMapper.selectById(supplierId);
        if (existing == null) {
            throw new ServiceException("供应商不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new ServiceException("状态值无效，只能是0(禁用)或1(启用)");
        }
        Supplier update = new Supplier();
        update.setSupplierId(supplierId);
        update.setStatus(status);
        update.setUpdatedAt(LocalDateTime.now());
        if (supplierMapper.updateById(update) <= 0) {
            throw new ServiceException("供应商状态更新失败");
        }
    }

    @Transactional
    public void deleteSupplier(Integer supplierId) {
        Supplier existing = supplierMapper.selectById(supplierId);
        if (existing == null) {
            throw new ServiceException("供应商不存在");
        }
        Long orderCount = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getSupplierId, supplierId)
        );
        if (orderCount != null && orderCount > 0) {
            throw new ServiceException("该供应商存在 " + orderCount + " 条采购订单，无法删除");
        }
        if (supplierMapper.deleteById(supplierId) <= 0) {
            throw new ServiceException("供应商删除失败");
        }
    }

    // ==================== 供应商评价 ====================

    public Page<SupplierEvaluation> getEvaluationsByPage(Integer supplierId, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<SupplierEvaluation> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(SupplierEvaluation::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(SupplierEvaluation::getEvaluationId);

        Page<SupplierEvaluation> page = supplierEvaluationMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillEvaluationNames(page.getRecords());
        return page;
    }

    public List<SupplierEvaluation> getEvaluationsBySupplier(Integer supplierId) {
        List<SupplierEvaluation> list = supplierEvaluationMapper.selectList(
                new LambdaQueryWrapper<SupplierEvaluation>()
                        .eq(SupplierEvaluation::getSupplierId, supplierId)
                        .orderByDesc(SupplierEvaluation::getEvaluationId)
        );
        fillEvaluationNames(list);
        return list;
    }

    @Transactional
    public void createEvaluation(SupplierEvaluation evaluation) {
        if (supplierMapper.selectById(evaluation.getSupplierId()) == null) {
            throw new ServiceException("供应商不存在");
        }
        if (evaluation.getOrderId() != null && purchaseOrderMapper.selectById(evaluation.getOrderId()) == null) {
            throw new ServiceException("关联的采购订单不存在");
        }
        int total = evaluation.getDeliverySpeedScore() + evaluation.getQualityScore()
                + evaluation.getServiceScore() + evaluation.getPriceScore();
        evaluation.setTotalScore(total);
        evaluation.setEvaluationId(null);
        LocalDateTime now = LocalDateTime.now();
        evaluation.setCreatedAt(now);
        evaluation.setUpdatedAt(now);
        if (supplierEvaluationMapper.insert(evaluation) <= 0) {
            throw new ServiceException("评价提交失败");
        }
    }

    private void fillEvaluationNames(List<SupplierEvaluation> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Integer, String> supplierNames = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(Supplier::getSupplierId, Supplier::getSupplierName, (a, b) -> a));
        Map<Integer, String> orderNos = purchaseOrderMapper.selectList(null).stream()
                .collect(Collectors.toMap(PurchaseOrder::getOrderId, PurchaseOrder::getOrderNo, (a, b) -> a));
        for (SupplierEvaluation evaluation : list) {
            evaluation.setSupplierName(supplierNames.get(evaluation.getSupplierId()));
            evaluation.setOrderNo(orderNos.get(evaluation.getOrderId()));
        }
    }

    private boolean isNameExists(String supplierName, Integer excludeId) {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            throw new ServiceException("供应商名称不能为空");
        }
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierName, supplierName.trim());
        if (excludeId != null) {
            wrapper.ne(Supplier::getSupplierId, excludeId);
        }
        Long count = supplierMapper.selectCount(wrapper);
        return count != null && count > 0;
    }
}

package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.ProductBatchQuality;
import org.example.springboot.entity.Supplier;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ProductBatchQualityMapper;
import org.example.springboot.mapper.ProductMapper;
import org.example.springboot.mapper.SupplierMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品批次质量业务层
 */
@Service
public class ProductBatchQualityService {
    @Resource
    private ProductBatchQualityMapper qualityMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private SupplierMapper supplierMapper;

    @Resource
    private UserMapper userMapper;

    public Page<ProductBatchQuality> getQualityByPage(Integer productId, Integer supplierId,
                                                      Integer qualityLevel, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<ProductBatchQuality> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(ProductBatchQuality::getProductId, productId);
        }
        if (supplierId != null) {
            wrapper.eq(ProductBatchQuality::getSupplierId, supplierId);
        }
        if (qualityLevel != null) {
            wrapper.eq(ProductBatchQuality::getQualityLevel, qualityLevel);
        }
        wrapper.orderByDesc(ProductBatchQuality::getQualityId);

        Page<ProductBatchQuality> page = qualityMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Transactional
    public void createQuality(ProductBatchQuality quality) {
        if (quality.getProductId() == null || productMapper.selectById(quality.getProductId()) == null) {
            throw new ServiceException("商品不存在");
        }
        if (quality.getSupplierId() == null || supplierMapper.selectById(quality.getSupplierId()) == null) {
            throw new ServiceException("供应商不存在");
        }
        if (quality.getQualityLevel() == null || quality.getQualityLevel() < 1 || quality.getQualityLevel() > 4) {
            throw new ServiceException("质量等级只能是 1-4");
        }
        if (quality.getUserId() == null) {
            User current = JwtTokenUtils.getCurrentUser();
            if (current != null) {
                quality.setUserId(current.getUserId());
            }
        }
        if (quality.getUserId() == null) {
            throw new ServiceException("请先登录后再提交质量评价");
        }
        LocalDateTime now = LocalDateTime.now();
        quality.setQualityId(null);
        quality.setCreatedAt(now);
        quality.setUpdatedAt(now);
        if (qualityMapper.insert(quality) <= 0) {
            throw new ServiceException("质量记录保存失败");
        }
    }

    @Transactional
    public void deleteQuality(Integer qualityId) {
        if (qualityMapper.selectById(qualityId) == null) {
            throw new ServiceException("质量记录不存在");
        }
        if (qualityMapper.deleteById(qualityId) <= 0) {
            throw new ServiceException("质量记录删除失败");
        }
    }

    private void fillNames(List<ProductBatchQuality> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Integer, String> productNames = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getProductName, (a, b) -> a));
        Map<Integer, String> supplierNames = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(Supplier::getSupplierId, Supplier::getSupplierName, (a, b) -> a));
        Map<Integer, String> userNames = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getUserId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername(), (a, b) -> a));

        list.forEach(item -> {
            item.setProductName(productNames.get(item.getProductId()));
            item.setSupplierName(supplierNames.get(item.getSupplierId()));
            item.setUserName(userNames.get(item.getUserId()));
        });
    }

    /**
     * 某供应商的批次质量平均分（1-4，越小越好）
     */
    public Double getAverageLevelBySupplier(Integer supplierId) {
        List<ProductBatchQuality> list = qualityMapper.selectList(
                new LambdaQueryWrapper<ProductBatchQuality>().eq(ProductBatchQuality::getSupplierId, supplierId)
        );
        return list.stream()
                .map(ProductBatchQuality::getQualityLevel)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average().orElse(0);
    }
}

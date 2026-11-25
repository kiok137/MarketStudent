package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Category;
import org.example.springboot.entity.Product;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.CategoryMapper;
import org.example.springboot.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类业务层
 */
@Service
public class CategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 查询全部分类（按ID升序）
     */
    public List<Category> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getCategoryId)
        );
        fillParentName(categories);
        return categories;
    }

    /**
     * 查询分类树（父子结构）
     */
    public List<Category> getCategoryTree() {
        List<Category> all = getAllCategories();
        Map<Integer, Category> idMap = all.stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c, (a, b) -> a, LinkedHashMap::new));

        List<Category> roots = new ArrayList<>();
        for (Category category : all) {
            Integer parentId = category.getParentId();
            if (parentId == null || parentId == 0 || !idMap.containsKey(parentId)) {
                roots.add(category);
            } else {
                Category parent = idMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(category);
            }
        }
        return roots;
    }

    /**
     * 分页查询分类
     */
    public Page<Category> getCategoriesByPage(String categoryName, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            wrapper.like(Category::getCategoryName, categoryName.trim());
        }
        wrapper.orderByAsc(Category::getCategoryId);

        Page<Category> page = categoryMapper.selectPage(
                new Page<>(currentPage == null ? 1 : currentPage, pageSize == null ? 10 : pageSize), wrapper);
        fillParentName(page.getRecords());
        return page;
    }

    /**
     * 分类及其商品数量（供仪表盘饼图使用）
     */
    public List<Map<String, Object>> getCategoriesWithProductCount() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getCategoryId)
        );
        List<Product> products = productMapper.selectList(null);

        Map<Integer, Long> countMap = products.stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(Product::getCategoryId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Category category : categories) {
            long count = countMap.getOrDefault(category.getCategoryId(), 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getCategoryName());
            item.put("value", count);
            item.put("productCount", count);
            item.put("categoryId", category.getCategoryId());
            result.add(item);
        }
        return result;
    }

    public Category getCategoryById(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        return category;
    }

    @Transactional
    public void createCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new ServiceException("分类名称不能为空");
        }
        if (isNameExists(category.getCategoryName().trim(), null)) {
            throw new ServiceException("分类名称已存在");
        }
        if (category.getParentId() != null && category.getParentId() != 0
                && categoryMapper.selectById(category.getParentId()) == null) {
            throw new ServiceException("父分类不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        category.setCategoryId(null);
        category.setCategoryName(category.getCategoryName().trim());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        if (categoryMapper.insert(category) <= 0) {
            throw new ServiceException("分类创建失败");
        }
    }

    @Transactional
    public void updateCategory(Category category) {
        if (category.getCategoryId() == null) {
            throw new ServiceException("分类ID不能为空");
        }
        Category existing = categoryMapper.selectById(category.getCategoryId());
        if (existing == null) {
            throw new ServiceException("分类不存在");
        }
        if (isNameExists(category.getCategoryName(), category.getCategoryId())) {
            throw new ServiceException("分类名称已存在");
        }
        if (category.getParentId() != null && category.getParentId().equals(category.getCategoryId())) {
            throw new ServiceException("父分类不能是自己");
        }
        category.setCreatedAt(existing.getCreatedAt());
        category.setUpdatedAt(LocalDateTime.now());

        if (categoryMapper.updateById(category) <= 0) {
            throw new ServiceException("分类更新失败");
        }
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        Category existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new ServiceException("分类不存在");
        }
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, categoryId)
        );
        if (productCount != null && productCount > 0) {
            throw new ServiceException("该分类下还有 " + productCount + " 个商品，无法删除");
        }
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId)
        );
        if (childCount != null && childCount > 0) {
            throw new ServiceException("该分类下还有子分类，无法删除");
        }
        if (categoryMapper.deleteById(categoryId) <= 0) {
            throw new ServiceException("分类删除失败");
        }
    }

    private boolean isNameExists(String categoryName, Integer excludeId) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, categoryName.trim());
        if (excludeId != null) {
            wrapper.ne(Category::getCategoryId, excludeId);
        }
        Long count = categoryMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private void fillParentName(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        Map<Integer, String> idToName = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName, (a, b) -> a));
        for (Category category : categories) {
            if (category.getParentId() != null) {
                category.setParentName(idToName.get(category.getParentId()));
            }
        }
    }
}

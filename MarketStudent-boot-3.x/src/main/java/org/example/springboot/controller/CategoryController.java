package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Category;
import org.example.springboot.service.CategoryService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品分类接口")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Operation(summary = "获取全部分类")
    @GetMapping("/all")
    public Result<?> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<?> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Category> page = categoryService.getCategoriesByPage(categoryName, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "获取分类及其商品数量")
    @GetMapping("/stats")
    public Result<?> getCategoryStats() {
        return Result.success(categoryService.getCategoriesWithProductCount());
    }

    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Integer id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public Result<?> createCategory(@RequestBody Category category) {
        categoryService.createCategory(category);
        return Result.success("分类创建成功");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setCategoryId(id);
        categoryService.updateCategory(category);
        return Result.success("分类更新成功");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }
}

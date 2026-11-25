package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Product;
import org.example.springboot.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品管理接口")
@RestController
@RequestMapping("/product")
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Resource
    private ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public Result<?> getProductsByPage(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> page = productService.getProductsByPage(
                productName, categoryId, barcode, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取商品")
    @GetMapping("/{id}")
    public Result<?> getProductById(@PathVariable Integer id) {
        return Result.success(productService.getProductById(id));
    }

    @Operation(summary = "获取全部商品")
    @GetMapping("/all")
    public Result<?> getAllProducts() {
        return Result.success(productService.getAllProducts());
    }

    @Operation(summary = "按分类获取商品")
    @GetMapping("/category/{categoryId}")
    public Result<?> getProductsByCategory(@PathVariable Integer categoryId) {
        return Result.success(productService.getProductsByCategory(categoryId));
    }

    @Operation(summary = "关键字搜索商品")
    @GetMapping("/search")
    public Result<?> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProductsByKeyword(keyword);
        return Result.success(products);
    }

    @Operation(summary = "新增商品")
    @PostMapping("/add")
    public Result<?> createProduct(@RequestBody Product product) {
        productService.createProduct(product);
        return Result.success("商品创建成功");
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<?> updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        product.setProductId(id);
        productService.updateProduct(product);
        return Result.success("商品更新成功");
    }

    @Operation(summary = "更新商品上下架状态")
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        productService.updateProductStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }
}

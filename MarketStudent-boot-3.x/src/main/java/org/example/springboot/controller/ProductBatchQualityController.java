package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ProductBatchQuality;
import org.example.springboot.service.ProductBatchQualityService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品批次质量接口")
@RestController
@RequestMapping("/quality")
public class ProductBatchQualityController {

    @Resource
    private ProductBatchQualityService qualityService;

    @Operation(summary = "分页查询批次质量记录")
    @GetMapping("/page")
    public Result<?> getQualityByPage(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) Integer qualityLevel,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ProductBatchQuality> page = qualityService.getQualityByPage(
                productId, supplierId, qualityLevel, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "新增批次质量记录")
    @PostMapping("/add")
    public Result<?> createQuality(@RequestBody ProductBatchQuality quality) {
        qualityService.createQuality(quality);
        return Result.success("质量记录提交成功");
    }

    @Operation(summary = "删除批次质量记录")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteQuality(@PathVariable Integer id) {
        qualityService.deleteQuality(id);
        return Result.success("质量记录删除成功");
    }
}

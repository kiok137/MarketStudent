package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Supplier;
import org.example.springboot.entity.SupplierEvaluation;
import org.example.springboot.service.SupplierService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理接口")
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @GetMapping("/page")
    public Result<?> getSuppliersByPage(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Supplier> page = supplierService.getSuppliersByPage(supplierName, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "获取全部启用供应商")
    @GetMapping("/all")
    public Result<?> getAllSuppliers() {
        return Result.success(supplierService.getAllSuppliers());
    }

    @Operation(summary = "根据ID获取供应商")
    @GetMapping("/{id}")
    public Result<?> getSupplierById(@PathVariable Integer id) {
        return Result.success(supplierService.getSupplierById(id));
    }

    @Operation(summary = "新增供应商")
    @PostMapping("/add")
    public Result<?> createSupplier(@RequestBody Supplier supplier) {
        supplierService.createSupplier(supplier);
        return Result.success("供应商创建成功");
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public Result<?> updateSupplier(@PathVariable Integer id, @RequestBody Supplier supplier) {
        supplier.setSupplierId(id);
        supplierService.updateSupplier(supplier);
        return Result.success("供应商更新成功");
    }

    @Operation(summary = "更新供应商状态")
    @PutMapping("/status/{id}")
    public Result<?> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        supplierService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteSupplier(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);
        return Result.success("供应商删除成功");
    }

    // ==================== 供应商评价 ====================

    @Operation(summary = "分页查询供应商评价")
    @GetMapping("/evaluation/page")
    public Result<?> getEvaluationsByPage(
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SupplierEvaluation> page = supplierService.getEvaluationsByPage(supplierId, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "查询某供应商的全部评价")
    @GetMapping("/{id}/evaluations")
    public Result<?> getEvaluationsBySupplier(@PathVariable Integer id) {
        return Result.success(supplierService.getEvaluationsBySupplier(id));
    }

    @Operation(summary = "提交供应商评价")
    @PostMapping("/evaluation/add")
    public Result<?> createEvaluation(@RequestBody SupplierEvaluation evaluation) {
        supplierService.createEvaluation(evaluation);
        return Result.success("评价提交成功");
    }
}

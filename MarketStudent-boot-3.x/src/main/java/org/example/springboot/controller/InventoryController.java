package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Inventory;
import org.example.springboot.entity.StockIn;
import org.example.springboot.entity.StockOut;
import org.example.springboot.service.InventoryService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存管理接口")
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Resource
    private InventoryService inventoryService;

    @Operation(summary = "分页查询库存")
    @GetMapping("/page")
    public Result<?> getInventoryByPage(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Inventory> page = inventoryService.getInventoryByPage(productName, lowStock, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "获取全部库存")
    @GetMapping("/all")
    public Result<?> getAllInventory() {
        return Result.success(inventoryService.getAllInventory());
    }

    @Operation(summary = "库存预警数量")
    @GetMapping("/low/count")
    public Result<?> countLowInventory() {
        return Result.success(inventoryService.countLowInventory());
    }

    @Operation(summary = "更新库存上下限")
    @PutMapping("/{id}/threshold")
    public Result<?> updateThreshold(@PathVariable Integer id,
                                     @RequestParam(required = false) Integer minQuantity,
                                     @RequestParam(required = false) Integer maxQuantity) {
        inventoryService.updateThreshold(id, minQuantity, maxQuantity);
        return Result.success("库存阈值更新成功");
    }

    // ==================== 入库管理 ====================

    @Operation(summary = "分页查询入库记录")
    @GetMapping("/stockin/page")
    public Result<?> getStockInByPage(
            @RequestParam(required = false) String stockInNo,
            @RequestParam(required = false) Integer productId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<StockIn> page = inventoryService.getStockInByPage(stockInNo, productId, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "新增入库单")
    @PostMapping("/stockin/add")
    public Result<?> createStockIn(@RequestBody StockIn stockIn) {
        inventoryService.createStockIn(stockIn);
        return Result.success("入库成功，库存已增加");
    }

    // ==================== 出库管理 ====================

    @Operation(summary = "分页查询出库记录")
    @GetMapping("/stockout/page")
    public Result<?> getStockOutByPage(
            @RequestParam(required = false) String stockOutNo,
            @RequestParam(required = false) Integer productId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<StockOut> page = inventoryService.getStockOutByPage(stockOutNo, productId, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "新增出库单")
    @PostMapping("/stockout/add")
    public Result<?> createStockOut(@RequestBody StockOut stockOut) {
        inventoryService.createStockOut(stockOut);
        return Result.success("出库成功，库存已扣减");
    }
}

package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PurchaseOrder;
import org.example.springboot.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购管理接口")
@RestController
@RequestMapping("/purchase")
public class PurchaseOrderController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Operation(summary = "分页查询采购订单")
    @GetMapping("/page")
    public Result<?> getOrdersByPage(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PurchaseOrder> page = purchaseOrderService.getOrdersByPage(
                orderNo, supplierId, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取采购订单（含明细）")
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable Integer id) {
        return Result.success(purchaseOrderService.getOrderById(id));
    }

    @Operation(summary = "新增采购订单")
    @PostMapping("/add")
    public Result<?> createOrder(@RequestBody PurchaseOrder order) {
        purchaseOrderService.createOrder(order);
        return Result.success("采购订单创建成功");
    }

    @Operation(summary = "审核采购订单")
    @PutMapping("/{id}/approve")
    public Result<?> approveOrder(@PathVariable Integer id) {
        purchaseOrderService.approveOrder(id);
        return Result.success("订单审核通过");
    }

    @Operation(summary = "确认收货并自动入库")
    @PutMapping("/{id}/receive")
    public Result<?> receiveOrder(@PathVariable Integer id) {
        purchaseOrderService.receiveOrder(id);
        return Result.success("收货成功，库存已更新");
    }

    @Operation(summary = "取消采购订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable Integer id) {
        purchaseOrderService.cancelOrder(id);
        return Result.success("订单已取消");
    }

    @Operation(summary = "删除采购订单")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteOrder(@PathVariable Integer id) {
        purchaseOrderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }
}

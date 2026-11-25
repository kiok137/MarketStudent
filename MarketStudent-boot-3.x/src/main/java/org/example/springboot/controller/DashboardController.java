package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.CategoryService;
import org.example.springboot.service.InventoryService;
import org.example.springboot.service.ProductService;
import org.example.springboot.service.PurchaseOrderService;
import org.example.springboot.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "仪表盘接口")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    @Resource
    private ProductService productService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private CategoryService categoryService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public Result<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取商品总数
            Integer productCount = productService.count();
            stats.put("products", productCount);
            
            // 获取供应商数量
            Integer supplierCount = supplierService.count();
            stats.put("suppliers", supplierCount);
            
            // 获取采购订单数量
            Integer orderCount = purchaseOrderService.count();
            stats.put("orders", orderCount);
            
            // 获取库存预警数量
            Integer warningCount = inventoryService.countLowInventory();
            stats.put("warnings", warningCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            LOGGER.error("获取仪表盘统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取近期采购统计数据")
    @GetMapping("/purchases")
    public Result<?> getPurchaseStats(@RequestParam(required = false, defaultValue = "7days") String timeRange) {
        try {
            // 根据timeRange参数确定要查询的天数
            int days = 7; // 默认为7天
            
            switch (timeRange) {
                case "7days":
                    days = 7;
                    break;
                case "30days":
                    days = 30;
                    break;
                case "90days":
                    days = 90;
                    break;
                default:
                    days = 7;
            }
            LOGGER.error("获取采购统计数据范围"+ timeRange);
            List<Map<String, Object>> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            
            // 这里可以从数据库获取真实数据，现在使用示例数据
            LocalDate today = LocalDate.now();
            
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                BigDecimal amount = purchaseOrderService.getAmountByDate(date);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.format(formatter));
                dayData.put("amount", amount);
                result.add(dayData);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            LOGGER.error("获取采购统计数据失败", e);
            return Result.error("获取采购统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商品分类统计数据")
    @GetMapping("/categories")
    public Result<?> getCategoryStats() {
        try {
            List<Map<String, Object>> result = categoryService.getCategoriesWithProductCount();
            return Result.success(result);
        } catch (Exception e) {
            LOGGER.error("获取分类统计数据失败", e);
            return Result.error("获取分类统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取采购与库存对比数据")
    @GetMapping("/compare")
    public Result<?> getCompareStats(@RequestParam(required = false, defaultValue = "month") String timeRange) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 根据时间范围参数获取不同的数据
            // timeRange可能的值: month, quarter, year
            
            // 获取各分类的采购与库存数据
            List<Map<String, Object>> categories = categoryService.getCategoriesWithProductCount();
            
            for (Map<String, Object> category : categories) {
                String categoryName = (String) category.get("name");
                
                // 获取该分类的采购额和库存量
                BigDecimal purchases = productService.getSalesByCategoryAndTimeRange(categoryName, timeRange);
                Integer stock = inventoryService.getStockByCategory(categoryName);
                
                Map<String, Object> compareData = new HashMap<>();
                compareData.put("category", categoryName);
                compareData.put("sales", purchases);  // 保留sales字段名以兼容前端
                compareData.put("stock", stock);
                
                result.add(compareData);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            LOGGER.error("获取采购与库存对比数据失败", e);
            return Result.error("获取采购与库存对比数据失败: " + e.getMessage());
        }
    }
} 
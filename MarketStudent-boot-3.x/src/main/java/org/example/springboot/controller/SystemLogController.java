package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.SystemLog;
import org.example.springboot.service.SystemLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@Tag(name = "系统操作日志管理接口")
@RestController
@RequestMapping("/systemLog")
public class SystemLogController {

    @Resource
    private SystemLogService systemLogService;

    @Operation(summary = "获取单个系统日志")
    @GetMapping("/{logId}")
    public Result<?> getById(@PathVariable Integer logId) {
        SystemLog systemLog = systemLogService.getSystemLogById(logId);
        return Result.success(systemLog);
    }

    @Operation(summary = "分页查询系统日志")
    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<SystemLog> page = systemLogService.getSystemLogsByPage(
                username, operationType, operationModule, ipAddress, status, startTime, endTime, currentPage, pageSize);
        
        return Result.success(page);
    }

    @Operation(summary = "清空所有系统日志（该操作会永久删除所有日志记录，请谨慎使用）")
    @DeleteMapping("/clear")
    public Result<?> clearAllLogs() {
        boolean success = systemLogService.clearAllLogs();
        if (success) {
            return Result.success("清空日志成功");
        } else {
            return Result.error("清空日志失败");
        }
    }
} 
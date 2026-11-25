package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.SystemLog;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.OperationType;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.SystemLogMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemLogService {
    
    @Resource
    private SystemLogMapper systemLogMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 创建系统日志
     */
    public void createSystemLog(SystemLog systemLog) {
        // 检查用户是否存在
        if (systemLog.getUserId() != null) {
            User user = userMapper.selectById(systemLog.getUserId());
            if (user == null) {
                throw new ServiceException("用户不存在");
            }
        }
        
        // 设置操作时间
        if (systemLog.getOperationTime() == null) {
            systemLog.setOperationTime(LocalDateTime.now());
        }
        
        // 保存系统日志
        if (systemLogMapper.insert(systemLog) <= 0) {
            throw new ServiceException("创建系统日志失败");
        }
    }
    
    /**
     * 根据ID获取系统日志
     */
    public SystemLog getSystemLogById(Integer logId) {
        SystemLog systemLog = systemLogMapper.selectById(logId);
        if (systemLog == null) {
            throw new ServiceException("系统日志不存在");
        }
        
        // 设置用户名
        if (systemLog.getUserId() != null) {
            User user = userMapper.selectById(systemLog.getUserId());
            if (user != null) {
                systemLog.setUsername(user.getUsername());
            }
        }
        
        return systemLog;
    }
    
    /**
     * 分页查询系统日志
     */
    public Page<SystemLog> getSystemLogsByPage(
            String username,
            String operationType,
            String operationModule,
            String ipAddress,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer currentPage,
            Integer pageSize) {
        
        LambdaQueryWrapper<SystemLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        // 用户名查询需要特殊处理，因为它是通过关联表获取的
        if (StringUtils.isNotBlank(username)) {
            if(username.equals("未知用户")){
                queryWrapper.isNull(SystemLog::getUserId);
            } else {
                // 查找匹配用户名的用户ID
                List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, username));
                if (!users.isEmpty()) {
                    List<Integer> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
                    queryWrapper.in(SystemLog::getUserId, userIds);
                } else {
                    // 如果没有找到匹配的用户名，设置不可能的条件，保证查询结果为空
                    queryWrapper.eq(SystemLog::getLogId, -1);
                }
            }
        }
        
        if (StringUtils.isNotBlank(operationType)) {
            queryWrapper.eq(SystemLog::getOperationType, Integer.parseInt(operationType));
        }
        
        if (operationModule != null && !operationModule.trim().isEmpty()) {
            queryWrapper.like(SystemLog::getOperationModule, operationModule);
        }
        
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            queryWrapper.like(SystemLog::getIpAddress, ipAddress);
        }
        
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(SystemLog::getStatus, Integer.parseInt(status));
        }
        
        if (startTime != null) {
            queryWrapper.ge(SystemLog::getOperationTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(SystemLog::getOperationTime, endTime);
        }
        
        // 按操作时间降序排序
        queryWrapper.orderByDesc(SystemLog::getOperationTime);
        
        // 执行分页查询
        Page<SystemLog> page = systemLogMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
        
        // 设置关联信息
        setRelatedInfo(page.getRecords());
        
        return page;
    }
    
    /**
     * 记录登录日志
     */
    public void recordLoginLog(Integer userId, String ipAddress, String deviceInfo, Integer status) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setOperationType(OperationType.LOGIN.getCode());
        log.setOperationModule("用户管理");
        log.setOperationDesc(status == 1 ? "用户登录成功" : "用户登录失败");
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setStatus(status);
        log.setOperationTime(LocalDateTime.now());
        
        createSystemLog(log);
    }
    
    /**
     * 记录登出日志
     */
    public void recordLogoutLog(Integer userId, String ipAddress, String deviceInfo) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setOperationType(OperationType.LOGOUT.getCode());
        log.setOperationModule("用户管理");
        log.setOperationDesc("用户登出");
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setStatus(1); // 成功
        log.setOperationTime(LocalDateTime.now());
        
        createSystemLog(log);
    }
    
    /**
     * 记录操作日志
     */
    public void recordOperationLog(Integer userId, OperationType operationType, 
                                String operationModule, String operationDesc, 
                                String ipAddress, String requestUrl, 
                                String requestMethod, String requestParams, 
                                Long executionTime, Integer status, String exceptionInfo,
                                String deviceInfo) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setOperationType(operationType.getCode());
        log.setOperationModule(operationModule);
        log.setOperationDesc(operationDesc);
        log.setIpAddress(ipAddress);
        log.setRequestUrl(requestUrl);
        log.setRequestMethod(requestMethod);
        log.setRequestParams(requestParams);
        log.setExecutionTime(executionTime);
        log.setStatus(status);
        log.setExceptionInfo(exceptionInfo);
        log.setDeviceInfo(deviceInfo);
        log.setOperationTime(LocalDateTime.now());
        
        createSystemLog(log);
    }
    
    /**
     * 设置关联信息
     */
    private void setRelatedInfo(List<SystemLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        
        // 获取所有用户ID（过滤掉null值）
        List<Integer> userIds = logs.stream()
                .map(SystemLog::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return;
        }
        
        // 批量查询用户信息
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .in(User::getUserId, userIds)
        );
        
        // 转换为Map方便查找
        Map<Integer, String> userNameMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));
        
        // 设置用户名
        for (SystemLog log : logs) {
            if (log.getUserId() != null) {
                log.setUsername(userNameMap.get(log.getUserId()));
            }
        }
    }
    
    /**
     * 清空所有系统日志
     * 该操作会永久删除所有日志记录，请谨慎使用
     */
    public boolean clearAllLogs() {
        try {
            // 直接使用mapper的delete方法，传入一个空的条件构造器，即删除全部记录
            LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
            systemLogMapper.delete(wrapper);
            return true;
        } catch (Exception e) {
            throw new ServiceException("清空日志失败: " + e.getMessage());
        }
    }
} 
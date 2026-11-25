package org.example.springboot.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.springboot.annotation.OperationLog;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.OperationType;
import org.example.springboot.service.SystemLogService;
import org.example.springboot.util.IpUtil;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.UserUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
@Order(1)
public class OperationLogAspect {

    @Resource
    private SystemLogService systemLogService;
    
    @Resource
    private ObjectMapper objectMapper;
    
    /**
     * 配置切入点，拦截所有带有@RestController注解的类中的方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void operationLogPointCut() {
    }
    
    /**
     * 环绕通知，记录操作日志
     */
    @Around("operationLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 执行时长(毫秒)
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 保存日志
        try {
            saveOperationLog(joinPoint, null, executionTime, 1);
        } catch (Exception e) {
            // 记录日志出错，不影响业务逻辑
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 异常通知，记录操作日志
     */
    @AfterThrowing(value = "operationLogPointCut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            saveOperationLog(joinPoint, e.getMessage(), 0L, 0);
        } catch (Exception ex) {
            // 记录日志出错，不影响业务逻辑
            ex.printStackTrace();
        }
    }
    
    /**
     * 保存操作日志
     */
    private void saveOperationLog(JoinPoint joinPoint, String exceptionInfo, Long executionTime, Integer status) throws Exception {
        // 获取请求相关信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String requestUrl = request.getRequestURI();
        
        // 只处理/api开头的请求
        if (!requestUrl.startsWith("/api/")) {
            return;
        }
        
        // 对登录请求做特殊处理
        boolean isLoginRequest = requestUrl.endsWith("/api/user/login");
        Integer userId = null;
        
        if (!isLoginRequest) {
            // 非登录请求，获取当前用户ID
            User user = JwtTokenUtils.getCurrentUser();
            if (user != null) {
                userId = Integer.parseInt(String.valueOf(user.getUserId()));
            }
            if (userId == null) {
                // 没有登录用户，不记录日志
                return;
            }
        }
        
        String ipAddress = IpUtil.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestMethod = request.getMethod();
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取方法参数，过滤掉敏感信息
        String requestParams = getRequestParams(joinPoint);
        
        // 操作类型和操作描述
        OperationType operationType = OperationType.OTHER;
        String operationDesc = "";
        String operationModule = "";
        
        // 获取方法上的OperationLog注解
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        if (operationLog != null) {
            operationType = operationLog.operationType();
            operationDesc = operationLog.operationDesc();
            operationModule = operationLog.operationModule();
        } else {
            // 如果是登录请求
            if (isLoginRequest) {
                operationType = OperationType.LOGIN;
                operationDesc = "用户登录";
                operationModule = "用户管理";
            } else {
                // 根据方法名推断操作类型
                String methodName = method.getName();
                if (methodName.startsWith("create") || methodName.startsWith("add") || methodName.startsWith("save")) {
                    operationType = OperationType.INSERT;
                    operationDesc = "新增数据";
                } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
                    operationType = OperationType.DELETE;
                    operationDesc = "删除数据";
                } else if (methodName.startsWith("update") || methodName.startsWith("modify")) {
                    operationType = OperationType.UPDATE;
                    operationDesc = "更新数据";
                } else {
                    // 如果是查询操作，直接返回不记录日志
                    if (methodName.startsWith("get") || methodName.startsWith("find") || 
                        methodName.startsWith("query") || methodName.startsWith("select") || 
                        methodName.startsWith("list") || methodName.startsWith("page")) {
                        return;
                    }
                    operationType = OperationType.OTHER;
                    operationDesc = "其他操作";
                }
                
                // 从requestURI推断操作模块
                // 移除/api前缀
                String path = requestUrl;
                if (path.startsWith("/api/")) {
                    path = path.substring(5); // 移除"/api/"
                }
                
                String[] uriParts = path.split("/");
                if (uriParts.length > 0) {
                    operationModule = uriParts[0];
                    // 首字母大写，使显示更美观
                    if (!operationModule.isEmpty()) {
                        operationModule = operationModule.substring(0, 1).toUpperCase() + 
                                         (operationModule.length() > 1 ? operationModule.substring(1) : "");
                    }
                }
            }
        }
        
        // 记录操作日志
        systemLogService.recordOperationLog(
            userId, 
            operationType, 
            operationModule, 
            operationDesc, 
            ipAddress, 
            requestUrl, 
            requestMethod, 
            requestParams, 
            executionTime, 
            status, 
            exceptionInfo,
            userAgent  // 添加设备信息
        );
    }
    
    /**
     * 获取请求参数，过滤敏感信息
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();
            
            // 如果是登录请求，只记录用户名
            if (methodName.equals("login")) {
                Map<String, Object> loginParams = new HashMap<>();
                String[] parameterNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                
                for (int i = 0; i < parameterNames.length; i++) {
                    if ("username".equals(parameterNames[i])) {
                        loginParams.put("username", args[i]);
                        break;
                    }
                }
                return objectMapper.writeValueAsString(loginParams);
            }
            
            // 其他请求的参数处理
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            // 构建参数Map
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                String paramName = parameterNames[i];
                Object paramValue = args[i];
                
                // 跳过特定类型的参数
                if (paramValue instanceof HttpServletRequest || 
                    paramValue instanceof HttpServletResponse) {
                    continue;
                }
                
                // 过滤敏感参数
                if (isSensitiveParam(paramName)) {
                    params.put(paramName, "******");
                } else if (paramValue != null) {
                    // 对参数值进行处理
                    params.put(paramName, formatParamValue(paramValue));
                }
            }
            
            // 转为JSON字符串，并限制长度
            String jsonParams = objectMapper.writeValueAsString(params);
            return truncateString(jsonParams, 1000); // 限制参数长度为1000字符
        } catch (Exception e) {
            return "参数解析失败";
        }
    }
    
    /**
     * 判断是否是敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        return paramName.toLowerCase().contains("password") ||
               paramName.toLowerCase().contains("token") ||
               paramName.toLowerCase().contains("secret") ||
               paramName.toLowerCase().contains("key") ||
               paramName.toLowerCase().contains("cookie") ||
               paramName.toLowerCase().contains("session");
    }
    
    /**
     * 格式化参数值
     */
    private Object formatParamValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // 如果是基本类型或String，直接返回
        if (value instanceof String || 
            value instanceof Number || 
            value instanceof Boolean) {
            return value;
        }
        
        // 如果是数组，返回长度信息
        if (value.getClass().isArray()) {
            return "Array[" + Arrays.asList((Object[])value).size() + "]";
        }
        
        // 如果是集合，返回大小信息
        if (value instanceof Collection) {
            return "Collection[" + ((Collection<?>)value).size() + "]";
        }
        
        // 如果是Map，返回大小信息
        if (value instanceof Map) {
            return "Map[" + ((Map<?,?>)value).size() + "]";
        }
        
        // 其他复杂对象，只返回类名
        return value.getClass().getSimpleName() + "@" + Integer.toHexString(value.hashCode());
    }
    
    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
} 
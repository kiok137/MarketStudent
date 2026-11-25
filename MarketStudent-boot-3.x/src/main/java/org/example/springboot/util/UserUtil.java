package org.example.springboot.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户工具类
 */
public class UserUtil {
    
    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，如果未登录则返回null
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        // 根据实际的认证对象类型获取用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // 如果使用UserDetails，通常username会存储用户的唯一标识
            // 这里需要根据实际情况转换为用户ID
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof Integer) {
            return principal.toString();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
    
    /**
     * 获取当前登录用户ID(整数类型)
     *
     * @return 用户ID，如果未登录或无法转换则返回0
     */
    public static Integer getCurrentUserIdAsInt() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 获取当前登录用户名
     * 
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
} 
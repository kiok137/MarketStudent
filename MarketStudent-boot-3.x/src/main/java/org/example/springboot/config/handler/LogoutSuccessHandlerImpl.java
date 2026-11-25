package org.example.springboot.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot.entity.User;
import org.example.springboot.service.SystemLogService;
import org.example.springboot.util.IpUtil;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.UserUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final SystemLogService systemLogService;

    public LogoutSuccessHandlerImpl(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException, ServletException {
        try {
            if (authentication != null && authentication.getPrincipal() != null) {
                User user = JwtTokenUtils.getCurrentUser();
                String ipAddress = IpUtil.getIpAddress(request);
                String deviceInfo = request.getHeader("User-Agent");
                
                // 记录登出日志
                if (user != null) {
                    systemLogService.recordLogoutLog(user.getUserId(), ipAddress, deviceInfo);
                }else{
                    systemLogService.recordLogoutLog(null, ipAddress, deviceInfo);
                }
            }
            
            // 重定向到登录页面
            response.sendRedirect("/login");
        } catch (Exception e) {
            // 记录日志失败不应影响登出流程
            e.printStackTrace();
            response.sendRedirect("/login");
        }
    }
} 
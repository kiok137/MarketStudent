package org.example.springboot.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot.entity.User;
import org.example.springboot.service.SystemLogService;
import org.example.springboot.service.UserService;
import org.example.springboot.util.IpUtil;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final SystemLogService systemLogService;
    private final UserService userService;

    public LoginFailureHandler(SystemLogService systemLogService, @Lazy UserService userService) {
        this.systemLogService = systemLogService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        try {
            String username = request.getParameter("username");
            String ipAddress = IpUtil.getIpAddress(request);
            String deviceInfo = request.getHeader("User-Agent");
            
            // 尝试获取用户ID
            User user  = JwtTokenUtils.getCurrentUser();
            // 将用户名转换为整数ID（如果可能）
            Integer userId = null;
            try {
                if (user != null) {
                    userId = Integer.parseInt(String.valueOf(user.getUserId()));
                }
            } catch (NumberFormatException e) {
                userId = 0; // 如果用户名不是数字格式，使用0
            }
            
            // 记录登录失败日志
            systemLogService.recordLoginLog(userId, ipAddress, deviceInfo, 0); // 0表示失败
            
            // 调用父类方法继续处理登录失败后的操作
            super.onAuthenticationFailure(request, response, exception);
        } catch (Exception e) {
            // 记录日志失败不应影响登录失败流程
            e.printStackTrace();
            super.onAuthenticationFailure(request, response, exception);
        }
    }
} 
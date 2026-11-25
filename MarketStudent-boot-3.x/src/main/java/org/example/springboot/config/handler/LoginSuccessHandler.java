package org.example.springboot.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot.entity.User;
import org.example.springboot.service.SystemLogService;
import org.example.springboot.util.IpUtil;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final SystemLogService systemLogService;

    public LoginSuccessHandler(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            String ipAddress = IpUtil.getIpAddress(request);
            String deviceInfo = request.getHeader("User-Agent");

            User user = JwtTokenUtils.getCurrentUser();
            // 将用户名转换为整数ID（如果可能）
            Integer userId = null;
            try {
                if (user != null) {
                    userId = Integer.parseInt(String.valueOf(user.getUserId()));
                    // 生成token
                    String token = JwtTokenUtils.genToken(String.valueOf(userId), user.getPassword());
                    // 设置响应类型
                    response.setContentType("application/json;charset=UTF-8");
                    // 返回token和用户信息
                    response.getWriter().write("{\"code\":\"200\",\"msg\":\"登录成功\",\"data\":{\"token\":\"" + token + "\",\"user\":" + user + "}}");
                    return;
                }
            } catch (NumberFormatException e) {
                userId = 0; // 如果用户名不是数字格式，使用0
            }
            
            // 记录登录成功日志
            systemLogService.recordLoginLog(userId, ipAddress, deviceInfo, 1); // 1表示成功
        } catch (Exception e) {
            // 记录日志失败不应影响登录流程
            e.printStackTrace();
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"500\",\"msg\":\"登录处理失败\"}");
        }
    }
} 
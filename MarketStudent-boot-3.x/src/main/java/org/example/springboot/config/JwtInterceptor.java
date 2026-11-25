package org.example.springboot.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class JwtInterceptor implements HandlerInterceptor {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandlerInterceptor.class);
    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401状态码
            response.getWriter().print("Token缺失"); // 返回错误信息
            return false;
        }

        try {
            // 解析token，获取用户ID
            DecodedJWT jwt = JWT.decode(token);
            String userId = jwt.getAudience().get(0);
            
            // 获取用户密码进行token验证
            // 注意：在生产环境中应该考虑使用缓存来存储密钥，而不是每次查询数据库
            User user = userService.getUserById(Integer.valueOf(userId));
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("User not found");
                return false;
            }
            
            // 验证token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token);
            
            // 将用户ID存储在请求属性中，后续处理器可以直接获取
            request.setAttribute("userId", userId);
            request.setAttribute("roleId", user.getRoleId());
            
            LOGGER.info("Token验证成功，用户ID: {}, 角色ID: {}", userId, user.getRoleId());
            return true;
        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("token认证失败，重新登录！");
            return false;
        } catch (Exception e) {
            String errMsg = "token处理异常，请重新登录！";
            LOGGER.error(errMsg + " ,token=" + token, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(errMsg);
            return false;
        }
    }
}

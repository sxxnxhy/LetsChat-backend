package com.application.letschat.config.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

// header로 토큰을 보내고싶은데 안됨 하 stomp는 커스터마이즈 header 보내기 불가능.

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        String token = null;
//        if (request instanceof ServletServerHttpRequest) {
//            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//            String query = servletRequest.getServletRequest().getQueryString();
//
//            if (query != null && query.contains("token=")) {
//                token = query.split("token=")[1].split("&")[0]; // Extract token from query string
//            }
//        }
//        if (token != null && jwtUtil.validateToken(token)) {
//            Integer userId = jwtUtil.getUserIdFromToken(token);
//            attributes.put("userId", userId);
//            return true;
//        }

        //쿠키

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            // 쿠키 가져오기
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("Authorization".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        System.out.println("Extracted Token from Cookie: " + token);

                        // 토큰 검증
                        if (jwtUtil.validateToken(token)) {
                            Integer userId = jwtUtil.getUserIdFromToken(token);
                            attributes.put("userId", userId);
                            System.out.println("Validated UserID: " + userId);
                            return true;
                        } else {
                            System.out.println("Token validation failed");
                        }
                    }
                }
            }
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
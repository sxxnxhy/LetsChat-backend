package com.application.letschat.config.jwt;

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
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        String authHeader = request.getHeaders().getFirst("Authorization");
//        System.out.println("WebSocket Auth Header: " + authHeader);
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            System.out.println("Extracted Token: " + token);
//            if (jwtUtil.validateToken(token)) {
//                Integer userId = jwtUtil.getUserIdFromToken(token);
//                attributes.put("userId", userId);
//                System.out.println("Validated UserID: " + userId);
//                return true;
//            } else {
//                System.out.println("Token validation failed");
//            }
//        } else {
//            System.out.println("No valid Authorization header found");
//        }
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        return false;
//    }

//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
//        if (request instanceof ServletServerHttpRequest) {
//            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//            HttpHeaders headers = servletRequest.getHeaders();
//            System.out.println("Headers: " + headers); // Should now include Authorization
//            String authHeader = headers.getFirst("Authorization");
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                if (jwtUtil.validateToken(token)) {
//                    Integer userId = jwtUtil.getUserIdFromToken(token);
//                    attributes.put("userId", userId);
//                    return true;
//                }
//            }
//        }
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        return false;
//    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String query = servletRequest.getServletRequest().getQueryString();

            if (query != null && query.contains("token=")) {
                token = query.split("token=")[1].split("&")[0]; // Extract token from query string
            }
        }
        if (token != null && jwtUtil.validateToken(token)) {
            Integer userId = jwtUtil.getUserIdFromToken(token);
            attributes.put("userId", userId);
            return true;
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
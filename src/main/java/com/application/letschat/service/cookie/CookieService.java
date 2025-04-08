package com.application.letschat.service.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public Cookie createCookie(String name, String content) {
        Cookie cookie = new Cookie(name, content);
        cookie.setHttpOnly(true);  // XSS 공격 방지
        cookie.setSecure(true);    // HTTPS에서만 전송
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(60 * 60 * 24); // 1일(86400초) 유지
        return cookie;
    }

}

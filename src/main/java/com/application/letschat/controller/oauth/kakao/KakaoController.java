package com.application.letschat.controller.oauth.kakao;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.kakao.KakaoInfoResponseDto;
import com.application.letschat.dto.user.SignUpRequestDto;
import com.application.letschat.entity.user.User;
import com.application.letschat.service.cookie.CookieService;
import com.application.letschat.service.oauth.kakao.KakaoService;
import com.application.letschat.service.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/kakao")
public class KakaoController {

    @Value("${kakao.client.id}")
    String clientId;
    @Value("${kakao.redirect.uri}")
    String redirectUri;
    @Value("${kakao.client.secret}")
    String clientSecret;
    @Value("${kakao.callback.redirect.uri}")
    String callbackRedirectUri;

    private final KakaoService kakaoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;


    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> kakaoConnect() {
        return ResponseEntity.ok(kakaoService.createLoginUrl());
    }

    @GetMapping("callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = kakaoService.getAccessToken(code);
        KakaoInfoResponseDto kakaoInfo = kakaoService.getKakaoInfo(accessToken);

        User user = userService.getUserByEmail(kakaoInfo.getEmail());
        if (user == null) {
            User createdUser = userService.createUser(SignUpRequestDto.builder()
                    .name(kakaoInfo.getNickname())
                    .email(kakaoInfo.getEmail())
                    .password(UUID.randomUUID().toString()).build());
            Cookie authCookie = cookieService.createCookie("Authorization", jwtUtil.generateToken(createdUser.getUserId()));
            response.addCookie(authCookie);
        } else {
            Cookie authCookie = cookieService.createCookie("Authorization", jwtUtil.generateToken(user.getUserId()));
            response.addCookie(authCookie);
        }

        Cookie kakaoCookie = cookieService.createCookie("kakaoToken", accessToken);
        response.addCookie(kakaoCookie);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(callbackRedirectUri))
                .build();
    }
}
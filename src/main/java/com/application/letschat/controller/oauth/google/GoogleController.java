package com.application.letschat.controller.oauth.google;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.google.GoogleInfoResponseDto;
import com.application.letschat.dto.oauth.LoginUrlResponseDto;
import com.application.letschat.dto.user.SignUpRequestDto;
import com.application.letschat.entity.user.User;
import com.application.letschat.service.oauth.google.GoogleService;
import com.application.letschat.service.cookie.CookieService;
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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/google")
public class GoogleController {

    @Value("${google.client.id}")
    String clientId;
    @Value("${google.redirect.uri}")
    String redirectUri;
    @Value("${google.client.secret}")
    String clientSecret;
    @Value("${google.callback.redirect.uri}")
    String callbackRedirectUri;

    private final GoogleService googleService;
    private final UserService userService;
    private final CookieService cookieService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public ResponseEntity<LoginUrlResponseDto> GoogleConnect() {
        return ResponseEntity.ok(googleService.createLoginUrl());
    }

    @GetMapping("callback")
    public ResponseEntity<Void> GoogleCallback(@RequestParam("code") String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = googleService.getAccessToken(code);
        GoogleInfoResponseDto googleInfo = googleService.getGoogleInfo(accessToken);

        User user = userService.getUserByEmail(googleInfo.getEmail());
        if (user == null) {
            User createdUser = userService.createUser(SignUpRequestDto.builder()
                    .name(googleInfo.getName())
                    .email(googleInfo.getEmail())
                    .password(UUID.randomUUID().toString()).build());
            Cookie authCookie = cookieService.createCookie("Authorization", jwtUtil.generateToken(createdUser.getUserId()));
            response.addCookie(authCookie);
        } else {
            Cookie authCookie = cookieService.createCookie("Authorization", jwtUtil.generateToken(user.getUserId()));
            response.addCookie(authCookie);
        }

        Cookie googleCookie = cookieService.createCookie("googleToken", accessToken);
        response.addCookie(googleCookie);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(callbackRedirectUri))
                .build();
    }

}

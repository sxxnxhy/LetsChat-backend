package com.application.letschat.controller.user;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.user.*;
import com.application.letschat.entity.user.User;
import com.application.letschat.service.oauth.google.GoogleService;
import com.application.letschat.service.cookie.CookieService;
import com.application.letschat.service.oauth.kakao.KakaoService;
import com.application.letschat.service.user.UserService;
import com.application.letschat.service.validation.ValidationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final KakaoService kakaoService;
    private final CookieService cookieService;
    private final GoogleService googleService;
    private final ValidationService validationService;


    @PostMapping("/login")
    public ResponseEntity<UserInfoDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        if (!validationService.validateEmail(loginRequestDto)) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.isAuthenticated(loginRequestDto)) {
            UserInfoDto userInfoDto = userService.getUserInfoByEmail(loginRequestDto.getEmail());

            Cookie cookie = cookieService.createCookieAndToken("Authorization", userInfoDto.getUserId()); // 쿠키 생성
            response.addCookie(cookie); // 응답에 쿠키 추가

            return ResponseEntity.ok(userInfoDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, HttpServletRequest request) {

        Cookie cookie = cookieService.createCookie("Authorization", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie[] cookies = request.getCookies();
        for (Cookie c : cookies) {
            if (c.getName().equals("googleToken")) {
                googleService.logout(c.getValue());
                Cookie googleCookie = cookieService.createCookie("googleToken", null);
                googleCookie.setMaxAge(0);
                response.addCookie(googleCookie);
            }
            else if (c.getName().equals("kakaoToken")) {
                kakaoService.logout(c.getValue());
                Cookie kakaoCookie = cookieService.createCookie("kakaoToken", null);
                kakaoCookie.setMaxAge(0);
                response.addCookie(kakaoCookie);
            }
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserInfoDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        if (!validationService.validateSignUpRequest(signUpRequestDto)) {
            return ResponseEntity.badRequest().body(null);
        }
        User user = userService.getUserByEmail(signUpRequestDto.getEmail());
        if (user != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else{
            User createdUser= userService.createUser(signUpRequestDto);
            UserInfoDto response = UserInfoDto.builder()
                    .userId(createdUser.getUserId())
                    .name(createdUser.getName())
                    .build();
            return ResponseEntity.ok(response);
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.length() > 255) {
            return ResponseEntity.badRequest().body(null);
        }
        List<User> users = userService.getUsersByKeyword(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id")
    public ResponseEntity<Map<String, String>> getUserId(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(Map.of("userId", customUserDetails.getUserId()));
    }

}

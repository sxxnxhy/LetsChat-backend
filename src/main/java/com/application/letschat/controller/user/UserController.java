package com.application.letschat.controller.user;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.model.user.User;
import com.application.letschat.service.OAuth.google.GoogleService;
import com.application.letschat.service.cookie.CookieService;
import com.application.letschat.service.OAuth.kakao.KakaoService;
import com.application.letschat.service.user.UserService;
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


    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        if (userDTO.getEmail() == null || userDTO.getEmail().length() > 255 ||
                !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.authenticate(userDTO)) {
            System.out.println("Login successful");
            User user = userService.getUserByEmail(userDTO.getEmail());
            String token = jwtUtil.generateToken(user.getUserId());
            UserDTO responseDTO = UserDTO.builder()
                    .name(user.getName())
                    .userId(user.getUserId())
                    .build();
            // 쿠키 생성 및 설정
            Cookie cookie = cookieService.createCookie("Authorization", token);
            // 응답에 쿠키 추가
            response.addCookie(cookie);
            return ResponseEntity.ok(responseDTO);
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
                Cookie kakaoCookie = cookieService.createCookie("kakaoToken", null);
                kakaoCookie.setMaxAge(0);
                response.addCookie(kakaoCookie);
            }
        }



        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> signUp(@RequestBody UserDTO userDTO) {
        if (userDTO.getEmail() == null || userDTO.getEmail().length() > 255 ||
                !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userDTO.getName() == null ||
                userDTO.getName().length() > 100 ||
                !userDTO.getName().matches("^[a-zA-Z가-힣\\-.'][a-zA-Z가-힣\\s\\-.']{0,99}$")) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().length() > 255) {
            return ResponseEntity.badRequest().body(null);
        }
        User user = userService.getUserByEmail(userDTO.getEmail());
        if (user != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else{
            User createdUser= userService.createUser(userDTO);
            UserDTO response = UserDTO.builder()
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

    @GetMapping("/get-user-id")
    public ResponseEntity<Map<String, String>> getUserId(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(Map.of("userId", customUserDetails.getUserId()));
    }

}

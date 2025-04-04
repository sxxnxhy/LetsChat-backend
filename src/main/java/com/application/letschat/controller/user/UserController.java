package com.application.letschat.controller.user;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.model.user.User;
import com.application.letschat.service.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


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
            Cookie cookie = new Cookie("Authorization", token);
            cookie.setHttpOnly(true);  // XSS 공격 방지
            cookie.setSecure(true);    // HTTPS에서만 전송
            cookie.setPath("/");       // 모든 경로에서 접근 가능
            cookie.setMaxAge(60 * 60 * 24); // 1일(86400초) 유지

            // 응답에 쿠키 추가
            response.addCookie(cookie);

            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // Secure 설정 시 클라이언트에서 삭제 불가
        cookie.setSecure(true);  //https 에서만 가능
        cookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> signUp(@RequestBody UserDTO userDTO) {
        if (userDTO.getEmail() == null || userDTO.getEmail().length() > 255 ||
                !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userDTO.getName() == null || userDTO.getName().length() > 100) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().length() > 255) {
            return ResponseEntity.badRequest().body(null);
        }
        User user = userService.getUserByEmail(userDTO.getEmail());
        if (user != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else{
            User users= userService.createUser(userDTO);
            UserDTO responseDTO = UserDTO.builder()
                    .userId(users.getUserId())
                    .name(users.getName())
                    .build();
            return ResponseEntity.ok(responseDTO);
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

}

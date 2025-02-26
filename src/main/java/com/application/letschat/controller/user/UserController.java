package com.application.letschat.controller.user;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.model.user.User;
import com.application.letschat.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO) {
        if (userService.authenticate(userDTO)) {
            User user = userService.getUserByName(userDTO.getName());
            String token = jwtUtil.generateToken(user.getUserId());
            UserDTO responseDTO = UserDTO.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .token(token)
                    .build();
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> signUp(@RequestBody UserDTO userDTO) {
        User user = userService.getUserByName(userDTO.getName());
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
        List<User> users = userService.getUsersByKeyword(keyword);
        return ResponseEntity.ok(users);
    }

}

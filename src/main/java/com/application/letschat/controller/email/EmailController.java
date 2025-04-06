package com.application.letschat.controller.email;

import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.model.user.User;
import com.application.letschat.service.email.EmailService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final RedisService redisService;
    private final UserService userService;


    @PostMapping("/verification/send")
    public ResponseEntity<Void> sendVerificationEmail(@RequestParam("email") String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/verification/verify")
    public ResponseEntity<Void> verifyCode(@RequestBody EmailVerificationRequestDto emailVerificationRequestDto) {
        if (emailService.verifyCode(emailVerificationRequestDto)) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

}

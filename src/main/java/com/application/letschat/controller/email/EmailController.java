package com.application.letschat.controller.email;

import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.model.user.User;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.email.EmailService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final RedisService redisService;
    private final UserService userService;
    private final ChatRoomUserService chatRoomUserService;


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

    @PostMapping("/notification/send")
    public ResponseEntity<Void> sendNotificationEmail(@RequestParam("chatRoomId") Long chatRoomId,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (!chatRoomUserService.isUserInChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> emails = chatRoomUserService.getEmailsByChatRoomId(chatRoomId)
                .stream()
                .filter(email -> !email.equals(customUserDetails.getEmail()))
                .toList();
        emailService.sendNotificationEmail(emails, customUserDetails.getUsername());
        return ResponseEntity.ok().build();
    }

}

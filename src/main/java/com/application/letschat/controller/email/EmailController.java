package com.application.letschat.controller.email;

import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.entity.user.User;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.email.EmailService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
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
        emailService.sendNotificationEmail(chatRoomId, customUserDetails.getUsername(), customUserDetails.getEmail());
        return ResponseEntity.ok().build();
    }

}

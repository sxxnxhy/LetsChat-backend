package com.application.letschat.controller.email;

import com.application.letschat.dto.email.EmailSendRequestDto;
import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.email.EmailService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import com.application.letschat.service.validation.ValidationService;
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
    private final RedisService redisService;
    private final ValidationService validationService;


    @PostMapping("/verification/send")
    public ResponseEntity<Void> sendVerificationEmail(@RequestBody EmailVerificationRequestDto dto) {
        String email = dto.getEmail();
        if (email == null || !validationService.isValidEmail(email)) {
            return ResponseEntity.badRequest().build();
        }
        if (userService.getUserByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!redisService.isAllowedToRequestVerification(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> sendNotificationEmail(@RequestBody EmailSendRequestDto emailSendRequestDto,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (!chatRoomUserService.isUserInChat(emailSendRequestDto.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!redisService.isAllowedToSendNotificationEmail(emailSendRequestDto.getChatRoomId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        emailService.sendNotificationEmail(emailSendRequestDto.getChatRoomId(), customUserDetails.getUsername(), customUserDetails.getEmail());
        return ResponseEntity.ok().build();
    }

}

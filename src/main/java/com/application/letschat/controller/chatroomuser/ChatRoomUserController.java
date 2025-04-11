package com.application.letschat.controller.chatroomuser;

import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room-user")
@Slf4j
public class ChatRoomUserController {

    private final ChatRoomUserService chatRoomUserService;
    private final MessageService messageService;
    private final RedisService redisService;

    @DeleteMapping("leave-chat")
    public ResponseEntity<Void> leaveChat(@RequestParam("chatRoomId") Long chatRoomId,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("{} is not in chat room no.{}", customUserDetails.getUsername(), chatRoomId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        chatRoomUserService.removeUserFromChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()));
        messageService.sendSystemMessageForLeave(chatRoomId, customUserDetails.getUsername());
        return ResponseEntity.ok().build();
    }

}

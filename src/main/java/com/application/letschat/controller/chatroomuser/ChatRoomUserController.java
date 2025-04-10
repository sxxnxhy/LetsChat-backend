package com.application.letschat.controller.chatroomuser;

import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
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

    @DeleteMapping("leave-chat")
    public ResponseEntity<Void> leaveChat(@RequestParam("chatRoomId") Long chatRoomId,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        chatRoomUserService.removeUserFromChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()));
        return ResponseEntity.ok().build();
    }

}

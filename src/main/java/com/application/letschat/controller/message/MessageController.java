package com.application.letschat.controller.message;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatroomuser.ChatRoomUserDto;
import com.application.letschat.dto.message.MessageDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatroom.ChatRoomService;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final ChatRoomUserService chatRoomUserService;


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDto messageDTO,
                                   @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (messageDTO.getContent() == null || messageDTO.getContent().length() > 3000) {
            return;
        }
        if (!chatRoomUserService.isUserInChat(messageDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            return;
        }
        messageDTO.setSenderName(customUserDetails.getUsername());
        messageDTO.setEnrolledAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + messageDTO.getChatRoomId(), messageDTO);
    }

    @MessageMapping("/user-active")
    public void handleUserActive(@Payload ChatRoomUserDto chatRoomUserDTO) {
        chatRoomUserDTO.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDTO);
    }

    @MessageMapping("/user-inactive")
    public void handleUserInactive(@Payload ChatRoomUserDto chatRoomUserDTO) {
        chatRoomUserDTO.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDTO);
    }

}
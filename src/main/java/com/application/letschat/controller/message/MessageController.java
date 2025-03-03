package com.application.letschat.controller.message;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDTO messageDTO,
                                   Principal principal) throws Exception {
        messageDTO.setSenderName(principal.getName());
        messageDTO.setEnrolledAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + messageDTO.getChatRoomId(), messageDTO);

    }

    @MessageMapping("/user-active")
    public void handleUserActive(@Payload ChatRoomUserDTO chatRoomUserDTO) {
        chatRoomUserDTO.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDTO);
    }

    @MessageMapping("/user-inactive")
    public void handleUserInactive(@Payload ChatRoomUserDTO chatRoomUserDTO) {
        chatRoomUserDTO.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDTO);
    }



}
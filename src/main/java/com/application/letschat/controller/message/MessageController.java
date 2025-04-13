package com.application.letschat.controller.message;

import com.application.letschat.dto.chatroomuser.ChatRoomUserDto;
import com.application.letschat.dto.message.MessageDto;
import com.application.letschat.dto.user.UserInfoDto;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatRoomUserService chatRoomUserService;
    private final UserService userService;


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDto messageDto,
                                   Principal principal) throws Exception {
        if (messageDto.getContent() == null || messageDto.getContent().length() > 3000) {
            return;
        }
        UserInfoDto userInfo = userService.extractUserInfoFromSpringSecurity(principal);
        if (!chatRoomUserService.isUserInChat(messageDto.getChatRoomId(), userInfo.getUserId())) {
            return;
        }
        messageDto.setSenderName(userInfo.getName());
        messageDto.setEnrolledAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingMessage(messageDto);
        messagingTemplate.convertAndSend("/topic/private-chat/" + messageDto.getChatRoomId(), messageDto);
    }

    @MessageMapping("/user-active")
    public void handleUserActive(@Payload ChatRoomUserDto chatRoomUserDto) {
        chatRoomUserDto.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDto);
    }

    @MessageMapping("/user-inactive")
    public void handleUserInactive(@Payload ChatRoomUserDto chatRoomUserDto) {
        chatRoomUserDto.setLastReadAt(Timestamp.valueOf(LocalDateTime.now()));
        redisService.addPendingLastReadAt(chatRoomUserDto);
    }

}
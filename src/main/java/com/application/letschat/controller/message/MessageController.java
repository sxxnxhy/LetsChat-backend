package com.application.letschat.controller.message;

import com.application.letschat.dto.chatroomuser.ChatRoomUserDto;
import com.application.letschat.dto.message.MessageDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDto messageDto,
                                   Principal principal) throws Exception {
        if (messageDto.getContent() == null || messageDto.getContent().length() > 3000) {
            return;
        }

//        //자바 17이상부터는 instanceof로 대체 가능
//        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
//        CustomUserDetails userDetails = (CustomUserDetails) authToken.getPrincipal();
//        String username = userDetails.getUsername();
//        Integer userId = Integer.parseInt(userDetails.getUserId());

        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            Object principalObj = authToken.getPrincipal();
            if (principalObj instanceof CustomUserDetails userDetails) {
                String username = userDetails.getUsername();
                Integer userId = Integer.parseInt(userDetails.getUserId());
                if (!chatRoomUserService.isUserInChat(messageDto.getChatRoomId(), userId)) {
                    return;
                }
                messageDto.setSenderName(username);
                messageDto.setEnrolledAt(Timestamp.valueOf(LocalDateTime.now()));
                redisService.addPendingMessage(messageDto);
                messagingTemplate.convertAndSend("/topic/private-chat/" + messageDto.getChatRoomId(), messageDto);
            }
        }
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
package com.application.letschat.controller.message;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoom.ChatRoomRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.repository.user.UserRepository;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatRoomService chatRoomService;

    private final MessageService messageService;

    private final UserService userService;

    private final JwtUtil jwtUtil;


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDTO messageDTO){

        Long chatRoomId = messageDTO.getChatRoomId();

        User user = userService.getUserById(messageDTO.getSenderId());

        Message message = new Message();
        message.setUser(user);
        message.setContent(messageDTO.getContent());

        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);
        message.setChatRoom(chatRoom);

        Message savedMessage = messageService.saveMessage(message);

        messageDTO.setSenderName(user.getName());
        messageDTO.setEnrolledAt(savedMessage.getEnrolledAt());

        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomId, messageDTO);

    }

    @MessageMapping("/authenticate")
    @SendToUser("/queue/auth")
    public String authenticate(@Header("Authorization") String authHeader,
                             @Header("simpSessionId") String sessionId) {
        String result = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                Integer userId = jwtUtil.getUserIdFromToken(token);
                System.out.println("Authenticated user " + userId + " for session " + sessionId);
                result = "Authentication successful";
            } else {
                result = "Invalid token";
            }
        } else {
            result = "Missing token";
        }
        return result;
    }

}
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

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomService chatRoomService;

    private final JwtUtil jwtUtil;

    @MessageMapping("all-chat")
    public void sendAllChat(@Payload MessageDTO messageDTO) {

        Message message = new Message();



    }

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload MessageDTO messageDTO){

        Long chatRoomId = messageDTO.getChatRoomId();

        Message message = new Message();
        User user = userRepository.findById(messageDTO.getSenderId()).orElseThrow();
        message.setUser(user);
        message.setContent(messageDTO.getContent());

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        message.setChatRoom(chatRoom);

        Message savedMessage = messageRepository.save(message);

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
package com.application.letschat.controller.message;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;


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

        Message savedMessage = messageService.saveMessage(messageDTO);

        messageDTO.setSenderName(savedMessage.getUser().getName());
        messageDTO.setEnrolledAt(savedMessage.getEnrolledAt());

        messagingTemplate.convertAndSend("/topic/private-chat/" + messageDTO.getChatRoomId(), messageDTO);

    }

// //토큰 검사. (이제 할 필요 없음. 웹소켓을 connect할 때 이미 인터셉터에 걸려서 쿠기에 유효한 토큰이 들어있는지 확인함.
//    @MessageMapping("/authenticate")
//    @SendToUser("/queue/auth")
//    public String authenticate(@Header("Authorization") String authHeader,
//                             @Header("simpSessionId") String sessionId) {
//        String result = "";
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            if (jwtUtil.validateToken(token)) {
//                Integer userId = jwtUtil.getUserIdFromToken(token);
//                System.out.println("Authenticated user " + userId + " for session " + sessionId);
//                result = "Authentication successful";
//            } else {
//                result = "Invalid token";
//            }
//        } else {
//            result = "Missing token";
//        }
//        return result;
//    }

}
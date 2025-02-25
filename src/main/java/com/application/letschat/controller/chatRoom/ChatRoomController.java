package com.application.letschat.controller.chatRoom;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.repository.chatRoom.ChatRoomRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.service.chatRoom.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    private final JwtUtil jwtUtil;

    private final ChatRoomRepository chatRoomRepository;

    private final MessageRepository messageRepository;



    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createChatRoom(@RequestBody ChatRoomCreateDTO chatRoomCreateDTO) {
        Integer userId = chatRoomCreateDTO.getUserId();
        Integer targetUserId = chatRoomCreateDTO.getTargetUserId();
        String targetUserName = chatRoomCreateDTO.getTargetUserName();

        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setChatRoomName(targetUserName);

        Long chatRoomId = chatRoomService.createChatRoom(userId, chatRoomDTO, targetUserId);


        Map<String, Long> response = new HashMap<>();
        response.put("chatRoomId", chatRoomId);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/check-access")
//    public ResponseEntity<Boolean> checkAccess(@RequestParam("chatRoomId") Long chatRoomId,
//                                               @RequestHeader("Authorization") String authorizationHeader) {
//        String token = authorizationHeader.replace("Bearer ", "");
//        System.out.println(chatRoomId);
//        System.out.println(token);
//
//        boolean isValid = chatRoomService.checkAccess(token, chatRoomId);
//        System.out.println(isValid);
//        return ResponseEntity.ok(isValid);
//    }

    @GetMapping("/chat-history")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestParam("chatRoomId") Long chatRoomId,
                                                           @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");
        boolean isValid = chatRoomService.checkAccess(token, chatRoomId);  //유저가 채팅방에 존재하는지 확인.

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        } else {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new RuntimeException("Chat room not found"));
            List<Message> messages = messageRepository.findByChatRoom(chatRoom);
            List<Map<String, Object>> messageDtos = messages.stream()
                    .map(m -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("chatRoomId", m.getChatRoom().getChatRoomId());
                        map.put("senderId", m.getUser().getUserId());
                        map.put("senderName", m.getUser().getName());
                        map.put("content", m.getContent());
                        map.put("enrolledAt", m.getEnrolledAt());
                        return map;
                    })
                    .toList();
            Map<String, Object> response = Map.of(
                    "chatRoomName", chatRoom.getChatRoomName(),
                    "messages", messageDtos
            );
            return ResponseEntity.ok(response);
        }
    }
}
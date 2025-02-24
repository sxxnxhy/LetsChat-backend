package com.application.letschat.controller.message;

import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoom.ChatRoomRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/api/chat/messages")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestParam("chatRoomId") Long chatRoomId) {
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
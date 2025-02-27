package com.application.letschat.controller.chatRoom;

import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat-room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    private final MessageService messageService;

    private final ChatRoomUserService chatRoomUserService;


    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createChatRoom(@RequestBody ChatRoomCreateDTO chatRoomCreateDTO,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long chatRoomId = chatRoomService.createChatRoom(chatRoomCreateDTO, Integer.parseInt(userDetails.getUserId()));

        Map<String, Long> response = new HashMap<>();
        response.put("chatRoomId", chatRoomId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/chat-history")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestParam("chatRoomId") Long chatRoomId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        }

        Integer userId = Integer.parseInt(userDetails.getUserId());
        boolean isValid = chatRoomUserService.isUserInChat(chatRoomId, userId);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        } else {
            ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);
            List<MessageDTO> messageDTOs = messageService.getMessageDTOs(chatRoom);
            Map<String, Object> response = Map.of(
                    "chatRoomName", chatRoom.getChatRoomName(),
                    "messages", messageDTOs
            );
            return ResponseEntity.ok(response);
        }
    }
}
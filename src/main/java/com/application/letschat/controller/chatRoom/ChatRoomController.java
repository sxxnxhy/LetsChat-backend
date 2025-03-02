package com.application.letschat.controller.chatRoom;

import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.user.User;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat-room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    private final MessageService messageService;

    private final ChatRoomUserService chatRoomUserService;

    private final RedisService redisService;

    private final SimpMessagingTemplate messagingTemplate;

    private final UserService userService;


    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createChatRoom(@RequestBody ChatRoomCreateDTO chatRoomCreateDTO,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long chatRoomId = chatRoomService.createChatRoom(chatRoomCreateDTO, Integer.parseInt(userDetails.getUserId()));

        //레디스에 등록
        redisService.addChatRoomIdsAndUserIds(Integer.parseInt(userDetails.getUserId()), chatRoomId);
        redisService.addChatRoomIdsAndUserIds(chatRoomCreateDTO.getTargetUserId(), chatRoomId);

        Map<String, Long> response = new HashMap<>();
        response.put("chatRoomId", chatRoomId);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/chat-history")
    public ResponseEntity<Map<String, Object>> getMessages(@RequestParam("chatRoomId") Long chatRoomId,
                                                           @RequestParam(value = "page" , defaultValue = "0") int page,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        //레디스 싱크
        messageService.syncMessagesByChatRoomId(chatRoomId);

        int size = 20;
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        }

        Integer userId = Integer.parseInt(userDetails.getUserId());
        boolean isValid = chatRoomUserService.isUserInChat(chatRoomId, userId);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        } else {
            ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "enrolledAt"));
            Page<MessageDTO> messagePage = messageService.getMessageDTOs(chatRoom, pageable);
            messagePage.getTotalPages();
            List<MessageDTO> reversedMessages = new ArrayList<>(messagePage.getContent());
            Collections.reverse(reversedMessages);

            List<Integer> usersInChatRoom = redisService.getUserIdsByChatRoomId(chatRoomId);
            List<User> users = userService.getUsersById(usersInChatRoom); // Fetch users from DB
            List<Map<String, Object>> userList = users.stream()
                    .map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("userId", user.getUserId());
                        map.put("userName", user.getName());
                        return map;
                    })
                    .toList();

            Map<String, Object> response = Map.of(
                    "chatRoomName", chatRoom.getChatRoomName(),
                    "messages", reversedMessages,
                    "totalPages", messagePage.getTotalPages(),
                    "users", userList
            );

            ChatRoomUserDTO chatRoomUserDto =ChatRoomUserDTO.builder()
                    .chatRoomId(chatRoomId)
                    .userId(userId)
                    .lastReadAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            redisService.addPendingLastReadAt(chatRoomUserDto);

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/update-subject")
    public void updateSubject(@RequestBody ChatRoomDTO chatRoomDTO) {
        chatRoomService.updateSubject(chatRoomDTO);
        chatRoomDTO.setSenderId(0);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomDTO.getChatRoomId(), chatRoomDTO);

    }

}
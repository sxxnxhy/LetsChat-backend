package com.application.letschat.controller.chatRoom;

import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.user.User;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.mail.MailService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
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

    private final MailService mailService;


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
            ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "enrolledAt"));
            Page<MessageDTO> messagePage = messageService.getMessageDTOs(chatRoom, pageable);
            List<MessageDTO> reversedMessages = new ArrayList<>(messagePage.getContent());
            Collections.reverse(reversedMessages);

            List<Integer> usersInChatRoom = redisService.getUserIdsByChatRoomId(chatRoomId);
            List<User> users = userService.getUsersById(usersInChatRoom); // Fetch users from DB
            System.out.println(users);
            List<Map<String, Object>> userList = users.stream()
                    .map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("userId", user.getUserId());
                        map.put("name", user.getName());
                        map.put("email", user.getEmail());
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
    public void updateSubject(@RequestBody ChatRoomDTO chatRoomDTO, Principal principal, @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (chatRoomDTO.getChatRoomName() == null || chatRoomDTO.getChatRoomName().length() > 100) {
            return;
        }

        if (!chatRoomUserService.isUserInChat(chatRoomDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return;
        }

        chatRoomService.updateSubject(chatRoomDTO);

        //system message
        MessageDTO messageDTO = MessageDTO.builder()
                .content(String.format("Chat name updated to \"%s\" by \"%s\"", chatRoomDTO.getChatRoomName(), principal.getName()))
                .senderName(chatRoomDTO.getChatRoomName())
                .chatRoomId(chatRoomDTO.getChatRoomId())
                .enrolledAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomDTO.getChatRoomId(), messageDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam("keyword") String keyword,
                                                           @RequestParam("chatRoomId") Long chatRoomId) {
        if (keyword == null || keyword.length() > 255) {
            return ResponseEntity.badRequest().body(null);
        }
        List<User> users = userService.getUsersByKeyword(keyword);
        List<Integer> chatRoomUserIds = redisService.getUserIdsByChatRoomId(chatRoomId);

        Map<String, Object> response = new HashMap<>();
        response.put("allUsers", users);
        response.put("chatRoomUsers", chatRoomUserIds);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-user")
    public ResponseEntity<Map<String, String>> addUserToChatRoom(@RequestBody ChatRoomUserDTO chatRoomUserDTO, Principal principal,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomUserDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of());
        }

        User user = userService.getUserById(chatRoomUserDTO.getUserId());
        chatRoomUserService.addUserToChatRoom(user, chatRoomService.getChatRoomById(chatRoomUserDTO.getChatRoomId()));
        redisService.addChatRoomIdsAndUserIds(chatRoomUserDTO.getUserId(), chatRoomUserDTO.getChatRoomId());


        MessageDTO messageDTO = MessageDTO.builder()
                .content(String.format("\"%s\" added by \"%s\"", user.getName(), principal.getName()))
                .chatRoomId(chatRoomUserDTO.getChatRoomId())
                .enrolledAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomUserDTO.getChatRoomId(), messageDTO);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-list")
    public ResponseEntity<Map<String, Object>> getUserList(@RequestParam Long chatRoomId) {
        List<UserDTO> users = chatRoomUserService.getUsersInChatRoom(chatRoomId);
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-email-notification")
    public void sendEmailNotification(@RequestParam("chatRoomId") Long chatRoomId) {
        List<String> emails = chatRoomUserService.getEmailsByChatRoomId(chatRoomId);
        for (String email : emails) {
            mailService.sendMail(email);
        }
    }


}
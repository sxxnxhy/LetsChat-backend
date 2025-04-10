package com.application.letschat.controller.chatroom;

import com.application.letschat.dto.StatusResponseDto;
import com.application.letschat.dto.chatroom.ChatRoomCreateDto;
import com.application.letschat.dto.chatroom.ChatRoomDto;
import com.application.letschat.dto.chatroom.ChatRoomResponseDto;
import com.application.letschat.dto.chatroomuser.ChatRoomUserDto;
import com.application.letschat.dto.message.MessageDto;
import com.application.letschat.dto.user.*;
import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.user.User;
import com.application.letschat.service.chatroom.ChatRoomService;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    @PostMapping("/create")
    public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody ChatRoomCreateDto chatRoomCreateDTO,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long chatRoomId = chatRoomService.createChatRoom(chatRoomCreateDTO, Integer.parseInt(userDetails.getUserId()));

        //레디스에 등록
        redisService.addChatRoomIdsAndUserIds(Integer.parseInt(userDetails.getUserId()), chatRoomId);
        redisService.addChatRoomIdsAndUserIds(chatRoomCreateDTO.getTargetUserId(), chatRoomId);

        return ResponseEntity.ok(ChatRoomDto.builder().chatRoomId(chatRoomId).build());
    }



    @GetMapping("/chat-history")
    public ResponseEntity<ChatRoomResponseDto> getMessages(@RequestParam("chatRoomId") Long chatRoomId,
                                                           @RequestParam(value = "page" , defaultValue = "0") int page,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Integer userId = Integer.parseInt(userDetails.getUserId());

        if (!chatRoomUserService.isUserInChat(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else {
            messageService.syncMessagesByChatRoomId(chatRoomId); //레디스 싱크
            ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);
            Page<MessageDto> messagePage = messageService.getMessagePage(chatRoom, page);
            List<MessageDto> reversedMessages = messageService.getReversedMessages(chatRoom, page);
            List<UserInfoDto> userList = chatRoomService.getUsersInChatRoom(chatRoomId);
            ChatRoomResponseDto response = new ChatRoomResponseDto(
                    chatRoom.getChatRoomName(),
                    reversedMessages,
                    messagePage.getTotalPages(),
                    userList
            );
            chatRoomService.updateLastReadAt(chatRoomId, userId);

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/update-subject")
    public ResponseEntity<Void> updateSubject(@RequestBody ChatRoomDto chatRoomDTO, Principal principal, @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (chatRoomDTO.getChatRoomName() == null || chatRoomDTO.getChatRoomName().length() > 100) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!chatRoomUserService.isUserInChat(chatRoomDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        chatRoomService.updateSubject(chatRoomDTO);

        //system message
        MessageDto messageDTO = MessageDto.builder()
                .content(String.format("\"%s\"님이 채팅 이름을 \"%s\" (으)로 변경하였습니다", principal.getName(), chatRoomDTO.getChatRoomName()))
                .senderName(chatRoomDTO.getChatRoomName())
                .chatRoomId(chatRoomDTO.getChatRoomId())
                .enrolledAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        redisService.addPendingMessage(messageDTO);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomDTO.getChatRoomId(), messageDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<UserSearchResponseDto> searchUsers(@RequestParam("keyword") String keyword,
                                                           @RequestParam("chatRoomId") Long chatRoomId) {
        if (keyword == null || keyword.length() > 255) {
            return ResponseEntity.badRequest().body(null);
        }
        List<User> users = userService.getUsersByKeyword(keyword);
        List<Integer> chatRoomUserIds = redisService.getUserIdsByChatRoomId(chatRoomId);
        return ResponseEntity.ok(new UserSearchResponseDto(users, chatRoomUserIds));
    }

    @PostMapping("/add-user")
    public ResponseEntity<StatusResponseDto> addUserToChatRoom(@RequestBody ChatRoomUserDto chatRoomUserDTO, Principal principal,
                                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomUserDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(StatusResponseDto.builder().status("forbidden").build());
        }

        User user = userService.getUserById(chatRoomUserDTO.getUserId());
        chatRoomUserService.addUserToChatRoom(user, chatRoomService.getChatRoomById(chatRoomUserDTO.getChatRoomId()));
        redisService.addChatRoomIdsAndUserIds(chatRoomUserDTO.getUserId(), chatRoomUserDTO.getChatRoomId());

        MessageDto systemMessage = MessageDto.builder()
                .content(String.format("\"%s\" 님이 \"%s\" 님을 추가했습니다", principal.getName(), user.getName()))
                .chatRoomId(chatRoomUserDTO.getChatRoomId())
                .enrolledAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        redisService.addPendingMessage(systemMessage);
        messagingTemplate.convertAndSend("/topic/private-chat/" + chatRoomUserDTO.getChatRoomId(), systemMessage);
        return ResponseEntity.ok(StatusResponseDto.builder().status("success").build());
    }

    @GetMapping("/user-list")
    public ResponseEntity<UserListResponseDto> getUserList(@RequestParam Long chatRoomId) {
        return ResponseEntity.ok(UserListResponseDto.builder().users(chatRoomUserService.getUsersInChatRoom(chatRoomId)).build());
    }

}
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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

            chatRoomService.updateLastReadAt(chatRoomId, userId);

            ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);
            Page<MessageDto> messagePage = messageService.getMessagePage(chatRoom, page);
            List<MessageDto> reversedMessages = messageService.getReversedMessages(messagePage);
            List<UserInfoDto> userList = chatRoomService.getUsersInChatRoom(chatRoomId);

            return ResponseEntity.ok(ChatRoomResponseDto.builder()
                    .chatRoomName(chatRoom.getChatRoomName())
                    .totalPages(messagePage.getTotalPages())
                    .messages(reversedMessages)
                    .users(userList).build());
        }
    }

    @PostMapping("/update-subject")
    public ResponseEntity<Void> updateSubject(@RequestBody ChatRoomDto chatRoomDTO, @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (chatRoomDTO.getChatRoomName() == null || chatRoomDTO.getChatRoomName().length() > 100) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!chatRoomUserService.isUserInChat(chatRoomDTO.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        chatRoomService.updateSubject(chatRoomDTO, customUserDetails.getUsername());
        messageService.sendSystemMessageForUpdateSubject(customUserDetails.getUsername(), chatRoomDTO);
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
    public ResponseEntity<StatusResponseDto> addUserToChatRoom(@RequestBody ChatRoomUserDto chatRoomUserDto,
                                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomUserDto.getChatRoomId(), Integer.parseInt(customUserDetails.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(StatusResponseDto.builder().status("forbidden").build());
        }

        User user = userService.getUserById(chatRoomUserDto.getUserId());
        chatRoomUserService.addUserToChatRoom(user, chatRoomService.getChatRoomById(chatRoomUserDto.getChatRoomId()));
        redisService.addChatRoomIdsAndUserIds(chatRoomUserDto.getUserId(), chatRoomUserDto.getChatRoomId());

        messageService.sendSystemMessageForAddUser(customUserDetails.getUsername(), user.getName(), chatRoomUserDto, user.getEmail());
        return ResponseEntity.ok(StatusResponseDto.builder().status("success").build());
    }

    @GetMapping("/user-list")
    public ResponseEntity<UserListResponseDto> getUserList(@RequestParam Long chatRoomId) {
        UserListResponseDto users = UserListResponseDto.builder().users(chatRoomUserService.getUsersInChatRoom(chatRoomId)).build();
        return ResponseEntity.ok(users);
    }

}
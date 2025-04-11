package com.application.letschat.controller.chatlist;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatlist.ChatListDto;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.dto.user.UserInfoDto;
import com.application.letschat.service.chatlist.ChatListService;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat-list")
public class ChatListController {
    private final ChatListService chatListService;
    private final JwtUtil jwtUtil;
    private final ChatRoomUserService chatRoomUserService;
    private final MessageService messageService;

    @GetMapping("/chats")
    public ResponseEntity<List<ChatListDto>> getChatList(HttpServletRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Integer userId = Integer.parseInt(customUserDetails.getUserId());
        chatRoomUserService.updateLastReadAt(userId);
        messageService.syncMessagesByUserId(userId); //해당 유저의 채팅방들만 싱크
        return ResponseEntity.ok(chatListService.getChatList(userId));
    }

    @GetMapping("/name-and-email")
    public ResponseEntity<UserInfoDto> getName(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(UserInfoDto.builder()
                .name(customUserDetails.getUsername())
                .email(customUserDetails.getEmail())
                .build());
    }


}

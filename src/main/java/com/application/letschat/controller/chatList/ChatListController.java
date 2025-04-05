package com.application.letschat.controller.chatList;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatList.ChatListDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.dto.user.UserDTO;
import com.application.letschat.service.chatList.ChatListService;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat-list")
public class ChatListController {


    private final ChatListService chatListService;

    private final JwtUtil jwtUtil;

    private final ChatRoomUserService chatRoomUserService;

    private final MessageService messageService;

    @GetMapping("/chats")
    public ResponseEntity<List<ChatListDTO>> getChatList(HttpServletRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        //CustomUserDetails 사용
        Integer userId = Integer.parseInt(userDetails.getUserId());
        chatRoomUserService.updateLastReadAt(userId);

        //해당 유저의 채팅방들만 싱크
        messageService.syncMessagesByUserId(userId);

        List<ChatListDTO> chats = chatListService.getChatList(userId);
        return ResponseEntity.ok(chats);
    }
    @GetMapping("/get-name")
    public ResponseEntity<Map<String, String>> getName(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(Map.of("name", customUserDetails.getUsername()));
        //자원 + 동사 + Request + dto
        // PostCreateRequestDto
        // PostCreateResponse
        // UserResponseDto
    }


}

package com.application.letschat.controller.chatList;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatList.ChatListDTO;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatList.ChatListService;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import com.application.letschat.service.message.MessageService;
import com.application.letschat.service.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<ChatListDTO>> getChatList(HttpServletRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        //CustomUserDetails 사용
        Integer userId = Integer.parseInt(userDetails.getUserId());
        chatRoomUserService.updateLastReadAt(userId);

        //비효율적
//        messageService.syncAllMessages();

        messageService.syncMessagesByUserId(userId);


        List<ChatListDTO> chats = chatListService.getChatList(userId);
        System.out.println(chats);
        return ResponseEntity.ok(chats);
    }


}

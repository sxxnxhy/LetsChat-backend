package com.application.letschat.controller.chatList;


import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatList.ChatListDTO;
import com.application.letschat.service.chatList.ChatListService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/chats")
    public ResponseEntity<List<ChatListDTO>> getChatList(HttpServletRequest request) {

        //쿠키 불러와서 토큰 꺼내고 거기서 userId 추출
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        Integer userId = jwtUtil.getUserIdFromToken(token);

        List<ChatListDTO> chats = chatListService.getChatList(userId);
        return ResponseEntity.ok(chats);
    }


}

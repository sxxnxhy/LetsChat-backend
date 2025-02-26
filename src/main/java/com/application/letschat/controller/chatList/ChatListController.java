package com.application.letschat.controller.chatList;


import com.application.letschat.dto.chatList.ChatListDTO;
import com.application.letschat.service.chatList.ChatListService;
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

    @GetMapping("/chats")
    public ResponseEntity<List<ChatListDTO>> getChatList(@RequestParam("userId") Integer userId) {

        List<ChatListDTO> chats = chatListService.getChatList(userId);
        return ResponseEntity.ok(chats);
    }


}

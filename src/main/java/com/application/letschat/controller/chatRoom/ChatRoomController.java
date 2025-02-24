package com.application.letschat.controller.chatRoom;

import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.service.chatRoom.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/api/create-chat-room")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> createChatRoom(@RequestBody ChatRoomCreateDTO chatRoomCreateDTO) {
        Integer userId = chatRoomCreateDTO.getUserId();
        Integer targetUserId = chatRoomCreateDTO.getTargetUserId();
        String targetUserName = chatRoomCreateDTO.getTargetUserName();

        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setChatRoomName(targetUserName);

        Long chatRoomId = chatRoomService.createChatRoom(userId, chatRoomDTO, targetUserId);


        Map<String, Long> response = new HashMap<>();
        response.put("chatRoomId", chatRoomId);
        return ResponseEntity.ok(response);
    }

}
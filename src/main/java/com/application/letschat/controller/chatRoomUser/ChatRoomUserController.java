package com.application.letschat.controller.chatRoomUser;

import com.application.letschat.config.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room-user")
public class ChatRoomUserController {

//    private final JwtUtil jwtUtil;
//
//    @GetMapping("/token-for-ws")
//    public ResponseEntity<Map<String, String>> getTokenForWebSocket(@RequestHeader("Authorization") String authorizationHeader) {
//        Integer userId =  jwtUtil.getUserIdFromToken(authorizationHeader.replace("Bearer ", ""));
//        Map<String, String> response = Map.of("tfws",jwtUtil.generateTokenForWebSocket(userId));
//        return ResponseEntity.ok(response);
//
//    }




}

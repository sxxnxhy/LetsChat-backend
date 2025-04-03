package com.application.letschat.controller.chatRoomUser;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.user.CustomUserDetails;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room-user")
@Slf4j
public class ChatRoomUserController {

    private final ChatRoomUserService chatRoomUserService;

//    private final JwtUtil jwtUtil;
//
//    @GetMapping("/token-for-ws")
//    public ResponseEntity<Map<String, String>> getTokenForWebSocket(@RequestHeader("Authorization") String authorizationHeader) {
//        Integer userId =  jwtUtil.getUserIdFromToken(authorizationHeader.replace("Bearer ", ""));
//        Map<String, String> response = Map.of("tfws",jwtUtil.generateTokenForWebSocket(userId));
//        return ResponseEntity.ok(response);
//
//    }

    @DeleteMapping("leave-chat")
    public ResponseEntity<Void> leaveChat(@RequestParam("chatRoomId") Long chatRoomId,
                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {
        if (!chatRoomUserService.isUserInChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()))) {
            log.error("User is not in chat room");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        chatRoomUserService.removeUserFromChat(chatRoomId, Integer.parseInt(customUserDetails.getUserId()));
        return ResponseEntity.ok().build();
    }




}

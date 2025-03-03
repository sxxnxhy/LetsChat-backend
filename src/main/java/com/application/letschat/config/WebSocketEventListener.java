package com.application.letschat.config;

import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatRoomUserService chatRoomUserService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            log.info("WebSocket: User disconnected, userId: {}", userId);
            chatRoomUserService.updateLastReadAt(userId);
        } else {
            log.warn("WebSocket: User ID not found in session attributes, sessionId: {}", headerAccessor.getSessionId());
        }
    }
}

package com.application.letschat.service.notificationService;

import com.application.letschat.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void toggleRefresh(List<Integer> userIds) {
        for (Integer userId : userIds) {
            System.out.println(userId);
            messagingTemplate.convertAndSend("/topic/toggle-refresh/" + userId, Boolean.TRUE);
        }
    }
}

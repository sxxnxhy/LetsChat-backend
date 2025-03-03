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
            //새로운 메시지가 있다고 신호 보내기. 현재 접속해있는 해당방의 유저에게
            messagingTemplate.convertAndSend("/topic/toggle-refresh/" + userId, Boolean.TRUE);
        }
    }
}

package com.application.letschat.controller.call;

import com.application.letschat.dto.call.CallNotification;
import com.application.letschat.dto.call.CallRequest;
import com.application.letschat.dto.call.SignalMessage;
import com.application.letschat.dto.user.UserInfoDto;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @MessageMapping("/call/initiate")
    public void initiateCall(@Payload CallRequest request, Principal principal) {
        try {
            UserInfoDto userInfo = userService.extractUserInfoFromSpringSecurity(principal);
            String userId = userInfo.getUserId().toString();
            String targetUserId = request.getTargetUserId();
            log.info("Call request from {} to {}", userId, targetUserId);
            messagingTemplate.convertAndSend(
                    "/queue/call/incoming/" + targetUserId,
                    new CallNotification(userId, userInfo.getName())
            );
        } catch (Exception e) {
            log.error("Error initiating call: {}", e.getMessage());
        }
    }
    @MessageMapping("/call/accept")
    public void acceptCall(@Payload CallRequest request, Principal principal) {
        try {
            UserInfoDto userInfo = userService.extractUserInfoFromSpringSecurity(principal);
            String userId = userInfo.getUserId().toString();
            String targetUserId = request.getTargetUserId();
            log.info("Call Accept by {} for {}", userId, targetUserId);
            messagingTemplate.convertAndSend(
                    "/queue/call/accepted/" + targetUserId,
                    new CallNotification(userId, userInfo.getName())
            );
        } catch (Exception e) {
            log.error("Error accepting call: {}", e.getMessage());
        }
    }

    @MessageMapping("/call/reject")
    public void rejectCall(@Payload CallRequest request, Principal principal) {
        try {
            UserInfoDto userInfo = userService.extractUserInfoFromSpringSecurity(principal);
            String userId = userInfo.getUserId().toString();
            String targetUserId = request.getTargetUserId();
            log.info("Call Reject by {} for {}", userId, targetUserId);
            messagingTemplate.convertAndSend(
                    "/queue/call/rejected/" + targetUserId,
                    new CallNotification(userId, userInfo.getName())
            );
        } catch (Exception e) {
            log.error("Error rejecting call: {}", e.getMessage());
        }
    }
    @MessageMapping("/call/offer")
    public void sendOffer(@Payload SignalMessage signal) {
        try {
            String targetUserId = signal.getTargetUserId();
            if (targetUserId == null) {
                log.error("Offer missing targetUserId: {}", signal);
                return;
            }
            log.info("Offer to {}", targetUserId);
            messagingTemplate.convertAndSend("/queue/call/offer/" + targetUserId, signal);
        } catch (Exception e) {
            log.error("Error sending offer: {}", e.getMessage());
        }
    }

    @MessageMapping("/call/answer")
    public void sendAnswer(@Payload SignalMessage signal) {
        try {
            String targetUserId = signal.getTargetUserId();
            if (targetUserId == null) {
                log.error("Answer missing targetUserId: {}", signal);
                return;
            }
            log.info("Answer to {}", targetUserId);
            messagingTemplate.convertAndSend("/queue/call/answer/" + targetUserId, signal);
        } catch (Exception e) {
            log.error("Error sending answer: {}", e.getMessage());
        }
    }

    @MessageMapping("/call/ice-candidate")
    public void sendIceCandidate(@Payload SignalMessage signal) {
        try {
            String targetUserId = signal.getTargetUserId();
            if (targetUserId == null) {
                log.error("ICE candidate missing targetUserId: {}", signal);
                return;
            }
            log.info("ICE candidate to {}", targetUserId);
            messagingTemplate.convertAndSend("/queue/call/ice-candidate/" + targetUserId, signal);
        } catch (Exception e) {
            log.error("Error sending ICE candidate: {}", e.getMessage());
        }
    }

    @MessageMapping("/call/hangup")
    public void hangup(@Payload SignalMessage signal, Principal principal) {
        String targetUserId = signal.getTargetUserId();
        signal.setTargetUserId(String.valueOf(userService.extractUserInfoFromSpringSecurity(principal).getUserId()));
        messagingTemplate.convertAndSend("/queue/call/hangup/" + targetUserId, signal);
    }


}
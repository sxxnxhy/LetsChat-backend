package com.application.letschat.service.message;

import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final UserService userService;

    private final ChatRoomService chatRoomService;

    private final RedisService redisService;

//    public List<MessageDTO> getMessageDTOs (ChatRoom chatRoom) {
//        List<Message> messages = messageRepository.findByChatRoom(chatRoom);
//        return messages.stream()
//                .map(m -> new MessageDTO(
//                        m.getChatRoom().getChatRoomId(),
//                        m.getUser().getUserId(),
//                        m.getUser().getName(),
//                        m.getContent(),
//                        m.getEnrolledAt()
//                ))
//                .toList();
//    };
//
    public Page<MessageDTO> getMessageDTOs(ChatRoom chatRoom, Pageable pageable) {
        // Fetch paginated messages from the repository
        Page<Message> messagePage = messageRepository.findByChatRoomOrderByEnrolledAtDesc(chatRoom, pageable);

        // Convert entities to DTOs
        return messagePage.map(message -> new MessageDTO(
                message.getChatRoom().getChatRoomId(),
                message.getUser().getUserId(),
                message.getUser().getName(),
                message.getContent(),
                message.getEnrolledAt()
        ));
    }

//    public Message saveMessage(MessageDTO messageDTO) {
//
//        Long chatRoomId = messageDTO.getChatRoomId();
//        User user = userService.getUserById(messageDTO.getSenderId());
//        Message message = new Message();
//        message.setUser(user);
//        message.setContent(messageDTO.getContent());
//
//        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);
//        message.setChatRoom(chatRoom);
//
//        return messageRepository.save(message);
//    }


    @Transactional
    public void syncMessages(Long chatRoomId) {
        List<MessageDTO> pendingMessageDTOs = redisService.getPendingMessages(chatRoomId);
        List<Message> pendingMessages = pendingMessageDTOs.stream()
                .map(dto -> {
                    Message message = new Message();
                    message.setChatRoom(chatRoomService.getChatRoomById(dto.getChatRoomId()));
                    message.setUser(userService.getUserById(dto.getSenderId()));
                    message.setContent(dto.getContent());
                    message.setEnrolledAt(dto.getEnrolledAt());
                    return message;
                })
                .toList();
        log.info("채팅방 {} 싱크", chatRoomId);
        if (!pendingMessages.isEmpty()) {
            List<Message> savedMessages = messageRepository.saveAll(pendingMessages);
            redisService.removePendingMessage(chatRoomId);
//            log.info("채팅방 {} 싱크할 메세지 없음", chatRoomId);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void syncAllMessages() {
        List<MessageDTO> pendingMessages = redisService.getAllPendingMessages();
        if (pendingMessages.isEmpty()) {
            log.info("싱크할 메세지 없음");
            return;
        }
        List<Message> messages = pendingMessages.stream()
                .map(dto -> {
                    Message message = new Message();
                    message.setChatRoom(chatRoomService.getChatRoomById(dto.getChatRoomId()));
                    message.setUser(userService.getUserById(dto.getSenderId()));
                    message.setContent(dto.getContent());
                    message.setEnrolledAt(dto.getEnrolledAt());
                    return message;
                })
                .toList();
        messageRepository.saveAll(messages);
        log.info("Sync all messages 레디스 전체 메세지 싱크 완료");
        redisService.removeAllPendingMessages();
    }

}

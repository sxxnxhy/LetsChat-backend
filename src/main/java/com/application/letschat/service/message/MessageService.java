package com.application.letschat.service.message;

import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.message.Message;
import com.application.letschat.repository.message.MessageBulkRepository;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.service.chatroom.ChatRoomService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final UserService userService;

    private final ChatRoomService chatRoomService;

    private final RedisService redisService;
    private final MessageBulkRepository messageBulkRepository;

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

        // Convert entities to DTOs, handling system messages with null user
        return messagePage.map(message -> {
            if (message.getUser() == null) {
                // System message case
                return new MessageDTO(
                        message.getChatRoom().getChatRoomId(),
                        0,              // senderId = 0 for system messages
                        "System",        // senderName = "System"
                        message.getContent(),
                        message.getEnrolledAt()
                );
            } else {
                // Regular user message case
                return new MessageDTO(
                        message.getChatRoom().getChatRoomId(),
                        message.getUser().getUserId(),
                        message.getUser().getName(),
                        message.getContent(),
                        message.getEnrolledAt()
                );
            }
        });
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
    public void syncMessagesByChatRoomId(Long chatRoomId) {
        List<MessageDTO> pendingMessageDTOs = redisService.getPendingMessages(chatRoomId);
        List<Message> pendingMessages = pendingMessageDTOs.stream()
                .map(dto -> {
                    Message message = new Message();
                    message.setChatRoom(chatRoomService.getChatRoomById(dto.getChatRoomId()));
                    // Handle system messages (senderId = 0 or null)
                    if (dto.getSenderId() == null || dto.getSenderId() == 0) {
                        message.setUser(null); // System message
                    } else {
                        message.setUser(userService.getUserById(dto.getSenderId()));
                    }
                    message.setContent(dto.getContent());
                    message.setEnrolledAt(dto.getEnrolledAt());
                    return message;
                })
                .toList();
        if (!pendingMessages.isEmpty()) {
//            List<Message> savedMessages = messageRepository.saveAll(pendingMessages);
            messageBulkRepository.bulkInsertMessages(pendingMessages);
            redisService.removePendingMessage(chatRoomId);
            log.info("채팅방 {} 싱크완료", chatRoomId);
        } else {
            log.info("채팅방 {} 싱크할 메세지 없음", chatRoomId);
        }
    }

    @Transactional
    public void syncMessagesByUserId(Integer userId) {
        List<Long> chatRoomIds = redisService.getChatRoomIdsByUserId(userId);

        if (chatRoomIds.isEmpty()) {
            log.info("유저 {}에 대해 싱크할 채팅방 없음", userId);
            return;
        }
        for (Long chatRoomId : chatRoomIds) {
            List<MessageDTO> pendingMessageDTOs = redisService.getPendingMessages(chatRoomId);
            List<Message> pendingMessages = pendingMessageDTOs.stream()
                    .map(dto -> {
                        Message message = new Message();
                        message.setChatRoom(chatRoomService.getChatRoomById(dto.getChatRoomId()));
                        // Handle system messages (senderId = 0 or null)
                        if (dto.getSenderId() == null || dto.getSenderId() == 0) {
                            message.setUser(null); // System message
                        } else {
                            message.setUser(userService.getUserById(dto.getSenderId()));
                        }
                        message.setContent(dto.getContent());
                        message.setEnrolledAt(dto.getEnrolledAt());
                        return message;
                    })
                    .toList();

            log.info("유저 {}의 채팅방 {} 싱크 시작", userId, chatRoomId);

            if (!pendingMessages.isEmpty()) {

//                List<Message> savedMessages = messageRepository.saveAll(pendingMessages);
                messageBulkRepository.bulkInsertMessages(pendingMessages);
                redisService.removePendingMessage(chatRoomId);
                log.info("유저 {}의 채팅방 {} 싱크 완료 - {} 메시지 저장됨", userId, chatRoomId, pendingMessages.size());
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void syncAllMessages() {
        List<MessageDTO> pendingMessages = redisService.getAllPendingMessages();
        if (pendingMessages.isEmpty()) {
            log.info("스케쥴러 실행. 전체 싱크할 메세지 없음");
            return;
        }
        List<Message> messages = pendingMessages.stream()
                .map(dto -> {
                    Message message = new Message();
                    message.setChatRoom(chatRoomService.getChatRoomById(dto.getChatRoomId()));
                    // Handle system messages (senderId = 0 or null)
                    if (dto.getSenderId() == null || dto.getSenderId() == 0) {
                        message.setUser(null); // System message
                    } else {
                        message.setUser(userService.getUserById(dto.getSenderId()));
                    }
                    message.setContent(dto.getContent());
                    message.setEnrolledAt(dto.getEnrolledAt());
                    return message;
                })
                .toList();
//        messageRepository.saveAll(messages);
        messageBulkRepository.bulkInsertMessages(messages);
        log.info("스캐줄러 Sync all messages 레디스 전체 메세지 싱크 완료");
        redisService.removeAllPendingMessages();
    }

}

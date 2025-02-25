package com.application.letschat.service.message;

import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public List<MessageDTO> getMessageDTOs (ChatRoom chatRoom) {
        List<Message> messages = messageRepository.findByChatRoom(chatRoom);
        return messages.stream()
                .map(m -> new MessageDTO(
                        m.getChatRoom().getChatRoomId(),
                        m.getUser().getUserId(),
                        m.getUser().getName(),
                        m.getContent(),
                        m.getEnrolledAt()
                ))
                .toList();
    };

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
}

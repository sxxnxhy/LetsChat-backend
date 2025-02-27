package com.application.letschat.service.message;

import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.message.Message;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.message.MessageRepository;
import com.application.letschat.service.chatRoom.ChatRoomService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final UserService userService;

    private final ChatRoomService chatRoomService;

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

    public Message saveMessage(MessageDTO messageDTO) {

        Long chatRoomId = messageDTO.getChatRoomId();
        User user = userService.getUserById(messageDTO.getSenderId());
        Message message = new Message();
        message.setUser(user);
        message.setContent(messageDTO.getContent());

        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);
        message.setChatRoom(chatRoom);

        return messageRepository.save(message);
    }

    public Long getChatHistorySize(Long chatRoomId) {
        return messageRepository.countByChatRoom(chatRoomService.getChatRoomById(chatRoomId));
    }
}

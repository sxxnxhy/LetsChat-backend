package com.application.letschat.service.chatroom;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatroom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatroom.ChatRoomDTO;
import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.user.User;
import com.application.letschat.repository.chatroom.ChatRoomRepository;
import com.application.letschat.repository.chatroomuser.ChatRoomUserRepository;
import com.application.letschat.repository.user.UserRepository;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomUserRepository chatRoomUserRepository;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final ChatRoomUserService chatRoomUserService;

    public Long createChatRoom(ChatRoomCreateDTO chatRoomCreateDTO, Integer userId) {

        User user = userRepository.findById(userId).orElseThrow();
        User targetUser = userRepository.findById(chatRoomCreateDTO.getTargetUserId()).orElseThrow();

        //방만들기
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomName(user.getName() + ", " + chatRoomCreateDTO.getTargetUserName()); //방 제목
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        //생성된 방에 유저 추가
        chatRoomUserService.addUserToChatRoom(user, savedChatRoom);

        //생성된 방에 유저 추가
        chatRoomUserService.addUserToChatRoom(targetUser, savedChatRoom);

        return savedChatRoom.getChatRoomId();
    }


//    public Boolean checkAccess(String token, Long chatRoomId) {
//        boolean isValid = false;
//        Integer userId = jwtUtil.getUserIdFromToken(token);
//        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByChatRoomId(chatRoomId)
//                .orElseThrow(() -> new RuntimeException("No users found in chat room"));
//        isValid = chatRoomUsers.stream()
//                .anyMatch(chatRoomUser -> chatRoomUser.getUser().getUserId().equals(userId));
//        return isValid;
//    }




    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    public void updateSubject(ChatRoomDTO chatRoomDTO) {
        chatRoomRepository.updateChatRoomName(chatRoomDTO.getChatRoomId(),
                chatRoomDTO.getChatRoomName(),
                Timestamp.valueOf(LocalDateTime.now()));
    }
}

package com.application.letschat.service.chatRoom;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatRoom.ChatRoomCreateDTO;
import com.application.letschat.dto.chatRoom.ChatRoomDTO;
import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.chatRoomUser.ChatRoomUser;
import com.application.letschat.model.user.User;
import com.application.letschat.repository.chatRoom.ChatRoomRepository;
import com.application.letschat.repository.chatRoomUser.ChatRoomUserRepository;
import com.application.letschat.repository.user.UserRepository;
import com.application.letschat.service.chatRoomUser.ChatRoomUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomUserRepository chatRoomUserRepository;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final ChatRoomUserService chatRoomUserService;


    public Long createChatRoom(ChatRoomCreateDTO chatRoomCreateDTO,
                               String token) {

        User user = userRepository.findById(jwtUtil.getUserIdFromToken(token)).orElseThrow();
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




    public ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow();
    }


}

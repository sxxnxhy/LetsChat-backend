package com.application.letschat.service.chatroom;

import com.application.letschat.config.jwt.JwtUtil;
import com.application.letschat.dto.chatroom.ChatRoomCreateDto;
import com.application.letschat.dto.chatroom.ChatRoomDto;
import com.application.letschat.dto.chatroomuser.ChatRoomUserDto;
import com.application.letschat.dto.user.UserInfoDto;
import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.user.User;
import com.application.letschat.repository.chatroom.ChatRoomRepository;
import com.application.letschat.repository.chatroomuser.ChatRoomUserRepository;
import com.application.letschat.repository.user.UserRepository;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.redis.RedisService;
import com.application.letschat.service.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ChatRoomUserService chatRoomUserService;
    private final RedisService redisService;
    private final UserService userService;

    public Long createChatRoom(ChatRoomCreateDto chatRoomCreateDTO, Integer userId) {

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


    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new RuntimeException("Chat room not found"));
    }

    public void updateSubject(ChatRoomDto chatRoomDTO) {
        chatRoomRepository.updateChatRoomName(chatRoomDTO.getChatRoomId(),
                chatRoomDTO.getChatRoomName(),
                Timestamp.valueOf(LocalDateTime.now()));
    }


    public List<UserInfoDto> getUsersInChatRoom(Long chatRoomId) {
        List<Integer> userIds = redisService.getUserIdsByChatRoomId(chatRoomId);
        List<User> users = userService.getUsersById(userIds);
        return users.stream()
                .map(user -> new UserInfoDto(user.getUserId(), user.getName(), user.getEmail()))
                .toList();
    }

    public void updateLastReadAt(Long chatRoomId, Integer userId) {
        ChatRoomUserDto chatRoomUserDto = ChatRoomUserDto.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .lastReadAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        redisService.addPendingLastReadAt(chatRoomUserDto);
    }



}

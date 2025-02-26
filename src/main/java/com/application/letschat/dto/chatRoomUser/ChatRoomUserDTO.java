package com.application.letschat.dto.chatRoomUser;


import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomUserDTO {

    private Long chatRoomUserId;

    private Long chatRoomId;

    private Integer userId;
}

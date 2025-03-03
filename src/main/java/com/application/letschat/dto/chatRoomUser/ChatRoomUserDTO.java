package com.application.letschat.dto.chatRoomUser;


import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.user.User;
import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomUserDTO {

    private Long chatRoomUserId;

    private Long chatRoomId;

    private Integer userId;

    private Timestamp lastReadAt;
}

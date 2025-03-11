package com.application.letschat.dto.chatRoomUser;


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

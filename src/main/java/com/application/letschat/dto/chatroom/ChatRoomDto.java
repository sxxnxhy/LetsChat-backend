package com.application.letschat.dto.chatroom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDto {

    private Long chatRoomId;
    private String chatRoomName;
    private Integer senderId;

}

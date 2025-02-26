package com.application.letschat.dto.chatList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class ChatListDTO {

    private Long chatRoomId;
    private String chatRoomName;
    private String lastMessage;
    private Timestamp lastMessageTime;
}

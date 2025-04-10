package com.application.letschat.dto.chatlist;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class ChatListDto {

    private Long chatRoomId;
    private String chatRoomName;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private Timestamp lastReadAt;
}

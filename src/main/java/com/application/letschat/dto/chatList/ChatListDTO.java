package com.application.letschat.dto.chatList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChatListDTO {

    private Long chatRoomId;
    private String chatRoomName;
    private String lastMessage;
}

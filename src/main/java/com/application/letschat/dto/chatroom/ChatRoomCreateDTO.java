package com.application.letschat.dto.chatroom;


import lombok.Data;

@Data
public class ChatRoomCreateDTO {
    private Integer targetUserId;
    private String targetUserName;
}

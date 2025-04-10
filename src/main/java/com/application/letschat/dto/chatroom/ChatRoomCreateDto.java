package com.application.letschat.dto.chatroom;


import lombok.Data;

@Data
public class ChatRoomCreateDto {
    private Integer targetUserId;
    private String targetUserName;
}

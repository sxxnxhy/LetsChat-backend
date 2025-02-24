package com.application.letschat.dto.chatRoom;


import lombok.Data;

@Data
public class ChatRoomCreateDTO {
    private Integer userId;
    private Integer targetUserId;
    private String targetUserName;
}

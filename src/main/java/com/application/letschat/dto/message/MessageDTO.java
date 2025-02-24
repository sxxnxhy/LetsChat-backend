package com.application.letschat.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long chatRoomId;

    private Integer senderId;

    private String senderName;

    private String content;


}

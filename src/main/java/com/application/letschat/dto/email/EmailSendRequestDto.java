package com.application.letschat.dto.email;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EmailSendRequestDto {
    Long chatRoomId;
}

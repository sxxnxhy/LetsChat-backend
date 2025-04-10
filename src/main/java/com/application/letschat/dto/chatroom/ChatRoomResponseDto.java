package com.application.letschat.dto.chatroom;

import com.application.letschat.dto.message.MessageDto;
import com.application.letschat.dto.user.UserInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
public class ChatRoomResponseDto {
    private String chatRoomName;
    private List<MessageDto> messages;
    private int totalPages;
    private List<UserInfoDto> users;
}

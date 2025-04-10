package com.application.letschat.dto.user;

import com.application.letschat.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserSearchResponseDto {
    private List<User> allUsers;
    private List<Integer> chatRoomUsers;
}

package com.application.letschat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserInfoDto {
    private Integer userId;
    private String email;
    private String name;
}

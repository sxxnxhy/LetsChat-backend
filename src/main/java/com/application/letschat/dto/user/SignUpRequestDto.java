package com.application.letschat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SignUpRequestDto {
    private String email;
    private String name;
    private String password;
}

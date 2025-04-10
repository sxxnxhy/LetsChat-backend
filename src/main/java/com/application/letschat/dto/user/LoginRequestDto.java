package com.application.letschat.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDto {
    String email;
    String password;

}

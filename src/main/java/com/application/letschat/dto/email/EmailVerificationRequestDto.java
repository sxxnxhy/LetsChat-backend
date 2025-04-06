package com.application.letschat.dto.email;

import lombok.Data;

@Data
public class EmailVerificationRequestDto {

    private String email;
    private int code;

}

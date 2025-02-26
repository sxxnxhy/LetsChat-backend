package com.application.letschat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
public class UserDTO {
    private Integer userId;
    private String name;
    private String password;
    private String token;

}

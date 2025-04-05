package com.application.letschat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String email;
    private String name;
    private String password;
    private String token;

    public UserDTO(Integer userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

}

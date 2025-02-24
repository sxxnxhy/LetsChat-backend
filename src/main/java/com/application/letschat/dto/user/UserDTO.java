package com.application.letschat.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;
    private String name;
    private String password;
    private String token;

    public UserDTO(Integer userId, String name, String token) {
        this.userId = userId;
        this.name = name;
        this.token = token;
    }

}

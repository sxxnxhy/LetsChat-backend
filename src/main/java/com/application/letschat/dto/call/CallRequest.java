package com.application.letschat.dto.call;

import lombok.Data;

@Data
public class CallRequest {
    private String targetUserId;
    private Integer userId;
}

package com.application.letschat.dto.call;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallNotification {
    private String userId;
    private String name;
}

package com.application.letschat.dto.call;

import lombok.Data;

@Data
public class SignalMessage {
    private String targetUserId;
    private Object sdp;
    private Object candidate;
}

package com.application.letschat.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginUrlResponseDto {
    String url;
}

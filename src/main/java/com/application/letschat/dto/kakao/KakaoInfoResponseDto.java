package com.application.letschat.dto.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoInfoResponseDto {
    private Long id;
    private String nickname;
    private String email;
}

package com.application.letschat.service.validation;

import com.application.letschat.dto.user.LoginRequestDto;
import com.application.letschat.dto.user.SignUpRequestDto;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {


    public boolean validateEmail(LoginRequestDto loginRequestDto) {
        return loginRequestDto.getEmail() != null && loginRequestDto.getEmail().length() <= 255 &&
                loginRequestDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public boolean validateSignUpRequest(SignUpRequestDto signUpRequestDto) {
        if (signUpRequestDto.getEmail() == null || signUpRequestDto.getEmail().length() > 255 ||
                !signUpRequestDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return false;
        }
        if (signUpRequestDto.getName() == null ||
                signUpRequestDto.getName().length() > 100 ||
                !signUpRequestDto.getName().matches("^[a-zA-Z가-힣\\-.'][a-zA-Z가-힣\\s\\-.']{0,99}$")) {
            return false;
        }
        if (signUpRequestDto.getPassword() == null || signUpRequestDto.getPassword().length() > 255) {
            return false;
        }
        return true;
    }
}

package com.application.letschat.service.validation;

import com.application.letschat.dto.user.LoginRequestDto;
import com.application.letschat.dto.user.SignUpRequestDto;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    public boolean validateSignUpRequest(SignUpRequestDto signUpRequestDto) {
        return isValidEmail(signUpRequestDto.getEmail()) &&
                isValidName(signUpRequestDto.getName()) &&
                isValidPassword(signUpRequestDto.getPassword());
    }


    public boolean isValidEmail(String email) {
        return email != null && email.length() <= 255 &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public boolean isValidName(String name) {
        return name != null &&
                !name.isEmpty() &&
                name.length() <= 100 &&
                name.matches("^[a-zA-Z가-힣\\-.'][a-zA-Z가-힣\\s\\-.']{0,99}$");
    }

    public boolean isValidPassword(String password) {
        return password !=null && password.length() < 255;
    }

}

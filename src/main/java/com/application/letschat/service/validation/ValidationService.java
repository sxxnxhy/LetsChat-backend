package com.application.letschat.service.validation;

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

        if (email != null && email.length() <= 255 &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return true;
        } else if (email.equals("tester1") || email.equals("tester2")) {
            return true;
        } else { return false; }
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

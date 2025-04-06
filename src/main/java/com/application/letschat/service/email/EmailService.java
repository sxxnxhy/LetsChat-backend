package com.application.letschat.service.email;


import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.service.redis.RedisService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisService redisService;
    private static final String senderEmail= "syoo.shop@gmail.com";
    private static int generatedVerificationCode;

    public static void generateVerificationCode() {
        generatedVerificationCode = (int)(Math.random() * (90000)) + 100000;
    }

    public MimeMessage createMail(String email) {
        generateVerificationCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + generatedVerificationCode + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }


    public void sendVerificationEmail(String email) {
        MimeMessage message = createMail(email);
        redisService.addEmailVerificationCode(email, generatedVerificationCode);
        javaMailSender.send(message);
    }

    public Boolean verifyCode(EmailVerificationRequestDto dto) {
        Object storedCode = redisService.getEmailVerificationCode(dto.getEmail());
        return storedCode != null && storedCode.toString().equals(String.valueOf(dto.getCode()));
    }

}

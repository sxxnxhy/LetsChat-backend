package com.application.letschat.service.email;


import com.application.letschat.dto.email.EmailVerificationRequestDto;
import com.application.letschat.service.chatroomuser.ChatRoomUserService;
import com.application.letschat.service.redis.RedisService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisService redisService;
    private final ChatRoomUserService chatRoomUserService;
    private static final String senderEmail= "syoo.shop@gmail.com";
    private static int generatedVerificationCode;

    public static void generateVerificationCode() {
        generatedVerificationCode = (int)(Math.random() * (90000)) + 100000;
    }

    public MimeMessage createMailForVerification(String email) {
        generateVerificationCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(senderEmail, "Let's Chat"));
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + generatedVerificationCode + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    public MimeMessage createMailForNotification(List<String> emails, String username) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(new InternetAddress(senderEmail, "Let's Chat"));
            helper.setTo(emails.toArray(new String[0]));
            helper.setSubject("새로운 메시지가 도착했습니다!");
            String body = "<h3>" + username + "님이 새 메시지를 보냈습니다.</h3>" +
                    "<h3>지금 확인해보세요.</h3>" +
                    "<h3>감사합니다.</h3>";
            helper.setText(body, true);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return message;
    }


    public void sendVerificationEmail(String email) {
        MimeMessage message = createMailForVerification(email);
        redisService.addEmailVerificationCode(email, generatedVerificationCode);
        javaMailSender.send(message);
    }

    public Boolean verifyCode(EmailVerificationRequestDto dto) {
        Object storedCode = redisService.getEmailVerificationCode(dto.getEmail());
        return storedCode != null && storedCode.toString().equals(String.valueOf(dto.getCode()));
    }

    public void sendNotificationEmail(Long chatRoomId, String username, String userEmail) {
        List<String> emails = chatRoomUserService.getEmailsByChatRoomId(chatRoomId)
                .stream()
                .filter(email -> !email.equals(userEmail))
                .toList();
        javaMailSender.send(createMailForNotification(emails, username));
    }



}

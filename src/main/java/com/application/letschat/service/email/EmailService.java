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
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(senderEmail, "Let's Chat"));
            helper.setTo(email);
            helper.setSubject("Let's Chat 이메일 인증번호 안내");

            String plainText = "안녕하세요!\n\n" +
                    "요청하신 이메일 인증번호는 다음과 같습니다:\n\n" +
                    generatedVerificationCode + "\n\n" +
                    "본 인증번호는 5분간 유효합니다.\n" +
                    "감사합니다.\n\n" +
                    "- Let's Chat 팀";

            String htmlText = "<html><body>" +
                    "<h2>안녕하세요!</h2>" +
                    "<p>요청하신 <strong>이메일 인증번호</strong>는 아래와 같습니다:</p>" +
                    "<h1 style='color: #2E86C1;'>" + generatedVerificationCode + "</h1>" +
                    "<p>본 인증번호는 <strong>5분간 유효</strong>합니다.</p>" +
                    "<br><p>감사합니다.<br>Let's Chat 팀 드림</p>" +
                    "</body></html>";

            helper.setText(plainText, htmlText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public MimeMessage createMailForNotification(List<String> emails, String username) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(senderEmail, "Let's Chat"));
            helper.setTo(emails.toArray(new String[0]));
            helper.setSubject("새 메시지가 도착했습니다 - Let's Chat");

            String plainText = username + "님이 새로운 메시지를 보냈습니다.\n\n" +
                    "지금 확인해보세요!\n\n" +
                    "감사합니다.\n\n" +
                    "- Let's Chat 팀";

            String htmlText = "<html><body>" +
                    "<h2>📩 새 메시지 알림</h2>" +
                    "<p><strong>" + username + "</strong>님이 새로운 메시지를 보냈습니다.</p>" +
                    "<p><a href='https://syoo.shop'>지금 확인하러 가기</a></p>" +
                    "<br><p>감사합니다.<br>Let's Chat 팀 드림</p>" +
                    "</body></html>";

            helper.setText(plainText, htmlText);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
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

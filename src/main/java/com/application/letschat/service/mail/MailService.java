package com.application.letschat.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String senderEmail= "syoo.shop@gmail.com";
    private static int number;

//    // 랜덤으로 숫자 생성
//    public static void createNumber() {
//        number = (int)(Math.random() * (90000)) + 100000; //(int) Math.random() * (최댓값-최소값+1) + 최소값
//    }

    public MimeMessage CreateMail(String mail) {
//        createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);

//            message.setSubject("New message in your inbox!");
//            String body = "";
//            body += "<p>Hi " + ",</p>" +
//                    "<p>You have a new message in your inbox.<br>" +
//                    "Log in to your account to read it:</p>" +
//                    "<p><a href='https://syoo.shop'>https://syoo.shop</a></p>" +
//                    "<p>Best regards,<br>Chat Team</p>";
//            message.setText(body, "UTF-8", "html");

            message.setSubject("syoo Chat에서 새 메시지가 도착했습니다");
            String body = "<p>안녕하세요.</p>" +
                    "<p>syoo Chat에서 새로운 메시지가 도착했습니다.<br>" +
                    "아래 링크를 클릭하여 확인해 주세요:</p>" +

                    "<p><a href='https://syoo.shop' target='_blank'>SYoo Chat 바로가기</a></p>" +

                    "<hr>" +

                    "<p>감사합니다.<br>" +
                    "SYoo Chat 팀 드림<br>" +
                    "<a href='https://syoo.shop'>https://syoo.shop</a></p>";
            message.setText(body, "UTF-8", "html");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public int sendMail(String mail) {
        MimeMessage message = CreateMail(mail);
        javaMailSender.send(message);

        return number;
    }
}
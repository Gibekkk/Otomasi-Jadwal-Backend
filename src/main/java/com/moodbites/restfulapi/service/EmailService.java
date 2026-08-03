package com.moodbites.restfulapi.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.moodbites.restfulapi.dto.EmailDetails;
import com.moodbites.restfulapi.model.User;

import jakarta.mail.internet.MimeMessage;

import com.moodbites.restfulapi.model.OTP;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void sendEmail(EmailDetails details) {
        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setText(details.getMsgBody(), true);
            helper.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(mimeMessage);
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOTPRegisToLogin(User user, OTP otp) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html>");
        htmlContent.append("<body>");
        htmlContent.append("<p>Halo ").append(user.getName()).append(",</p>");
        htmlContent.append("<p>Terima kasih telah bergabung bersama kami.</p>");
        htmlContent.append("<p>Untuk memverifikasi akun Anda, silakan gunakan kode OTP berikut:</p><p></p>");
        htmlContent.append("<p>Kode OTP Anda: </p>");
        htmlContent.append("<p style='font-size: 20px; color: blue;'>").append(otp.getCode()).append("</p><p></p>");
        htmlContent.append("<p>Kode ini hanya berlaku selama 5 menit sejak email ini diterima.</p><p></p>");
        htmlContent.append("<p>Jika Anda merasa tidak melakukan pendaftaran, abaikan email ini.</p><p></p>");
        htmlContent.append("<p><strong>Salam hangat, Tim Development</strong></p>");
        htmlContent.append("</body>");
        htmlContent.append("</html>");

        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Kode OTP Anda");
        details.setMsgBody(htmlContent.toString());

        sendEmail(details);
    }
}

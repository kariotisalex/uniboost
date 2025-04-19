package com.alexkariotis.uniboost.service;


import com.alexkariotis.uniboost.domain.entity.User;
import io.vavr.control.Try;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${reset-password-request.domain}")
    private String resetLink;

    public Try<Void> initiatePasswordReset(User user, String token) {

        return Try.run(()-> {
            resetLink += token;
            String subject = "Reset Your Password – Uniboost";
            String body = """
            Hello %s,

            We received a request to reset your password for your Uniboost account.

            Click the link below to choose a new password:
            %s

            This link will expire in 30 minutes. If you didn't request a password reset, you can safely ignore this email.

            Thanks,
            The Uniboost Team
            """.formatted(user.getFirstname(), resetLink);
            sendResetEmail(user.getEmail(), subject, body);

        });

    }

    private Try<Void> sendResetEmail(String to, String subject, String body) {
        return Try.run(()-> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("testing@uniboost.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        });
    }
}

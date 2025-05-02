package com.alexkariotis.uniboost.service;


import com.alexkariotis.uniboost.domain.entity.User;
import com.alexkariotis.uniboost.dto.SendEmailDto;
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

    public Try<Void> sendEmail(SendEmailDto sendEmailDto) {
        return Try.run(()-> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("testing@uniboost.com");
            message.setTo(sendEmailDto.getSendTo());
            message.setSubject(sendEmailDto.getSubject());
            message.setText(sendEmailDto.getBody());
            mailSender.send(message);
        });
    }
}

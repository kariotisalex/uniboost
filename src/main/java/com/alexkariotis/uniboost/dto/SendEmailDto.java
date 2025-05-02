package com.alexkariotis.uniboost.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SendEmailDto {

    private String sendTo;
    private String subject;
    private String body;

}

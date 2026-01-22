package com.ao.service.impl;

import com.ao.entity.Tender;
import com.ao.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendAlert(String subject, String body) {
        log.info("EMAIL START");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("jamaleddine.reda@gmail.com");
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("EMAIL END");
    }
}

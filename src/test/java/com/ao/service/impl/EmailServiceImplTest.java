package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void shouldNotSendWhenRecipientIsBlank() {
        EmailServiceImpl service = buildService(true);

        service.sendAlert(sampleAo(), "   ");

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void shouldSendWhenEnabledAndRecipientPresent() {
        EmailServiceImpl service = buildService(true);

        service.sendAlert(sampleAo(), "user@example.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertTrue(msg.getSubject().contains("AO"));
        assertTrue(msg.getText().contains("AO-123"));
    }

    private EmailServiceImpl buildService(boolean enabled) {
        EmailServiceImpl service = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(service, "mailFrom", "noreply@aomvp.local");
        ReflectionTestUtils.setField(service, "subjectPrefix", "[AO]");
        ReflectionTestUtils.setField(service, "mailEnabled", enabled);
        return service;
    }

    private AppelOffre sampleAo() {
        return AppelOffre.builder()
                .reference("AO-123")
                .objet("Objet test")
                .organisme("Org")
                .dateLimite(LocalDateTime.now().plusDays(2))
                .urlDetail("https://example.com")
                .build();
    }
}

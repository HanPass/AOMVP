package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.alert.mail.to}")
    private String mailTo;

    @Value("${app.alert.mail.from:${spring.mail.username:}}")
    private String mailFrom;

    @Value("${app.alert.mail.subject-prefix:[AO]}")
    private String subjectPrefix;

    @Override
    public void sendAlert(String subject, String body) {
        log.info("EMAIL START");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailTo);
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setSubject(subjectPrefix + " " + subject);
        message.setText(body);

        mailSender.send(message);
        log.info("EMAIL END");
    }

    @Override
    public void sendAlert(AppelOffre ao) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mailTo);
            message.setSubject("🆕 Nouvel appel d’offre – " + ao.getReference());
            message.setText("""
                Nouvel appel d’offre détecté

                Référence : %s
                Objet     : %s
                Organisme : %s
                Date limite : %s

                Lien :
                %s
                """.formatted(
                    ao.getReference(),
                    ao.getObjet(),
                    ao.getOrganisme(),
                    ao.getDateLimite(),
                    ao.getUrlDetail()
            ));

            //mailSender.send(message);
            log.info("📧 Mail envoyé pour {}", ao.getReference());

        } catch (Exception e) {
            log.error("❌ Erreur envoi mail {}", ao.getReference(), e);
        }
    }

}

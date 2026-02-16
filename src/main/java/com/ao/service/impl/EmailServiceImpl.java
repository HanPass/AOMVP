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

    @Value("${app.alert.mail.from:${spring.mail.username:}}")
    private String mailFrom;

    @Value("${app.alert.mail.subject-prefix:[AO]}")
    private String subjectPrefix;

    @Value("${app.alert.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    public void sendAlert(AppelOffre ao, String recipientEmail) {
        if (ao == null) {
            log.warn("⚠️ Mail ignoré: AppelOffre null");
            return;
        }

        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("⚠️ Mail ignoré: destinataire vide pour {}", ao.getReference());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail.trim());
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setSubject(subjectPrefix + " 🆕 Nouvel appel d’offre – " + ao.getReference());
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

            if (!mailEnabled) {
                log.info("📧 [DISABLED] Mail préparé pour {} ({})", ao.getReference(), recipientEmail);
                return;
            }

            mailSender.send(message);
            log.info("📧 Mail envoyé pour {} ({})", ao.getReference(), recipientEmail);

        } catch (Exception e) {
            log.error("❌ Erreur envoi mail {} vers {}", ao.getReference(), recipientEmail, e);
        }
    }
}

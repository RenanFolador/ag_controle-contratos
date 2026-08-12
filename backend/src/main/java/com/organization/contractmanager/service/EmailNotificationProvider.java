package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationProvider(
            JavaMailSender mailSender,
            @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(notification.getSubject());
            helper.setText(emailTemplate(notification), true);
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Could not prepare expiration email", exception);
        }
    }

    private String emailTemplate(Notification notification) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                  <body style="font-family:Arial,sans-serif;color:#202124">
                    <h2>Aviso de vencimento contratual</h2>
                    <p>Olá, %s.</p>
                    <p>O contrato <strong>%s</strong> vence em <strong>%s</strong>.</p>
                    <p>Este aviso corresponde ao prazo de <strong>%d dias</strong>.</p>
                    <p>Verifique as providências necessárias para a continuidade ou encerramento da vigência.</p>
                  </body>
                </html>
                """.formatted(
                escape(notification.getRecipientName()),
                escape(notification.getContract().getContractNumber()),
                notification.getExpirationDate(), notification.getDaysBefore());
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

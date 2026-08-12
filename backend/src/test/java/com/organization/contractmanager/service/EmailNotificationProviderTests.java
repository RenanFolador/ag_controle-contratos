package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.Person;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

class EmailNotificationProviderTests {

    @Test
    void preparesExpirationEmailWithoutConnectingToSmtp() throws Exception {
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        EmailNotificationProvider provider = new EmailNotificationProvider(
                mailSender, "contratos@example.com");
        Notification notification = notification();

        provider.send(notification);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo(notification.getSubject());
        assertThat(mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())
                .isEqualTo("responsavel@example.com");
        assertThat(mimeMessage.getFrom()[0].toString()).isEqualTo("contratos@example.com");
        assertThat(mimeMessage.getContent().toString())
                .contains("Aviso de vencimento contratual", "30 dias", "025/2026");
    }

    private Notification notification() {
        Contract contract = new Contract(
                "025/2026", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "test");
        Person person = new Person(
                "Responsavel", null, "REG", "responsavel@example.com",
                null, false, true);
        return new Notification(
                contract, person, contract.getEndDate(), 30,
                contract.getEndDate().minusDays(30), NotificationChannel.EMAIL,
                person.getName(), person.getEmail(),
                "[Contratos] Aviso de vencimento — Contrato 025/2026 — prazo de 30 dias",
                "Mensagem");
    }
}

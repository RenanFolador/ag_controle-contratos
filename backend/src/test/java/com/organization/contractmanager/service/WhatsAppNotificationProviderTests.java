package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.Person;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WhatsAppNotificationProviderTests {

    @Test
    void sendsThroughAbstractApiClient() {
        WhatsAppApiClient apiClient = org.mockito.Mockito.mock(WhatsAppApiClient.class);
        when(apiClient.sendText("5541999999999", "Mensagem"))
                .thenReturn(new WhatsAppSendResult("wamid.123", "accepted"));
        WhatsAppNotificationProvider provider = new WhatsAppNotificationProvider(apiClient);

        provider.send(notification());

        assertThat(provider.getChannel()).isEqualTo(NotificationChannel.WHATSAPP);
        verify(apiClient).sendText("5541999999999", "Mensagem");
    }

    @Test
    void propagatesSanitizedProviderFailure() {
        WhatsAppApiClient apiClient = org.mockito.Mockito.mock(WhatsAppApiClient.class);
        WhatsAppProviderException failure =
                new WhatsAppProviderException(401, "190", "trace-123");
        when(apiClient.sendText("5541999999999", "Mensagem")).thenThrow(failure);
        WhatsAppNotificationProvider provider = new WhatsAppNotificationProvider(apiClient);

        assertThatThrownBy(() -> provider.send(notification()))
                .isSameAs(failure)
                .hasMessage("WhatsApp provider request failed (HTTP 401, code 190)")
                .hasMessageNotContaining("access-token");
    }

    private Notification notification() {
        Contract contract = new Contract(
                "025/2026", "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "test");
        Person person = new Person(
                "Responsavel", null, "REG", null,
                "5541999999999", true, true);
        return new Notification(
                contract, person, contract.getEndDate(), 30,
                contract.getEndDate().minusDays(30), NotificationChannel.WHATSAPP,
                person.getName(), person.getPhone(), "Assunto", "Mensagem");
    }
}

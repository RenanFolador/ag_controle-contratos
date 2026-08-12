package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import com.organization.contractmanager.domain.Person;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class NotificationRepositoryTests {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private PersonRepository personRepository;

    @Test
    void persistsPendingNotificationWithRecipientSnapshot() {
        Contract contract = contractRepository.saveAndFlush(contract("NOTIFICATION-001"));
        Person person = personRepository.saveAndFlush(person("person1@example.com"));

        Notification saved = notificationRepository.saveAndFlush(
                notification(contract, person, NotificationChannel.EMAIL));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getRecipientName()).isEqualTo("Responsavel");
        assertThat(saved.getRecipientAddress()).isEqualTo("person1@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateContractPersonExpirationDeadlineAndChannel() {
        Contract contract = contractRepository.saveAndFlush(contract("NOTIFICATION-002"));
        Person person = personRepository.saveAndFlush(person("person2@example.com"));
        notificationRepository.saveAndFlush(
                notification(contract, person, NotificationChannel.EMAIL));

        assertThatThrownBy(() -> notificationRepository.saveAndFlush(
                notification(contract, person, NotificationChannel.EMAIL)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Notification notification(
            Contract contract, Person person, NotificationChannel channel) {
        return new Notification(
                contract, person, contract.getEndDate(), 30,
                contract.getEndDate().minusDays(30), channel, person.getName(),
                person.getEmail(), "Vencimento", "Mensagem");
    }

    private Contract contract(String number) {
        return new Contract(
                number, "PROC", "Objeto", "Empresa", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("100.00"), ContractStatus.ACTIVE, null, "test");
    }

    private Person person(String email) {
        return new Person("Responsavel", null, "REG", email,
                "11999999999", true, true);
    }
}

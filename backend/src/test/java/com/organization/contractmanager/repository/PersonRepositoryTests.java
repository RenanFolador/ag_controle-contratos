package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class PersonRepositoryTests {

    @Autowired
    private PersonRepository repository;

    @Test
    void persistsAndFindsPersonByCpf() {
        Person saved = repository.saveAndFlush(person("Maria da Silva", "123.456.789-01"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(saved.isActive()).isTrue();
        assertThat(repository.findByCpf("123.456.789-01"))
                .get()
                .extracting(Person::getName, Person::getEmail)
                .containsExactly("Maria da Silva", "maria@example.com");
    }

    @Test
    void rejectsDuplicateCpfWhenInformed() {
        repository.saveAndFlush(person("Maria da Silva", "123.456.789-01"));

        assertThatThrownBy(() -> repository.saveAndFlush(
                person("Outra Pessoa", "123.456.789-01")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsMoreThanOnePersonWithoutCpf() {
        repository.saveAndFlush(person("Pessoa sem CPF 1", null));
        repository.saveAndFlush(person("Pessoa sem CPF 2", null));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void updatesAuditTimestamp() {
        Person saved = repository.saveAndFlush(person("Maria da Silva", "123.456.789-01"));
        var originalUpdatedAt = saved.getUpdatedAt();

        saved.setPhone("(11) 99999-0000");
        repository.saveAndFlush(saved);

        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    private Person person(String name, String cpf) {
        return new Person(
                name,
                cpf,
                "MAT-001",
                "maria@example.com",
                "(11) 3333-4444",
                true,
                true);
    }
}

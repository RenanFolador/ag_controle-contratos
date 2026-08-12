package com.organization.contractmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.dto.PersonCreateRequest;
import com.organization.contractmanager.dto.PersonUpdateRequest;
import com.organization.contractmanager.exception.DuplicatePersonCpfException;
import com.organization.contractmanager.exception.PersonNotFoundException;
import com.organization.contractmanager.mapper.PersonMapper;
import com.organization.contractmanager.repository.PersonRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceTests {

    @Mock
    private PersonRepository repository;

    private PersonService service;

    @BeforeEach
    void setUp() {
        service = new PersonService(repository, new PersonMapper());
    }

    @Test
    void createsPersonAsActive() {
        when(repository.findByCpf("123.456.789-01")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(createRequest("123.456.789-01"));

        assertThat(response.active()).isTrue();
        assertThat(response.name()).isEqualTo("Maria da Silva");
    }

    @Test
    void rejectsDuplicateCpf() {
        when(repository.findByCpf("123.456.789-01"))
                .thenReturn(Optional.of(person(true)));

        assertThatThrownBy(() -> service.create(createRequest("123.456.789-01")))
                .isInstanceOf(DuplicatePersonCpfException.class);
    }

    @Test
    void searchesByNameInRepository() {
        when(repository.findAllByNameContainingIgnoreCaseOrderByNameAsc("maria"))
                .thenReturn(List.of(person(true)));

        assertThat(service.findAll("  maria ")).hasSize(1);
        verify(repository).findAllByNameContainingIgnoreCaseOrderByNameAsc("maria");
    }

    @Test
    void reportsMissingPerson() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(PersonNotFoundException.class);
    }

    @Test
    void logicallyDeactivatesPersonThroughUpdate() {
        UUID id = UUID.randomUUID();
        Person person = person(true);
        when(repository.findById(id)).thenReturn(Optional.of(person));
        when(repository.findByCpf("123.456.789-01")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(person)).thenReturn(person);

        var response = service.update(id, new PersonUpdateRequest(
                "Maria da Silva", "123.456.789-01", "MAT-1", "maria@example.com",
                null, false, false));

        assertThat(response.active()).isFalse();
        verify(repository).saveAndFlush(person);
    }

    private PersonCreateRequest createRequest(String cpf) {
        return new PersonCreateRequest(
                "Maria da Silva", cpf, "MAT-1", "maria@example.com", null, true);
    }

    private Person person(boolean active) {
        return new Person(
                "Maria da Silva", "123.456.789-01", "MAT-1", "maria@example.com",
                null, true, active);
    }
}

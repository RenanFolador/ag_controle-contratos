package com.organization.contractmanager.mapper;

import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.dto.PersonCreateRequest;
import com.organization.contractmanager.dto.PersonResponse;
import com.organization.contractmanager.dto.PersonUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public Person toEntity(PersonCreateRequest request) {
        return new Person(
                request.name(), normalizeOptional(request.cpf()),
                normalizeOptional(request.registration()), normalizeOptional(request.email()),
                normalizeOptional(request.phone()), request.whatsappEnabled(), true);
    }

    public void update(Person person, PersonUpdateRequest request) {
        person.setName(request.name());
        person.setCpf(normalizeOptional(request.cpf()));
        person.setRegistration(normalizeOptional(request.registration()));
        person.setEmail(normalizeOptional(request.email()));
        person.setPhone(normalizeOptional(request.phone()));
        person.setWhatsappEnabled(request.whatsappEnabled());
        person.setActive(request.active());
    }

    public PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.getId(), person.getName(), person.getCpf(), person.getRegistration(),
                person.getEmail(), person.getPhone(), person.isWhatsappEnabled(),
                person.isActive(), person.getCreatedAt(), person.getUpdatedAt());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

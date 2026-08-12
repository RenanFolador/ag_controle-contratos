package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.dto.PersonCreateRequest;
import com.organization.contractmanager.dto.PersonResponse;
import com.organization.contractmanager.dto.PersonUpdateRequest;
import com.organization.contractmanager.exception.DuplicatePersonCpfException;
import com.organization.contractmanager.exception.PersonNotFoundException;
import com.organization.contractmanager.mapper.PersonMapper;
import com.organization.contractmanager.repository.PersonRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final PersonMapper mapper;

    public PersonService(PersonRepository repository, PersonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public PersonResponse create(PersonCreateRequest request) {
        String cpf = normalizeCpf(request.cpf());
        ensureCpfAvailable(cpf, null);
        return mapper.toResponse(saveTranslatingDuplicate(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<PersonResponse> findAll(String name) {
        List<Person> persons = name == null || name.isBlank()
                ? repository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                : repository.findAllByNameContainingIgnoreCaseOrderByNameAsc(name.trim());
        return persons.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PersonResponse findById(UUID id) {
        return mapper.toResponse(getPerson(id));
    }

    @Transactional
    public PersonResponse update(UUID id, PersonUpdateRequest request) {
        Person person = getPerson(id);
        ensureCpfAvailable(normalizeCpf(request.cpf()), id);
        mapper.update(person, request);
        return mapper.toResponse(saveTranslatingDuplicate(person));
    }

    private Person getPerson(UUID id) {
        return repository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
    }

    private void ensureCpfAvailable(String cpf, UUID currentId) {
        if (cpf == null) {
            return;
        }
        repository.findByCpf(cpf).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new DuplicatePersonCpfException(cpf);
            }
        });
    }

    private Person saveTranslatingDuplicate(Person person) {
        try {
            return repository.saveAndFlush(person);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicatePersonCpfException(person.getCpf());
        }
    }

    private String normalizeCpf(String cpf) {
        return cpf == null || cpf.isBlank() ? null : cpf.trim();
    }
}

package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Person;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    Optional<Person> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Person> findAllByNameContainingIgnoreCaseOrderByNameAsc(String name);
}

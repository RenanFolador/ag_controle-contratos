package com.organization.contractmanager.exception;

import java.util.UUID;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(UUID id) {
        super("Person not found: " + id);
    }
}

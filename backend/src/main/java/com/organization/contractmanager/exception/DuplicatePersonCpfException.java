package com.organization.contractmanager.exception;

public class DuplicatePersonCpfException extends RuntimeException {

    public DuplicatePersonCpfException(String cpf) {
        super("Person CPF already exists: " + cpf);
    }
}

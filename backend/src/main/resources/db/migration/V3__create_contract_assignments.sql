CREATE TABLE contract_assignments (
    id UUID NOT NULL,
    contract_id UUID NOT NULL,
    person_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    CONSTRAINT pk_contract_assignments PRIMARY KEY (id),
    CONSTRAINT fk_contract_assignments_contract
        FOREIGN KEY (contract_id) REFERENCES contracts (id),
    CONSTRAINT fk_contract_assignments_person
        FOREIGN KEY (person_id) REFERENCES persons (id),
    CONSTRAINT ck_contract_assignments_dates
        CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT ck_contract_assignments_role
        CHECK (role IN ('MANAGER', 'PRIMARY_INSPECTOR', 'SUBSTITUTE_INSPECTOR'))
);

CREATE INDEX idx_contract_assignments_contract_id
    ON contract_assignments (contract_id);
CREATE INDEX idx_contract_assignments_person_id
    ON contract_assignments (person_id);
CREATE INDEX idx_contract_assignments_contract_active
    ON contract_assignments (contract_id, active);
CREATE INDEX idx_contract_assignments_person_active
    ON contract_assignments (person_id, active);

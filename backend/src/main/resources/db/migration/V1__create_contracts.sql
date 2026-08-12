CREATE TABLE contracts (
    id UUID NOT NULL,
    contract_number VARCHAR(100) NOT NULL,
    process_number VARCHAR(100),
    object TEXT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_cnpj VARCHAR(18),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    initial_value NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT pk_contracts PRIMARY KEY (id),
    CONSTRAINT uk_contracts_contract_number UNIQUE (contract_number),
    CONSTRAINT ck_contracts_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_contracts_initial_value CHECK (initial_value >= 0),
    CONSTRAINT ck_contracts_status CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED', 'SUSPENDED'))
);

CREATE INDEX idx_contracts_end_date ON contracts (end_date);
CREATE INDEX idx_contracts_status ON contracts (status);
CREATE INDEX idx_contracts_company_name ON contracts (company_name);

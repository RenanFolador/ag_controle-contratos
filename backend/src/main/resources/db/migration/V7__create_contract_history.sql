CREATE TABLE contract_history (
    id UUID NOT NULL,
    contract_id UUID NOT NULL,
    actor VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    CONSTRAINT pk_contract_history PRIMARY KEY (id),
    CONSTRAINT fk_contract_history_contract
        FOREIGN KEY (contract_id) REFERENCES contracts (id),
    CONSTRAINT ck_contract_history_action CHECK (action IN (
        'CREATE_CONTRACT', 'UPDATE_CONTRACT', 'CHANGE_EXPIRATION_DATE',
        'CLOSE_CONTRACT', 'CANCEL_CONTRACT', 'ADD_ASSIGNMENT',
        'REMOVE_ASSIGNMENT', 'NOTIFICATION_SENT', 'NOTIFICATION_FAILED'))
);

CREATE INDEX idx_contract_history_contract_occurred_at
    ON contract_history (contract_id, occurred_at);

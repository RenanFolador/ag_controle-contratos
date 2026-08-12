CREATE TABLE notifications (
    id UUID NOT NULL,
    contract_id UUID NOT NULL,
    person_id UUID NOT NULL,
    expiration_date DATE NOT NULL,
    days_before INTEGER NOT NULL,
    scheduled_date DATE NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(2000),
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_contract
        FOREIGN KEY (contract_id) REFERENCES contracts (id),
    CONSTRAINT fk_notifications_person
        FOREIGN KEY (person_id) REFERENCES persons (id),
    CONSTRAINT uk_notifications_contract_person_expiration_deadline_channel
        UNIQUE (contract_id, person_id, expiration_date, days_before, channel),
    CONSTRAINT ck_notifications_days_before_positive CHECK (days_before > 0),
    CONSTRAINT ck_notifications_retry_count_non_negative CHECK (retry_count >= 0),
    CONSTRAINT ck_notifications_channel CHECK (channel IN ('EMAIL', 'WHATSAPP')),
    CONSTRAINT ck_notifications_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_notifications_status_created_at
    ON notifications (status, created_at);
CREATE INDEX idx_notifications_contract_id ON notifications (contract_id);
CREATE INDEX idx_notifications_person_id ON notifications (person_id);

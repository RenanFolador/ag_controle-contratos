CREATE TABLE notification_schedules (
    id UUID NOT NULL,
    contract_id UUID NOT NULL,
    expiration_date DATE NOT NULL,
    days_before INTEGER NOT NULL,
    scheduled_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_notification_schedules PRIMARY KEY (id),
    CONSTRAINT fk_notification_schedules_contract
        FOREIGN KEY (contract_id) REFERENCES contracts (id),
    CONSTRAINT uk_notification_schedules_contract_expiration_deadline
        UNIQUE (contract_id, expiration_date, days_before),
    CONSTRAINT ck_notification_schedules_days_before_positive
        CHECK (days_before > 0),
    CONSTRAINT ck_notification_schedules_scheduled_date
        CHECK (scheduled_date = expiration_date - days_before),
    CONSTRAINT ck_notification_schedules_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'CANCELLED', 'FAILED'))
);

CREATE INDEX idx_notification_schedules_status_scheduled_date
    ON notification_schedules (status, scheduled_date);
CREATE INDEX idx_notification_schedules_contract_id
    ON notification_schedules (contract_id);
CREATE INDEX idx_notification_schedules_expiration_date
    ON notification_schedules (expiration_date);

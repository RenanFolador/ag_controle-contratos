CREATE TABLE notification_deadlines (
    id UUID NOT NULL,
    days_before INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_notification_deadlines PRIMARY KEY (id),
    CONSTRAINT uk_notification_deadlines_days_before UNIQUE (days_before),
    CONSTRAINT ck_notification_deadlines_days_before_positive CHECK (days_before > 0)
);

INSERT INTO notification_deadlines (
    id, days_before, enabled, created_at, updated_at
) VALUES
    ('00000000-0000-0000-0000-000000000060', 60, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000030', 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000015', 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

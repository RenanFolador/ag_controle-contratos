CREATE INDEX idx_contracts_status_end_date
    ON contracts (status, end_date);

CREATE INDEX idx_notifications_status_sent_at
    ON notifications (status, sent_at);

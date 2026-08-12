CREATE TABLE persons (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14),
    registration VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(30),
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_persons PRIMARY KEY (id),
    CONSTRAINT uk_persons_cpf UNIQUE (cpf)
);

CREATE INDEX idx_persons_name ON persons (name);
CREATE INDEX idx_persons_email ON persons (email);

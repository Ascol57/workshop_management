CREATE TABLE IF NOT EXISTS user_invitation (
    id INT NOT NULL AUTO_INCREMENT,
    token_hash CHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MANAGER', 'OUVRIER', 'ADMINISTRATEUR')),
    workshop_id INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_invitation_token UNIQUE (token_hash),
    CONSTRAINT fk_invitation_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

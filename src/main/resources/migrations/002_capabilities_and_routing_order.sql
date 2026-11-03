CREATE TABLE IF NOT EXISTS capability (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_capability_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS machine_capability (
    machine_id INT NOT NULL,
    capability_id INT NOT NULL,
    PRIMARY KEY (machine_id, capability_id),
    CONSTRAINT fk_machine_capability_machine FOREIGN KEY (machine_id)
        REFERENCES machine (id) ON DELETE CASCADE,
    CONSTRAINT fk_machine_capability_capability FOREIGN KEY (capability_id)
        REFERENCES capability (id) ON DELETE CASCADE
) ENGINE = InnoDB;

ALTER TABLE operation
    ADD COLUMN capability_id INT NULL,
    ADD COLUMN machine_id INT NULL,
    ADD COLUMN sequence_order INT NOT NULL DEFAULT 1,
    ADD CONSTRAINT fk_operation_capability FOREIGN KEY (capability_id)
        REFERENCES capability (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_operation_machine FOREIGN KEY (machine_id)
        REFERENCES machine (id) ON DELETE RESTRICT;

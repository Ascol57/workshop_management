CREATE TABLE IF NOT EXISTS storage_location (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    workshop_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_storage_location_name UNIQUE (workshop_id, name),
    CONSTRAINT fk_storage_location_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS piece (
    id INT NOT NULL AUTO_INCREMENT,
    piece_ref VARCHAR(100) NOT NULL,
    product_id INT NOT NULL,
    storage_location_id INT NULL,
    routing_id INT NULL,
    current_operation_id INT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'IN_TRANSFORMATION', 'COMPLETED')),
    PRIMARY KEY (id),
    CONSTRAINT uq_piece_ref UNIQUE (piece_ref),
    CONSTRAINT fk_piece_product FOREIGN KEY (product_id)
        REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT fk_piece_storage_location FOREIGN KEY (storage_location_id)
        REFERENCES storage_location (id) ON DELETE RESTRICT,
    CONSTRAINT fk_piece_routing FOREIGN KEY (routing_id)
        REFERENCES routing (id) ON DELETE RESTRICT,
    CONSTRAINT fk_piece_current_operation FOREIGN KEY (current_operation_id)
        REFERENCES operation (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

ALTER TABLE task
    ADD COLUMN piece_id INT NULL,
    ADD CONSTRAINT fk_task_piece FOREIGN KEY (piece_id)
        REFERENCES piece (id) ON DELETE RESTRICT;

CREATE INDEX idx_storage_location_workshop ON storage_location (workshop_id);
CREATE INDEX idx_piece_product ON piece (product_id);
CREATE INDEX idx_piece_storage_location ON piece (storage_location_id);
CREATE INDEX idx_piece_current_operation ON piece (current_operation_id);
CREATE INDEX idx_task_piece ON task (piece_id);

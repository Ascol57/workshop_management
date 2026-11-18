CREATE TABLE IF NOT EXISTS storage_capacity (
    storage_location_id INT NOT NULL,
    product_id INT NOT NULL,
    max_quantity INT NOT NULL CHECK (max_quantity >= 0),
    PRIMARY KEY (storage_location_id, product_id),
    CONSTRAINT fk_storage_capacity_location FOREIGN KEY (storage_location_id)
        REFERENCES storage_location (id) ON DELETE CASCADE,
    CONSTRAINT fk_storage_capacity_product FOREIGN KEY (product_id)
        REFERENCES product (id) ON DELETE CASCADE
) ENGINE = InnoDB;

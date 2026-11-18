ALTER TABLE operation
    ADD COLUMN required_product_id INT NULL,
    ADD CONSTRAINT fk_operation_required_product FOREIGN KEY (required_product_id)
        REFERENCES product (id) ON DELETE RESTRICT;

UPDATE operation o
JOIN routing r ON r.id = o.routing_id
SET o.required_product_id = r.product_id
WHERE o.required_product_id IS NULL;

ALTER TABLE general_goal
    ADD COLUMN product_id INT NULL,
    ADD CONSTRAINT fk_general_goal_product FOREIGN KEY (product_id)
        REFERENCES product (id) ON DELETE RESTRICT;

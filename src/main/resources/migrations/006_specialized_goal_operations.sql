ALTER TABLE specialized_goal
    ADD COLUMN operation_id INT NULL,
    ADD CONSTRAINT fk_specialized_goal_operation FOREIGN KEY (operation_id)
        REFERENCES operation (id) ON DELETE RESTRICT;

INSERT INTO specialized_goal (step, status, general_goal_id, operation_id)
SELECT op.description, 'IN_PROGRESS', g.id, op.id
FROM general_goal g
JOIN routing r ON r.product_id = g.product_id
JOIN operation op ON op.routing_id = r.id
LEFT JOIN specialized_goal existing
    ON existing.general_goal_id = g.id
    AND existing.operation_id = op.id
WHERE existing.id IS NULL;

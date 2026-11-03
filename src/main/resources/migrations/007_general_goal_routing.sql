ALTER TABLE general_goal
    ADD COLUMN routing_id INT NULL,
    ADD CONSTRAINT fk_general_goal_routing FOREIGN KEY (routing_id)
        REFERENCES routing (id) ON DELETE RESTRICT;

UPDATE general_goal g
JOIN (
    SELECT product_id, MIN(id) AS routing_id
    FROM routing
    GROUP BY product_id
) selected_routing ON selected_routing.product_id = g.product_id
SET g.routing_id = selected_routing.routing_id
WHERE g.routing_id IS NULL;

DELETE sg
FROM specialized_goal sg
JOIN general_goal g ON g.id = sg.general_goal_id
JOIN operation op ON op.id = sg.operation_id
WHERE g.routing_id IS NOT NULL
  AND op.routing_id <> g.routing_id;

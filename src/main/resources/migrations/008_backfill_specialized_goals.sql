INSERT INTO specialized_goal (step, status, general_goal_id, operation_id)
SELECT op.description, 'IN_PROGRESS', g.id, op.id
FROM general_goal g
JOIN routing r ON r.id = g.routing_id
JOIN operation op ON op.routing_id = r.id
LEFT JOIN specialized_goal existing
    ON existing.general_goal_id = g.id
    AND existing.operation_id = op.id
WHERE existing.id IS NULL;

package fr.insa.crusche.repository;

import fr.ascol.database.DatabaseManager;
import java.util.List;
import java.util.Map;

public class TaskRepository {

    public List<Map<String, Object>> findAll() {
        return DatabaseManager.query("SELECT * FROM task ORDER BY id");
    }

    public List<Map<String, Object>> findBySpecializedGoal(int specializedGoalId) {
        return DatabaseManager.query(
            "SELECT * FROM task WHERE specialized_goal_id = ? ORDER BY id", specializedGoalId);
    }

    public int insert(String taskId, java.sql.Timestamp startDate, java.sql.Timestamp endDate,
                       String status, int operatorId, int machineId,
                       int operationId, int specializedGoalId) {
        return DatabaseManager.update(
            "INSERT INTO task (id_task, debut, fin, statut, operator_id, machine_id, operation_id, specialized_goal_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            taskId, startDate, endDate, status, operatorId, machineId, operationId, specializedGoalId);
    }

    public int updateStatus(int id, String status) {
        return DatabaseManager.update("UPDATE task SET statut = ? WHERE id = ?", status, id);
    }

    public int closeTask(int id, java.sql.Timestamp endDate) {
        return DatabaseManager.update("UPDATE task SET fin = ?, statut = 'TERMINEE' WHERE id = ?", endDate, id);
    }

    public int delete(int id) {
        return DatabaseManager.update("DELETE FROM task WHERE id = ?", id);
    }
}
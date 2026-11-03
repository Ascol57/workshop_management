package fr.insa.crusche.repository;

import fr.ascol.database.DatabaseManager;
import java.util.List;
import java.util.Map;

public class SpecializedGoalRepository {

    public List<Map<String, Object>> findByGeneralGoal(int generalGoalId) {
        return DatabaseManager.query(
            "SELECT * FROM specialized_goal WHERE general_goal_id = ? ORDER BY id", generalGoalId);
    }

    public int insert(String step, String status, int generalGoalId) {
        return DatabaseManager.update(
            "INSERT INTO specialized_goal (etape, statut, general_goal_id) VALUES (?, ?, ?)",
            step, status, generalGoalId);
    }

    public int update(int id, String step, String status) {
        return DatabaseManager.update(
            "UPDATE specialized_goal SET etape = ?, statut = ? WHERE id = ?",
            step, status, id);
    }

    public int delete(int id) {
        return DatabaseManager.update("DELETE FROM specialized_goal WHERE id = ?", id);
    }
}
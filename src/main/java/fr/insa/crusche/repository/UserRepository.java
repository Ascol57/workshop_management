package fr.insa.crusche.repository;

import fr.ascol.database.DatabaseManager;
import java.util.List;
import java.util.Map;

public class UserRepository {

    public List<Map<String, Object>> findAll() {
        return DatabaseManager.query("SELECT id, name, first_name, login, role FROM app_user");
    }

    public List<Map<String, Object>> findByLogin(String login) {
        return DatabaseManager.query("SELECT * FROM app_user WHERE login = ?", login);
    }

    public int insert(String name, String firstName, String login, String passwordHash, String role, int workshopId) {
        return DatabaseManager.update(
            "INSERT INTO app_user (name, first_name, login, password_hash, role, workshop_id) VALUES (?, ?, ?, ?, ?, ?)",
            name, firstName, login, passwordHash, role, workshopId);
    }

    public int updateProfile(int id, String name, String firstName) {
        return DatabaseManager.update(
            "UPDATE app_user SET name = ?, first_name = ? WHERE id = ?",
            name, firstName, id);
    }

    public int updatePassword(int id, String hash) {
        return DatabaseManager.update("UPDATE app_user SET password_hash = ? WHERE id = ?", hash, id);
    }
}
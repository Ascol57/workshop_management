package fr.insa.crusche.webui;

import com.vaadin.flow.server.VaadinSession;
import fr.ascol.database.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Map;
import java.util.Optional;

public class SessionService {

    private static final String USER_KEY = "currentUser";

    public static SessionService getInstance() {
        VaadinSession session = VaadinSession.getCurrent();
        SessionService service = (SessionService) session.getAttribute(SessionService.class);
        if (service == null) {
            service = new SessionService();
            session.setAttribute(SessionService.class, service);
        }
        return service;
    }

    public boolean isLoggedIn() {
        return VaadinSession.getCurrent().getAttribute(USER_KEY) != null;
    }

    public void login(int userId, String login, String nom, String prenom, String role) {
        UserSession user = new UserSession(userId, login, nom, prenom, role);
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute(USER_KEY, null);
    }

    public Optional<UserSession> getCurrentUser() {
        return Optional.ofNullable((UserSession) VaadinSession.getCurrent().getAttribute(USER_KEY));
    }

    public int getCurrentUserId() {
        return getCurrentUser().map(u -> u.id).orElse(-1);
    }

    public String getCurrentUserLogin() {
        return getCurrentUser().map(u -> u.login).orElse("");
    }

    public String getCurrentUserNom() {
        return getCurrentUser().map(u -> u.nom).orElse("");
    }

    public String getCurrentUserPrenom() {
        return getCurrentUser().map(u -> u.prenom).orElse("");
    }

    public String getCurrentUserRole() {
        return getCurrentUser().map(u -> u.role).orElse("OUVRIER");
    }

    public boolean isAdmin() {
        return "ADMINISTRATEUR".equals(getCurrentUserRole());
    }

    public boolean canManageWorkshop() {
        return isAdmin() || "MANAGER".equals(getCurrentUserRole());
    }

    public boolean isClub() {
        return "CLUB".equals(getCurrentUserRole());
    }

    /**
     * Authentifie un utilisateur par login/mot de passe.
     * Retourne l'ID utilisateur si succès, -1 sinon.
     */
    public int authenticate(String login, String motDePasse) {
        String sql = "SELECT id, password_hash FROM app_user WHERE login = ?";
        var results = DatabaseManager.query(sql, login);

        if (results.isEmpty()) {
            return -1;
        }

        Map<String, Object> row = results.get(0);
        String storedHash = (String) row.get("password_hash");
        // Vérification BCrypt
        if (BCrypt.checkpw(motDePasse, storedHash)) {
            return ((Number) row.get("id")).intValue();
        }
        return -1;
    }

    /**
     * Inscription d'un nouveau joueur.
     */
    public boolean register(String login, String motDePasse, String nom, String prenom, 
                            String email, String sexe, String categorie) {
        // Vérifier si login existe
        String checkSql = "SELECT COUNT(*) as cnt FROM app_user WHERE login = ?";
        var check = DatabaseManager.query(checkSql, login);
        if (!check.isEmpty() && ((Number) check.get(0).get("cnt")).intValue() > 0) {
            return false;
        }

        // Hash du mot de passe avec BCrypt
        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

        String sql = """
            INSERT INTO app_user (name, first_name, login, password_hash, role, workshop_id)
            VALUES (?, ?, ?, ?, 'OUVRIER', ?)
            """;
        int rows = DatabaseManager.update(sql, nom, prenom, login, hash, 1); // TODO: get workshop ID from context
        return rows > 0;
    }

    public static class UserSession {
        public final int id;
        public final String login;
        public final String nom;
        public final String prenom;
        public final String role;

        public UserSession(int id, String login, String nom, String prenom, String role) {
            this.id = id;
            this.login = login;
            this.nom = nom;
            this.prenom = prenom;
            this.role = role;
        }
    }
}
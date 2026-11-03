package fr.insa.crusche.webui;

import fr.ascol.database.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class InvitationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private InvitationService() {
    }

    public static String create(String email, String login, String role, int workshopId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        DatabaseManager.update("""
            INSERT INTO user_invitation
                (token_hash, email, login, role, workshop_id, expires_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """, hash(token), email, login, role, workshopId,
            Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));
        return token;
    }

    public static Map<String, Object> findValid(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        List<Map<String, Object>> rows = DatabaseManager.query("""
            SELECT id, email, login, role, workshop_id
            FROM user_invitation
            WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP
            """, hash(token));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static void complete(String token, String name, String firstName, String password) {
        Map<String, Object> invitation = findValid(token);
        if (invitation == null) {
            throw new IllegalArgumentException("Invitation invalide ou expirée");
        }
        DatabaseManager.update("""
            INSERT INTO app_user (name, first_name, login, password_hash, role, workshop_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """, name, firstName, invitation.get("login"),
            BCrypt.hashpw(password, BCrypt.gensalt()), invitation.get("role"), invitation.get("workshop_id"));
        DatabaseManager.update("UPDATE user_invitation SET used_at = CURRENT_TIMESTAMP WHERE id = ?",
            invitation.get("id"));
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de sécuriser le token d'invitation", e);
        }
    }
}

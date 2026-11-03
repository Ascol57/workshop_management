package fr.ascol.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.ascol.utils.Env;

public final class DatabaseManager {

    /** l'unique base de l'application, {@code null} tant qu'elle n'a pas servi. */
    private static MySQLDatabase database;

    /** classe utilitaire : pas d'instance. */
    private DatabaseManager() {
    }

    /**
     * L'unique base de l'application, créée au premier appel.
     *
     * @throws Error si la configuration est incomplète (voir {@code .env.example})
     */
    public static synchronized MySQLDatabase database() {
        if (database == null) {
            database = creerDepuisConfiguration();
        }
        return database;
    }

    /**
     * Une connexion à la base, à fermer par l'appelant — idéalement avec un
     * try-with-resources. C'est le raccourci attendu dans la plupart des cas.
     *
     * @throws SQLException si la connexion ne peut pas être établie
     */
    public static Connection getConnection() throws SQLException {
        return database().getConnection();
    }

    /**
     * Ferme la base et oublie l'instance : le prochain appel en recréera une.
     *
     * <p>
     * Utile à l'arrêt de l'application, ou pour repartir d'une configuration
     * modifiée. En usage normal il n'y a pas à l'appeler.
     * </p>
     */
    public static synchronized void close() {
        if (database != null) {
            database.close();
            database = null;
        }
    }

    /**
     * Construit la base à partir de la configuration.
     *
     * <p>
     * {@code DB_USE_POOL} permet de passer en connexions directes
     * ({@code DB_USE_POOL=false}) ; par défaut on utilise un pool, qui convient
     * aussi bien à l'application web qu'aux menus console.
     * </p>
     */
    private static MySQLDatabase creerDepuisConfiguration() {
        String base = Env.require("DB_NAME");
        String url = "jdbc:mysql://" + Env.get("DB_HOST", "92.222.25.165")
                + ":" + Env.getInt("DB_PORT", 3306) + "/" + base;
        return new MySQLDatabase(
                url,
                // base de donnée et utilisateur ont le même nom sur le serveur
                // du module M3 : DB_USER n'est à renseigner que s'ils diffèrent
                Env.get("DB_USER", base),
                Env.require("DB_PASSWORD"),
                Boolean.parseBoolean(Env.get("DB_USE_POOL", "true")));
    }

    /**
     * LECTURE : Exécute une requête SQL SELECT (PreparedStatement)
     * 
     * @param sql La requête SQL avec des '?' pour les paramètres
     * @param parametres Les valeurs à injecter à la place des '?'
     * @return Une liste contenant les lignes de résultat sous forme de Map
     */
    public static List<Map<String, Object>> query(String sql, Object... parametres) {
        List<Map<String, Object>> resultats = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            if (parametres != null) {
                for (int i = 0; i < parametres.length; i++) {
                    pst.setObject(i + 1, parametres[i]);
                }
            }

            try (ResultSet rs = pst.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int nombreColonnes = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> ligne = new HashMap<>();
                    for (int i = 1; i <= nombreColonnes; i++) {
                        ligne.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    resultats.add(ligne);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution de la requête SQL (SELECT) : " + sql, e);
        }

        return resultats;
    }

    /**
     * ÉCRITURE : Exécute une requête SQL de modification de données (INSERT, UPDATE, DELETE).
     * 
     * @param sql La requête SQL avec des '?' pour les paramètres
     * @param parametres Les valeurs à injecter à la place des '?'
     * @return Le nombre de lignes modifiées (ex: 1 si un joueur est ajouté)
     */
    public static int update(String sql, Object... parametres) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            if (parametres != null) {
                for (int i = 0; i < parametres.length; i++) {
                    pst.setObject(i + 1, parametres[i]);
                }

            }

            return pst.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification SQL (INSERT/UPDATE/DELETE) : " + sql, e);
        }

    }

    public static int updateAndGetGeneratedId(String sql, Object... parametres) {
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (parametres != null) {
                for (int i = 0; i < parametres.length; i++) {
                    pst.setObject(i + 1, parametres[i]);
                }
            }
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RuntimeException("Aucun identifiant généré par la base de données");
                }
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion SQL : " + sql, e);
        }
    }

    /**
     * STRUCTURE : Exécute une requête SQL de définition (CREATE TABLE, DROP TABLE, ALTER).
     * 
     * @param sql La requête SQL avec des '?' pour les paramètres (rarement utile pour le DDL mais possible)
     * @param parametres Les valeurs à injecter à la place des '?'
     */
    public static void execute(String sql, Object... parametres) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            if (parametres != null) {
                for (int i = 0; i < parametres.length; i++) {
                    pst.setObject(i + 1, parametres[i]);
                }
            }

            pst.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution SQL (CREATE/DROP) : " + sql, e);
        }
    }

    /**
     * Applique toutes les migrations SQL non encore appliquées.
     * 
     * <p>
     * Les migrations sont des fichiers <code>.sql</code> situés dans
     * <code>src/main/resources/migrations/</code> (classpath <code>/migrations/</code>).
     * Ils doivent être nommés avec un préfixe numérique ordonnant leur exécution
     * (ex: <code>000_init.sql</code>, <code>001_ajout_table.sql</code>, …).
     * </p>
     * 
     * <p>
     * Une table <code>schema_version</code> est créée automatiquement au premier
     * appel pour mémoriser les migrations déjà appliquées.
     * </p>
     * 
     * @throws RuntimeException si une migration échoue
     */
    public static void migrer() {
        // 1. S'assurer que la table de suivi existe
        creerTableSuiviSiAbsente();

        // 2. Lister les fichiers de migration disponibles sur le classpath
        List<String> fichiersMigration = listerFichiersMigration();

        // 3. Récupérer les versions déjà appliquées
        List<String> versionsAppliquees = getVersionsAppliquees();

        // 4. Appliquer les migrations manquantes dans l'ordre
        for (String fichier : fichiersMigration) {
            String version = extraireVersion(fichier);
            if (!versionsAppliquees.contains(version)) {
                appliquerMigration(fichier, version);
                versionsAppliquees.add(version); // mise à jour locale pour la boucle
            }
        }
    }

    /**
     * Supprime toutes les tables de la base courante puis réapplique les migrations.
     * Cette opération est destructive et doit être déclenchée explicitement par un
     * administrateur.
     */
    public static synchronized void clearDatabaseAndMigrate() {
        try (Connection conn = getConnection();
             PreparedStatement listTables = conn.prepareStatement(
                 "SELECT table_name FROM information_schema.tables "
                     + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'");
             PreparedStatement disableForeignKeys = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0");
             PreparedStatement enableForeignKeys = conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 1")) {

            disableForeignKeys.execute();
            try (ResultSet tables = listTables.executeQuery()) {
                List<String> tableNames = new ArrayList<>();
                while (tables.next()) {
                    tableNames.add(tables.getString("table_name"));
                }

                for (String tableName : tableNames) {
                    String quotedTableName = "`" + tableName.replace("`", "``") + "`";
                    try (PreparedStatement dropTable = conn.prepareStatement(
                            "DROP TABLE IF EXISTS " + quotedTableName)) {
                        dropTable.execute();
                    }
                }
            } finally {
                enableForeignKeys.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la réinitialisation de la base de données", e);
        }

        migrer();
    }

    /**
     * Crée la table {@code schema_version} si elle n'existe pas encore.
     */
    private static void creerTableSuiviSiAbsente() {
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version VARCHAR(50) PRIMARY KEY,
                    fichier VARCHAR(255) NOT NULL,
                    appliquee_le TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE = InnoDB
                """;
        execute(sql);
    }

    /**
     * Liste tous les fichiers <code>.sql</code> dans <code>/migrations/</code>
     * sur le classpath, triés par nom (ordre d'exécution).
     */
    private static List<String> listerFichiersMigration() {
        // On scanne via l'URL du classpath
        var url = DatabaseManager.class.getClassLoader().getResource("migrations");
        if (url == null) {
            return List.of(); // pas de dossier migrations
        }
        
        try {
            if ("file".equals(url.getProtocol())) {
                // En mode développement (fichiers sur disque)
                var dir = java.nio.file.Paths.get(url.toURI());
                return java.nio.file.Files.list(dir)
                        .filter(p -> p.getFileName().toString().endsWith(".sql"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .collect(Collectors.toList());
            } else if ("jar".equals(url.getProtocol())) {
                // Dans un JAR : on parse l'URL jar:file:...!/migrations
                return listerDepuisJar(url);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur scan migrations : " + url, e);
        }
        return List.of();
    }

    /**
     * Liste les fichiers .sql dans un JAR (url protocole jar).
     */
    private static List<String> listerDepuisJar(java.net.URL urlMigrations) {
        try {
            // urlMigrations = jar:file:.../app.jar!/migrations
            String urlStr = urlMigrations.toString();
            // Extraire le chemin du JAR
            int idx = urlStr.indexOf("!/");
            String jarPath = urlStr.substring(4, idx); // retire "jar:" et tout après "!/"
            java.net.URI jarUri = new java.net.URI(jarPath);
            try (var fs = java.nio.file.FileSystems.newFileSystem(jarUri, Map.of())) {
                var path = fs.getPath("/migrations");
                return java.nio.file.Files.list(path)
                        .filter(p -> p.getFileName().toString().endsWith(".sql"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture migrations dans JAR", e);
        }
    }

    /**
     * Récupère la liste des versions déjà appliquées depuis la table de suivi.
     */
    private static List<String> getVersionsAppliquees() {
        String sql = "SELECT version FROM schema_version ORDER BY version";
        return query(sql).stream()
                .map(row -> (String) row.get("version"))
                .collect(Collectors.toList());
    }

    /**
     * Extrait le numéro de version du nom de fichier (ex: "001_foo.sql" -> "001").
     */
    private static String extraireVersion(String nomFichier) {
        int underscore = nomFichier.indexOf('_');
        if (underscore > 0) {
            return nomFichier.substring(0, underscore);
        }
        // Si pas de _, on prend tout avant .sql
        int dot = nomFichier.indexOf('.');
        return dot > 0 ? nomFichier.substring(0, dot) : nomFichier;
    }

    /**
     * Lit et exécute le contenu d'un fichier de migration.
     */
    private static void appliquerMigration(String nomFichier, String version) {
        String chemin = "migrations/" + nomFichier;
        String sql;
        try (InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream(chemin)) {
            if (is == null) {
                throw new RuntimeException("Fichier migration introuvable sur le classpath : " + chemin);
            }
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture migration " + nomFichier, e);
        }

        // Exécuter le script SQL (peut contenir plusieurs statements séparés par ;)
        executerScriptSql(sql);

        // Enregistrer la version comme appliquée
        String insertSql = "INSERT INTO schema_version (version, fichier) VALUES (?, ?)";
        update(insertSql, version, nomFichier);
    }

    /**
     * Exécute un script SQL complet (plusieurs statements séparés par ;).
     * Gère les blocs DECLARE/DELIMITER simples et ignore les commentaires.
     */
    private static void executerScriptSql(String script) {
        // Séparer par ; qui ne sont pas dans des guillemets ou commentaires
        // Approche simple : split sur ; suivi de newline, puis exécuter chaque statement non vide
        String[] statements = script.split(";(?=\\s*(?:--|/\\*|$|\\n))");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                while (trimmed.startsWith("--")) {
                    int endOfLine = trimmed.indexOf('\n');
                    trimmed = endOfLine < 0 ? "" : trimmed.substring(endOfLine + 1).trim();
                }
                while (trimmed.startsWith("/*")) {
                    int endOfComment = trimmed.indexOf("*/");
                    trimmed = endOfComment < 0 ? "" : trimmed.substring(endOfComment + 2).trim();
                }
                if (trimmed.isEmpty()) {
                    continue;
                }
                try (PreparedStatement pst = conn.prepareStatement(trimmed)) {
                    pst.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(
                        "Erreur exécution statement dans migration : "
                            + trimmed.substring(0, Math.min(100, trimmed.length()))
                            + "... (" + e.getMessage() + ")",
                        e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion lors migration", e);
        }
    }
}
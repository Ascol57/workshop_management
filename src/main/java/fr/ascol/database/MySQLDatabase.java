package fr.ascol.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Accès à une base de données MySQL, au choix par connexions directes ou par un
 * pool de connexions.
 *
 * <p>
 * Le mode est fixé à la construction :
 * </p>
 * <ul>
 * <li>{@code usePool = false} : chaque appel à {@link #getConnection()} ouvre
 * une nouvelle connexion au serveur. Simple, mais coûteux si on ouvre souvent
 * (une ouverture de connexion MySQL prend quelques dizaines de millisecondes).
 * Convient à un programme console qui garde sa connexion du début à la fin.</li>
 * <li>{@code usePool = true} : les connexions sont recyclées par HikariCP. Les
 * fermer les rend au pool au lieu de les détruire. C'est ce qu'il faut pour une
 * application web, où chaque affichage de page demande une connexion.</li>
 * </ul>
 *
 * <p>
 * Dans les deux cas l'appelant ferme la connexion qu'il a obtenue, idéalement
 * avec un try-with-resources :
 * </p>
 *
 * <pre>
 * try (Connection con = database.getConnection()) {
 *     ...
 * }
 * </pre>
 */
public class MySQLDatabase {

    private final String url;
    private final String username;
    private final String password;

    /** le pool, ou {@code null} si l'on est en mode connexion directe. */
    private final HikariDataSource pool;

    /**
     * @param url      url jdbc complète, par exemple
     *                 {@code jdbc:mysql://92.222.25.165:3306/m3_crusche01}
     * @param username nom d'utilisateur MySQL
     * @param password mot de passe MySQL
     * @param usePool  {@code true} pour passer par un pool de connexions
     */
    public MySQLDatabase(String url, String username, String password, boolean usePool) {
        this.url = url;
        this.username = username;
        this.password = password;
        // le pool est construit une fois pour toutes ; en mode direct il n'y a
        // rien à préparer, chaque connexion est ouverte à la demande
        this.pool = usePool ? PoolConnect() : null;
    }

    /**
     * Une connexion à la base, à fermer par l'appelant.
     *
     * @throws SQLException si la connexion ne peut pas être établie
     */
    public Connection getConnection() throws SQLException {
        return this.pool == null ? SimpleConnect() : this.pool.getConnection();
    }

    /**
     * Ouvre une nouvelle connexion directe, sans passer par un pool.
     */
    private Connection SimpleConnect() throws SQLException {
        // ce chargement explicite du driver ne devrait plus être nécessaire,
        // mais il évite des surprises lorsque l'application est déployée dans
        // un serveur Tomcat
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("driver mysql not found", ex);
        }
        Connection con = DriverManager.getConnection(this.url, this.username, this.password);
        // le plus haut degré d'isolation entre transactions : on privilégie la
        // sûreté sur les performances
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return con;
    }

    /**
     * Construit le pool de connexions HikariCP.
     *
     * @throws IllegalStateException si le pool ne peut pas être créé : c'est
     *                               une erreur de configuration, détectée au
     *                               démarrage plutôt qu'à la première requête
     */
    private HikariDataSource PoolConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("driver mysql not found", ex);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(this.url);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.setMaximumPoolSize(10);
        // mise en cache des requêtes préparées côté client et côté serveur
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setTransactionIsolation("TRANSACTION_SERIALIZABLE");
        return new HikariDataSource(config);
    }

    /**
     * Ferme le pool et toutes ses connexions. Sans effet en mode direct, où
     * chaque connexion est fermée par celui qui l'a demandée.
     */
    public void close() {
        if (this.pool != null) {
            this.pool.close();
        }
    }
}

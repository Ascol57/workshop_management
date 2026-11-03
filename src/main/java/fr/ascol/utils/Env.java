package fr.ascol.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.io.InputStream;

/**
 * Lecture de la configuration sensible (identifiants de base de données…) en
 * dehors du code source.
 *
 * <p>
 * Une valeur est cherchée dans cet ordre, la première trouvée gagne :
 * </p>
 * <ol>
 * <li>les variables d'environnement du système ({@code DB_PASSWORD=… java …},
 * {@code ENV} dans un Dockerfile, variables d'un serveur Tomcat…) ;</li>
 * <li>les propriétés système passées à la JVM ({@code -DDB_PASSWORD=…}) ;</li>
 * <li>le fichier {@code .env} à la racine du projet.</li>
 * </ol>
 *
 * <p>
 * Le fichier {@code .env} n'est pas versionné (voir {@code .gitignore}) : les
 * mots de passe ne partent donc jamais dans le dépôt git. Le modèle à recopier
 * est {@code .env.example}.
 * </p>
 *
 * <p>
 * Le fichier est cherché dans le répertoire courant puis dans ses répertoires
 * parents, ce qui permet de lancer l'application depuis la racine du projet
 * comme depuis un sous-répertoire. On peut aussi désigner explicitement un
 * autre fichier avec {@code -Ddotenv.path=/chemin/vers/le/fichier}.
 * </p>
 *
 * <p>
 * En production (conteneur Docker, serveur Tomcat) il n'y a en général pas de
 * fichier {@code .env} : les valeurs viennent alors des variables
 * d'environnement, sans rien changer au code.
 * </p>
 */
public final class Env {

    /** propriété système permettant de désigner un fichier .env précis. */
    private static final String PROPRIETE_CHEMIN = "dotenv.path";

    private static final String NOM_FICHIER = ".env";

    /** nombre de répertoires parents explorés pour trouver le fichier. */
    private static final int REMONTEE_MAX = 5;

    private static final Map<String, String> VALEURS;

    /** le fichier effectivement lu, ou {@code null} si aucun n'a été trouvé. */
    private static final Path FICHIER_LU;

    static {
        Optional<Path> trouve = chercherFichier();
        FICHIER_LU = trouve.orElse(null);
        VALEURS = trouve.map(Env::lire).orElseGet(Env::lireClasspath);
    }

    /** classe utilitaire : pas d'instance. */
    private Env() {
    }

    /**
     * La valeur associée à une clé, si elle est définie et non vide.
     */
    public static Optional<String> get(String cle) {
        String valeur = System.getenv(cle);
        if (estVide(valeur)) {
            valeur = System.getProperty(cle);
        }
        if (estVide(valeur)) {
            valeur = VALEURS.get(cle);
        }
        return estVide(valeur) ? Optional.empty() : Optional.of(valeur);
    }

    /**
     * La valeur associée à une clé, ou {@code defaut} si elle n'est pas définie.
     */
    public static String get(String cle, String defaut) {
        return get(cle).orElse(defaut);
    }

    /**
     * La valeur associée à une clé, interprétée comme un entier.
     *
     * @throws Error si la valeur est présente mais n'est pas un entier : mieux
     *               vaut un message explicite au démarrage qu'un comportement
     *               inattendu plus tard.
     */
    public static int getInt(String cle, int defaut) {
        Optional<String> valeur = get(cle);
        if (valeur.isEmpty()) {
            return defaut;
        }
        try {
            return Integer.parseInt(valeur.get().strip());
        } catch (NumberFormatException e) {
            throw new Error("la valeur de " + cle + " devrait être un nombre entier,"
                    + " et vaut : \"" + valeur.get() + "\"" + origine(), e);
        }
    }

    /**
     * La valeur associée à une clé obligatoire.
     *
     * @throws Error avec un message expliquant quoi faire si la clé est absente.
     */
    public static String require(String cle) {
        return get(cle).orElseThrow(() -> new Error(messageAbsence(cle)));
    }

    /** Le fichier .env effectivement lu, s'il y en a un. */
    public static Optional<Path> fichierLu() {
        return Optional.ofNullable(FICHIER_LU);
    }

    private static boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }

    private static String origine() {
        return FICHIER_LU == null ? "" : " (fichier " + FICHIER_LU + ")";
    }

    private static String messageAbsence(String cle) {
        if (FICHIER_LU == null) {
            return "configuration manquante : " + cle + "."
                    + " Aucun fichier " + NOM_FICHIER + " n'a été trouvé à partir de "
                    + Path.of("").toAbsolutePath() + "."
                    + " Copiez .env.example en .env à la racine du projet et renseignez vos"
                    + " identifiants, ou définissez la variable d'environnement " + cle + ".";
        }
        return "configuration manquante : " + cle + "."
                + " Le fichier " + FICHIER_LU + " a bien été lu, mais " + cle
                + " y est absent ou vide. Voir .env.example pour les clés attendues.";
    }

    private static Optional<Path> chercherFichier() {
        String impose = System.getProperty(PROPRIETE_CHEMIN);
        if (!estVide(impose)) {
            Path chemin = Path.of(impose);
            if (!Files.isRegularFile(chemin)) {
                throw new Error("le fichier désigné par -D" + PROPRIETE_CHEMIN
                        + " n'existe pas : " + chemin.toAbsolutePath());
            }
            return Optional.of(chemin);
        }
        Path repertoire = Path.of("").toAbsolutePath();
        for (int i = 0; i <= REMONTEE_MAX && repertoire != null; i++) {
            Path candidat = repertoire.resolve(NOM_FICHIER);
            if (Files.isRegularFile(candidat)) {
                return Optional.of(candidat);
            }
            repertoire = repertoire.getParent();
        }
        return Optional.empty();
    }

    private static Map<String, String> lireClasspath() {
        try (InputStream flux = Env.class.getClassLoader().getResourceAsStream(NOM_FICHIER)) {
            return flux == null ? Collections.emptyMap() : lire(flux);
        } catch (IOException e) {
            throw new Error("lecture impossible du fichier " + NOM_FICHIER + " dans le WAR", e);
        }
    }

    private static Map<String, String> lire(Path fichier) {
        Map<String, String> valeurs = new HashMap<>();
        try {
            for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
                String reste = ligne.strip();
                if (reste.isEmpty() || reste.startsWith("#")) {
                    continue;
                }
                if (reste.startsWith("export ")) {
                    reste = reste.substring("export ".length()).strip();
                }
                int separateur = reste.indexOf('=');
                if (separateur <= 0) {
                    continue;
                }
                valeurs.put(reste.substring(0, separateur).strip(),
                        nettoyerValeur(reste.substring(separateur + 1).strip()));
            }
        } catch (IOException e) {
            throw new Error("lecture impossible du fichier " + fichier, e);
        }
        return valeurs;
    }

    private static Map<String, String> lire(InputStream flux) throws IOException {
        Map<String, String> valeurs = new HashMap<>();
        try (var lecteur = new java.io.BufferedReader(
                new java.io.InputStreamReader(flux, StandardCharsets.UTF_8))) {
            for (String ligne; (ligne = lecteur.readLine()) != null;) {
                String reste = ligne.strip();
                if (reste.isEmpty() || reste.startsWith("#")) {
                    continue;
                }
                if (reste.startsWith("export ")) {
                    reste = reste.substring("export ".length()).strip();
                }
                int separateur = reste.indexOf('=');
                if (separateur > 0) {
                    valeurs.put(reste.substring(0, separateur).strip(),
                            nettoyerValeur(reste.substring(separateur + 1).strip()));
                }
            }
        }
        return valeurs;
    }

    /**
     * Enlève les guillemets éventuels autour de la valeur, et le commentaire de
     * fin de ligne pour une valeur qui n'est pas entre guillemets. Un mot de
     * passe contenant un '#' doit donc être écrit entre guillemets.
     */
    private static String nettoyerValeur(String brute) {
        if (brute.length() >= 2
                && ((brute.startsWith("\"") && brute.endsWith("\""))
                        || (brute.startsWith("'") && brute.endsWith("'")))) {
            return brute.substring(1, brute.length() - 1);
        }
        int commentaire = brute.indexOf(" #");
        return commentaire < 0 ? brute : brute.substring(0, commentaire).strip();
    }
}

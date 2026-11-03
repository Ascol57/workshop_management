package fr.ascol.database;

/**
 * Commande de réinitialisation complète de la base de données.
 */
public final class ResetDatabaseCommand {

    private ResetDatabaseCommand() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || !"--confirm-reset".equals(args[0])) {
            System.err.println("Usage : mvnw compile exec:java "
                    + "-Dexec.mainClass=fr.ascol.database.ResetDatabaseCommand "
                    + "-Dexec.args=--confirm-reset");
            System.exit(2);
        }

        try {
            System.out.println("Suppression des tables et réapplication des migrations...");
            DatabaseManager.clearDatabaseAndMigrate();
            System.out.println("Base réinitialisée avec succès.");
        } catch (RuntimeException exception) {
            System.err.println("Échec de la réinitialisation : " + exception.getMessage());
            System.exit(1);
        } finally {
            DatabaseManager.close();
        }
    }
}

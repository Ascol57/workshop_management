package fr.insa.crusche.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.ascol.database.DatabaseManager;

/**
 * Configuration pour l'exécution automatique des migrations de base de données
 * au démarrage de l'application Spring Boot.
 */
@Configuration
public class DatabaseMigrationRunner {

    @Bean
    CommandLineRunner runMigrations() {
        return args -> {
            DatabaseManager.migrer();
        };
    }
}
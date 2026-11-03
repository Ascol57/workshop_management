# Workshop Management

## English

Workshop Management is a Spring Boot and Vaadin application for managing
workshops and production planning.

### Features

- Workshop, equipment, machine, product, capability, and routing management.
- Alternative manufacturing routings for the same product.
- Production orders with automatic decomposition into specialized goals.
- On-demand task requests by operators.
- Automatic machine selection based on capability, operator authorization,
  availability, and hourly cost.
- Operator pause, resume, and task completion workflow.
- Interactive 2D workshop floor plan with movable and resizable machines.
- Invitation-only account creation managed by administrators.
- English and French user interface.

### Requirements

- Java 25
- Maven (or the included Maven Wrapper)
- MySQL

### Configuration

Copy the example environment file and fill in the database credentials:

```bash
cp .env.example .env
```

The application reads `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` (optional),
and `DB_PASSWORD`. Never commit `.env`; it contains local credentials.

### Run the application

```bash
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd spring-boot:run
```

Open <http://localhost:8080>.

Database migrations are applied by the application. The first launch provides
the initial setup page when no user exists.

### Build and package

```bash
./mvnw clean package
java -jar target/*.war
```

### Database reset

The reset command deletes all database tables and reapplies every migration.
It is destructive and requires an explicit confirmation flag:

```bash
./mvnw compile exec:java \
  -Dexec.mainClass=fr.ascol.database.ResetDatabaseCommand \
  -Dexec.args=--confirm-reset
```

Administrators can also apply migrations and reset production goals and tasks
from the Administration section.

### License

This project is distributed under the [MIT License](./LICENSE).

## Français

Workshop Management est une application Spring Boot et Vaadin de gestion
d'ateliers et de planification de production.

### Fonctionnalités

- Gestion des ateliers, équipements, machines, produits, capacités et gammes.
- Plusieurs gammes de fabrication alternatives pour un même produit.
- Ordres de production décomposés automatiquement en objectifs spécialisés.
- Demande de tâches à la demande par les ouvriers.
- Choix automatique de la machine selon la capacité, les autorisations de
  l'opérateur, la disponibilité et le coût horaire.
- Gestion de la pause, de la reprise et de la fin des tâches.
- Plan 2D interactif avec machines déplaçables et redimensionnables.
- Création de comptes par invitation gérée par les administrateurs.
- Interface disponible en anglais et en français.

### Prérequis

- Java 25
- Maven (ou le Maven Wrapper inclus)
- MySQL

### Configuration

Copiez le fichier d'environnement d'exemple et renseignez les identifiants
de la base de données :

```bash
cp .env.example .env
```

L'application utilise `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` (facultatif)
et `DB_PASSWORD`. Ne committez jamais `.env` : il contient des identifiants
locaux.

### Lancer l'application

```bash
./mvnw spring-boot:run
```

Sous Windows :

```bat
mvnw.cmd spring-boot:run
```

Ouvrez <http://localhost:8080>.

Les migrations de base de données sont appliquées par l'application. Au
premier lancement, la page de configuration initiale est affichée lorsqu'il
n'existe encore aucun utilisateur.

### Compiler et empaqueter

```bash
./mvnw clean package
java -jar target/*.war
```

### Réinitialiser la base de données

La commande de réinitialisation supprime toutes les tables puis réapplique
toutes les migrations. Cette opération est destructive et nécessite un
paramètre de confirmation explicite :

```bash
./mvnw compile exec:java \
  -Dexec.mainClass=fr.ascol.database.ResetDatabaseCommand \
  -Dexec.args=--confirm-reset
```

Les administrateurs peuvent également appliquer les migrations et réinitialiser
les objectifs de production et les tâches depuis la section Administration.

### Licence

Ce projet est distribué sous [licence MIT](./LICENSE).

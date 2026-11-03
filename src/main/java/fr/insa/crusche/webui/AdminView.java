package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Administration")
@AnonymousAllowed
public class AdminView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService = SessionService.getInstance();
    private Tabs tabs;
    private VerticalLayout contentArea;
    private Tab workshopsTab;
    private Tab usersTab;
    private Tab migrationsTab;
    private Tab invitationsTab;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || !sessionService.isAdmin()) {
            event.forwardTo("");
        }
    }

    public AdminView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Administration"));
        title.getStyle().set("margin", "0");

        // Tabs for different admin sections
        workshopsTab = new Tab(I18n.t("Ateliers"));
        usersTab = new Tab(I18n.t("Utilisateurs"));
        migrationsTab = new Tab(I18n.t("Migrations BDD"));
        invitationsTab = new Tab(I18n.t("Invitations"));

        tabs = new Tabs(workshopsTab, usersTab, invitationsTab, migrationsTab);
        tabs.setWidthFull();

        contentArea = new VerticalLayout();
        contentArea.setSizeFull();
        contentArea.setPadding(true);
        contentArea.setSpacing(true);

        tabs.addSelectedChangeListener(e -> showTabContent(e.getSelectedTab()));

        // Show first tab by default
        showTabContent(workshopsTab);

        add(title, tabs, contentArea);
        setFlexGrow(1, contentArea);
    }

    private void showTabContent(Tab tab) {
        contentArea.removeAll();
        
        if (tab == workshopsTab) {
            showWorkshopsTab();
        } else if (tab == usersTab) {
            showUsersTab();
        } else if (tab == migrationsTab) {
            showMigrationsTab();
        } else if (tab == invitationsTab) {
            showInvitationsTab();
        }
    }

    private void showInvitationsTab() {
        H3 subtitle = new H3(I18n.t("Inviter un utilisateur"));
        TextField email = new TextField(I18n.t("Adresse e-mail"));
        TextField login = new TextField(I18n.t("Identifiant"));
        ComboBox<String> role = new ComboBox<>(I18n.t("Rôle"));
        role.setItems("MANAGER", "OUVRIER", "ADMINISTRATEUR");
        role.setValue("OUVRIER");
        ComboBox<Map<String, Object>> workshop = new ComboBox<>(I18n.t("Atelier"));
        List<Map<String, Object>> workshops = DatabaseManager.query(
            "SELECT id, name FROM workshop ORDER BY name");
        workshop.setItems(workshops);
        workshop.setItemLabelGenerator(row -> String.valueOf(row.get("name")));
        Button create = new Button(I18n.t("Générer le lien d'invitation"), event -> {
            if (email.getValue().isBlank() || login.getValue().isBlank()
                    || role.getValue() == null || workshop.getValue() == null) {
                showError(I18n.t("Informations invalides"));
                return;
            }
            String token = InvitationService.create(email.getValue().trim(), login.getValue().trim(),
                role.getValue(), ((Number) workshop.getValue().get("id")).intValue());
            String link = "http://localhost:8080/invitation?token=" + token;
            TextField generatedLink = new TextField(I18n.t("Lien d'invitation"));
            generatedLink.setValue(link);
            generatedLink.setReadOnly(true);
            generatedLink.setWidthFull();
            Notification.show(I18n.t("Lien généré : copiez-le et envoyez-le à la personne invitée"),
                6000, Notification.Position.MIDDLE);
            contentArea.add(generatedLink);
        });
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        contentArea.add(subtitle, email, login, role, workshop, create);
    }

    private void showWorkshopsTab() {
            H3 subtitle = new H3(I18n.t("Gestion des ateliers"));
            Grid<Map<String, Object>> grid = new Grid<>();
            grid.setSizeFull();
            grid.addColumn(row -> row.get("id")).setHeader("ID").setWidth("80px").setFlexGrow(0);
            grid.addColumn(row -> row.get("name")).setHeader("Nom").setAutoWidth(true);
            grid.setItems(DatabaseManager.query("SELECT id, name FROM workshop ORDER BY id"));
            Button resetGoalsButton = new Button(I18n.t("Réinitialiser les objectifs et tâches"),
                e -> showGoalsResetDialog());
            resetGoalsButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            contentArea.add(subtitle, resetGoalsButton, grid);
            contentArea.setFlexGrow(1, grid);
        }

    private void showGoalsResetDialog() {
        com.vaadin.flow.component.dialog.Dialog dialog =
            new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle(I18n.t("Réinitialiser les objectifs et tâches"));
        dialog.add(new com.vaadin.flow.component.html.Paragraph(
            I18n.t("Cette action supprimera tous les objectifs généraux, objectifs spécialisés et tâches.")
                + " " + I18n.t("Les produits, gammes, machines et utilisateurs seront conservés.")));

        TextField confirmation = new TextField(I18n.t("Saisissez RESET pour confirmer"));
        confirmation.setWidthFull();
        Button cancel = new Button(I18n.t("Annuler"), e -> dialog.close());
        Button reset = new Button(I18n.t("Réinitialiser"), e -> {
            if (!"RESET".equals(confirmation.getValue().trim())) {
                showError(I18n.t("Saisissez exactement RESET pour confirmer"));
                return;
            }
            try {
                resetGoalsAndTasks();
                dialog.close();
                Notification.show(I18n.t("Objectifs et tâches réinitialisés"))
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (RuntimeException ex) {
                showError(I18n.t("Impossible de réinitialiser les objectifs et tâches")
                    + " : " + ex.getMessage());
            }
        });
        reset.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.add(new VerticalLayout(confirmation, new HorizontalLayout(cancel, reset)));
        dialog.open();
    }

    private void resetGoalsAndTasks() {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                executeResetStatement(connection, "DELETE FROM task");
                executeResetStatement(connection, "DELETE FROM specialized_goal");
                executeResetStatement(connection, "DELETE FROM general_goal");
                executeResetStatement(connection,
                    "UPDATE machine SET status = 'AVAILABLE' WHERE status = 'OCCUPIED'");
                executeResetStatement(connection,
                    "UPDATE operator SET status = 'AVAILABLE' WHERE status = 'OCCUPIED'");
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erreur SQL pendant la réinitialisation", ex);
        }
    }

    private void executeResetStatement(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private void showUsersTab() {
            H3 subtitle = new H3(I18n.t("Gestion des utilisateurs"));
            Grid<Map<String, Object>> grid = new Grid<>();
            grid.setSizeFull();
            grid.addColumn(row -> row.get("id")).setHeader("ID").setWidth("80px").setFlexGrow(0);
            grid.addColumn(row -> row.get("name")).setHeader("Nom").setAutoWidth(true);
            grid.addColumn(row -> row.get("first_name")).setHeader("Prénom").setAutoWidth(true);
            grid.addColumn(row -> row.get("login")).setHeader("Login").setAutoWidth(true);
            grid.addColumn(row -> row.get("role")).setHeader("Rôle").setAutoWidth(true);
            grid.addColumn(row -> row.get("workshop_id")).setHeader("Atelier").setAutoWidth(true);
            grid.setItems(DatabaseManager.query(
                "SELECT id, name, first_name, login, role, workshop_id FROM app_user ORDER BY id"));
            contentArea.add(subtitle, grid);
            contentArea.setFlexGrow(1, grid);
    }

    private void showTerrainsTab() {
        H3 subtitle = new H3("Gestion des terrains");
        
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(m -> m.get("id")).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(m -> m.get("id_tournoi")).setHeader("Tournoi ID").setWidth("120px").setFlexGrow(0);
        grid.addColumn(m -> m.get("numero")).setHeader("Numéro").setWidth("100px").setFlexGrow(0);
        grid.addColumn(m -> m.get("nom")).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(m -> m.get("plan")).setHeader("Plan").setAutoWidth(true);

        String sql = "SELECT * FROM terrain ORDER BY id_tournoi, numero";
        List<Map<String, Object>> terrains = DatabaseManager.query(sql);
        grid.setItems(terrains);

        HorizontalLayout toolbar = new HorizontalLayout();
        Button addButton = new Button("Ajouter terrain", e -> showTerrainDialog(grid, null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toolbar.add(addButton);

        contentArea.add(subtitle, toolbar, grid);
        contentArea.setFlexGrow(1, grid);
    }

    private void showTerrainDialog(Grid<Map<String, Object>> grid, Map<String, Object> terrain) {
        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(true);
        dialogContent.setSpacing(true);
        dialogContent.setWidth("400px");

        // Load tournois for selector
        List<Map<String, Object>> tournois = DatabaseManager.query("SELECT id, nom FROM tournoi");
        
        com.vaadin.flow.component.combobox.ComboBox<Map<String, Object>> tournoiField = 
            new com.vaadin.flow.component.combobox.ComboBox<>("Tournoi *");
        tournoiField.setItems(tournois);
        tournoiField.setItemLabelGenerator(t -> (String) t.get("nom"));
        tournoiField.setRequired(true);
        tournoiField.setWidthFull();
        if (terrain != null) {
            tournoiField.setValue(tournois.stream()
                .filter(t -> t.get("id").equals(terrain.get("id_tournoi")))
                .findFirst().orElse(null));
        }

        com.vaadin.flow.component.textfield.IntegerField numeroField = 
            new com.vaadin.flow.component.textfield.IntegerField("Numéro *");
        numeroField.setRequired(true);
        numeroField.setValue(terrain != null ? ((Number) terrain.get("numero")).intValue() : 1);

        com.vaadin.flow.component.textfield.TextField nomField = 
            new com.vaadin.flow.component.textfield.TextField("Nom");
        nomField.setValue(terrain != null ? (String) terrain.get("nom") : "");
        nomField.setWidthFull();

        com.vaadin.flow.component.textfield.TextField planField = 
            new com.vaadin.flow.component.textfield.TextField("Plan (chemin image/PDF)");
        planField.setValue(terrain != null ? (String) terrain.get("plan") : "");
        planField.setWidthFull();

        dialogContent.add(tournoiField, numeroField, nomField, planField);

        HorizontalLayout buttons = new HorizontalLayout();
        Button saveButton = new Button("Enregistrer", e -> saveTerrain(grid, terrain, tournoiField, numeroField, nomField, planField));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Annuler", e -> closeDialog());
        buttons.add(saveButton, cancelButton);
        dialogContent.add(buttons);

        openDialog(dialogContent);
    }

    private void saveTerrain(Grid<Map<String, Object>> grid, Map<String, Object> terrain,
                            com.vaadin.flow.component.combobox.ComboBox<Map<String, Object>> tournoiField,
                            com.vaadin.flow.component.textfield.IntegerField numeroField,
                            com.vaadin.flow.component.textfield.TextField nomField,
                            com.vaadin.flow.component.textfield.TextField planField) {
        Map<String, Object> tournoi = tournoiField.getValue();
        Integer numero = numeroField.getValue();
        String nom = nomField.getValue().trim();
        String plan = planField.getValue().trim();

        if (tournoi == null || numero == null) {
            showError("Tournoi et numéro requis");
            return;
        }

        try {
            if (terrain == null) {
                String sql = "INSERT INTO terrain (id_tournoi, numero, nom, plan) VALUES (?, ?, ?, ?)";
                DatabaseManager.update(sql, tournoi.get("id"), numero, nom.isEmpty() ? null : nom, plan.isEmpty() ? null : plan);
                Notification.show("Terrain créé").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                String sql = "UPDATE terrain SET id_tournoi=?, numero=?, nom=?, plan=? WHERE id=?";
                DatabaseManager.update(sql, tournoi.get("id"), numero, nom.isEmpty() ? null : nom, plan.isEmpty() ? null : plan, terrain.get("id"));
                Notification.show("Terrain mis à jour").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            closeDialog();
            showTerrainsTab();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void showInscriptionsTab() {
        H3 subtitle = new H3("Inscriptions aux tournois");
        
        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(m -> m.get("id_tournoi")).setHeader("Tournoi ID").setWidth("120px").setFlexGrow(0);
        grid.addColumn(m -> m.get("id_joueur")).setHeader("Joueur ID").setWidth("120px").setFlexGrow(0);
        // Join to get readable names
        String sql = """
            SELECT i.*, t.nom as tournoi_nom, j.nom as joueur_nom, j.prenom as joueur_prenom, j.login as joueur_login
            FROM inscription i
            JOIN tournoi t ON i.id_tournoi = t.id
            JOIN joueur j ON i.id_joueur = j.id
            ORDER BY t.date_debut DESC, j.nom, j.prenom
            """;
        List<Map<String, Object>> inscriptions = DatabaseManager.query(sql);
        grid.addColumn(m -> m.get("tournoi_nom")).setHeader("Tournoi").setAutoWidth(true);
        grid.addColumn(m -> m.get("joueur_nom")).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(m -> m.get("joueur_prenom")).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(m -> m.get("joueur_login")).setHeader("Login").setAutoWidth(true);
        grid.setItems(inscriptions);

        contentArea.add(subtitle, grid);
        contentArea.setFlexGrow(1, grid);
    }

    private void showMigrationsTab() {
        H3 subtitle = new H3("Migrations de base de données");
        
        VerticalLayout migrationContent = new VerticalLayout();
        migrationContent.setSpacing(true);
        
        com.vaadin.flow.component.html.Paragraph info = new com.vaadin.flow.component.html.Paragraph(
            "Les migrations sont appliquées automatiquement au démarrage de l'application. " +
            "Vous pouvez forcer une nouvelle vérification ici."
        );
        
        Button checkButton = new Button("Vérifier et appliquer les migrations", e -> {
            try {
                DatabaseManager.migrer();
                Notification.show("Migrations vérifiées et appliquées").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshMigrationsTable(migrationContent);
            } catch (Exception ex) {
                showError("Erreur migration : " + ex.getMessage());
            }
        });
        checkButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button resetButton = new Button("Clear DB and redo all migrations",
            e -> showDatabaseResetDialog(migrationContent));
        resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        migrationContent.add(info, checkButton, resetButton);
        
        refreshMigrationsTable(migrationContent);
        
        contentArea.add(subtitle, migrationContent);
    }

    private void showDatabaseResetDialog(VerticalLayout migrationContent) {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setHeaderTitle("Réinitialiser la base de données");
        dialog.setModal(true);

        VerticalLayout content = new VerticalLayout();
        content.add(new com.vaadin.flow.component.html.Paragraph(
            "Cette action supprimera toutes les données et toutes les tables, puis réappliquera les migrations."));

        com.vaadin.flow.component.textfield.TextField confirmationField =
            new com.vaadin.flow.component.textfield.TextField("Saisissez RESET pour confirmer");
        confirmationField.setWidthFull();

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        Button resetButton = new Button("Clear DB and redo all migrations", e -> {
            if (!"RESET".equals(confirmationField.getValue().trim())) {
                showError("Saisissez exactement RESET pour confirmer");
                return;
            }

            try {
                DatabaseManager.clearDatabaseAndMigrate();
                SessionService.getInstance().logout();
                dialog.close();
                Notification.show("Base réinitialisée et migrations réappliquées")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("setup"));
            } catch (Exception ex) {
                showError("Erreur lors de la réinitialisation : " + ex.getMessage());
            }
        });
        resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(cancelButton, resetButton);
        content.add(confirmationField, actions);
        dialog.add(content);
        dialog.open();
    }

    private void refreshMigrationsTable(VerticalLayout container) {
        try {
            List<Map<String, Object>> migrations = DatabaseManager.query(
                "SELECT version, fichier, appliquee_le FROM schema_version ORDER BY version");
            
            // Find existing grid or create new one
            Grid<Map<String, Object>> grid = container.getChildren()
                .filter(c -> c instanceof Grid)
                .map(c -> (Grid<Map<String, Object>>) c)
                .findFirst()
                .orElseGet(() -> {
                    Grid<Map<String, Object>> newGrid = new Grid<>();
                    newGrid.addColumn(m -> m.get("version")).setHeader("Version").setWidth("150px").setFlexGrow(0);
                    newGrid.addColumn(m -> m.get("fichier")).setHeader("Fichier").setAutoWidth(true);
                    newGrid.addColumn(m -> m.get("appliquee_le")).setHeader("Appliquée le").setAutoWidth(true);
                    container.add(newGrid);
                    return newGrid;
                });
            grid.setItems(migrations);
        } catch (Exception e) {
            // Table might not exist yet
        }
    }

    private void openDialog(VerticalLayout content) {
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.add(content);
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(false);
        dialog.open();
    }

    private void closeDialog() {
        getUI().ifPresent(ui -> ui.getChildren()
            .filter(c -> c instanceof com.vaadin.flow.component.dialog.Dialog)
            .findFirst()
            .ifPresent(c -> ((com.vaadin.flow.component.dialog.Dialog) c).close()));
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
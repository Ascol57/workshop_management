package fr.insa.crusche.webui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

/**
 * Page de configuration initiale - affichée seulement si aucun utilisateur n'existe.
 * Permet de créer le premier compte administrateur.
 */
@Route(value = "setup")
@PageTitle("Configuration initiale")
@AnonymousAllowed
public class SetupView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier si des utilisateurs existent déjà
        List<Map<String, Object>> users = DatabaseManager.query("SELECT COUNT(*) as cnt FROM app_user");
        long count = users.isEmpty() ? 0 : ((Number) users.get(0).get("cnt")).longValue();
        
        if (count > 0) {
            // Des utilisateurs existent déjà, rediriger vers login
            event.forwardTo("login");
        }
    }

    public SetupView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        VerticalLayout formLayout = createSetupForm();
        formLayout.setWidth("100%");
        formLayout.setMaxWidth("600px");
        formLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-l)");
        formLayout.getStyle().set("padding", "var(--lumo-space-xl)");
        formLayout.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        
        add(formLayout);
    }

    private VerticalLayout createSetupForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        // Header
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(true);
        header.setAlignItems(Alignment.CENTER);
        
        H2 title = new H2(I18n.t("Première configuration"));
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xlarge)");
        title.getStyle().set("text-align", "center");
        
        Paragraph subtitle = new Paragraph(I18n.t("Aucun utilisateur trouvé. Créez le compte administrateur initial."));
        subtitle.getStyle().set("margin", "0");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("text-align", "center");
        subtitle.getStyle().set("max-width", "500px");
        
        H3 warning = new H3(I18n.t("⚠️ Cette page n'apparaît qu'une seule fois"));
        warning.getStyle().set("margin", "var(--lumo-space-m) 0 0 0");
        warning.getStyle().set("color", "var(--lumo-error-color)");
        warning.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        header.add(title, subtitle, warning);
        form.add(header);

        // Informations personnelles
        H3 personalTitle = new H3(I18n.t("Informations du compte"));
        personalTitle.getStyle().set("margin-top", "var(--lumo-space-l)");
        personalTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        personalTitle.getStyle().set("border-top", "1px solid var(--lumo-contrast-20pct)");
        personalTitle.getStyle().set("padding-top", "var(--lumo-space-m)");
        form.add(personalTitle);

        HorizontalLayout nameLayout = new HorizontalLayout();
        nameLayout.setWidthFull();
        nameLayout.setSpacing(true);
        
        TextField nomField = new TextField(I18n.t("Nom") + " *");
        nomField.setRequired(true);
        nomField.setPlaceholder("Admin");
        
        TextField prenomField = new TextField(I18n.t("Prénom") + " *");
        prenomField.setRequired(true);
        prenomField.setPlaceholder("Super");
        
        nameLayout.add(nomField, prenomField);
        nameLayout.setFlexGrow(1, nomField, prenomField);

        form.add(nameLayout);

        // Identifiants
        H3 credsTitle = new H3(I18n.t("Identifiants administrateur"));
        credsTitle.getStyle().set("margin-top", "var(--lumo-space-l)");
        credsTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        credsTitle.getStyle().set("border-top", "1px solid var(--lumo-contrast-20pct)");
        credsTitle.getStyle().set("padding-top", "var(--lumo-space-m)");
        form.add(credsTitle);

        TextField loginField = new TextField(I18n.t("Login") + " *");
        loginField.setRequired(true);
        loginField.setPlaceholder("admin");
        loginField.setHelperText(I18n.t("Identifiant unique pour la connexion"));
        loginField.setWidthFull();

        PasswordField passwordField = new PasswordField(I18n.t("Mot de passe") + " *");
        passwordField.setRequired(true);
        passwordField.setPlaceholder("••••••••");
        passwordField.setHelperText(I18n.t("Minimum 8 caractères recommandés"));
        passwordField.setWidthFull();

        PasswordField confirmPasswordField = new PasswordField(I18n.t("Confirmer le mot de passe *"));
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setPlaceholder("••••••••");
        confirmPasswordField.setWidthFull();

        form.add(loginField, passwordField, confirmPasswordField);

        // Bouton de création
        Button createButton = new Button(I18n.t("Créer l'administrateur et continuer"));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        createButton.setWidthFull();
        createButton.getStyle().set("margin-top", "var(--lumo-space-m)");
        createButton.getStyle().set("font-size", "var(--lumo-font-size-l)");
        createButton.getStyle().set("padding", "var(--lumo-space-m) var(--lumo-space-l)");
        createButton.addClickListener(e -> attemptSetup(
            nomField, prenomField, loginField,
            passwordField, confirmPasswordField,
            createButton
        ));

        // Info bas de page
        Paragraph footer = new Paragraph(I18n.t("Ce compte aura le rôle ADMINISTRATEUR avec tous les droits sur l'application."));
        footer.getStyle().set("margin", "var(--lumo-space-l) 0 0 0");
        footer.getStyle().set("font-size", "var(--lumo-font-size-s)");
        footer.getStyle().set("color", "var(--lumo-secondary-text-color)");
        footer.getStyle().set("text-align", "center");

        form.add(createButton, footer);
        return form;
    }

    private void attemptSetup(TextField nomField, TextField prenomField,
                             TextField loginField, PasswordField passwordField,
                             PasswordField confirmPasswordField, Button createButton) {
        
        String nom = nomField.getValue().trim();
        String prenom = prenomField.getValue().trim();
        String login = loginField.getValue().trim();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validation
        if (nom.isEmpty() || prenom.isEmpty() || login.isEmpty() || password.isEmpty()) {
            showError(I18n.t("Tous les champs obligatoires (*) doivent être remplis"));
            createButton.setEnabled(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(I18n.t("Les mots de passe ne correspondent pas"));
            createButton.setEnabled(true);
            return;
        }

        if (password.length() < 8) {
            showError(I18n.t("Le mot de passe doit contenir au moins 8 caractères"));
            createButton.setEnabled(true);
            return;
        }

        // Vérifier une dernière fois qu'aucun user n'existe (race condition)
        List<Map<String, Object>> check = DatabaseManager.query("SELECT COUNT(*) as cnt FROM app_user");
        if (!check.isEmpty() && ((Number) check.get(0).get("cnt")).longValue() > 0) {
            showError("Un utilisateur existe déjà. Redirection...");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        // Hash du mot de passe avec BCrypt
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        
        DatabaseManager.update("INSERT INTO workshop (name) "
            + "SELECT ? FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM workshop)", "Atelier principal");

        String sql = """
            INSERT INTO app_user (name, first_name, login, password_hash, role, workshop_id)
            VALUES (?, ?, ?, ?, 'ADMINISTRATEUR', (SELECT MIN(id) FROM workshop))
            """;
        
        int rows = DatabaseManager.update(sql, nom, prenom, login, hash);

        if (rows > 0) {
            Notification.show(I18n.t("Administrateur créé avec succès ! Redirection vers la connexion..."), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Rediriger vers login après un court délai
            getUI().ifPresent(ui -> {
                ui.getPage().executeJs("setTimeout(() => window.location.href = '/login', 2000);");
            });
        } else {
            showError(I18n.t("Erreur lors de la création de l'administrateur"));
            createButton.setEnabled(true);
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
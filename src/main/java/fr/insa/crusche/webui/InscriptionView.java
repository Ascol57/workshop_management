package fr.insa.crusche.webui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.insa.crusche.repository.UserRepository;

@Route(value = "inscription")
@PageTitle("Inscription")
@AnonymousAllowed
public class InscriptionView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService = SessionService.getInstance();
    private final UserRepository userRepo = new UserRepository();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo("login");
    }

    public InscriptionView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout formLayout = createRegistrationForm();
        formLayout.setWidth("100%");
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-l)");
        formLayout.getStyle().set("padding", "var(--lumo-space-xl)");
        formLayout.getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        add(formLayout);
    }

    private VerticalLayout createRegistrationForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        H2 title = new H2(I18n.t("Créer un compte"));
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xlarge)");

        HorizontalLayout nameLayout = new HorizontalLayout();
        nameLayout.setWidthFull();
        nameLayout.setSpacing(true);

        TextField nomField = new TextField(I18n.t("Nom"));
        nomField.setRequired(true);
        nomField.setPlaceholder("Dupont");

        TextField prenomField = new TextField(I18n.t("Prénom"));
        prenomField.setRequired(true);
        prenomField.setPlaceholder("Jean");

        nameLayout.add(nomField, prenomField);
        nameLayout.setFlexGrow(1, nomField, prenomField);

        TextField loginField = new TextField(I18n.t("Identifiant (login)"));
        loginField.setRequired(true);
        loginField.setPlaceholder("jdupont");
        loginField.setWidthFull();
        loginField.setHelperText(I18n.t("Unique, sera utilisé pour la connexion"));

        PasswordField passwordField = new PasswordField(I18n.t("Mot de passe"));
        passwordField.setRequired(true);
        passwordField.setPlaceholder("••••••••");
        passwordField.setWidthFull();

        PasswordField confirmPasswordField = new PasswordField(I18n.t("Confirmer le mot de passe"));
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setPlaceholder("••••••••");
        confirmPasswordField.setWidthFull();

        Button registerButton = new Button(I18n.t("S'inscrire"));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.getStyle().set("margin-top", "var(--lumo-space-s)");
        registerButton.addClickListener(e -> attemptRegistration(
            nomField, prenomField, loginField, passwordField, confirmPasswordField, registerButton));

        Anchor loginLink = new Anchor("login", I18n.t("Déjà un compte ? Se connecter"));
        loginLink.getElement().getStyle().set("font-size", "var(--lumo-font-size-s)");

        form.add(title, nameLayout, loginField, passwordField, confirmPasswordField, registerButton, loginLink);
        return form;
    }

    private void attemptRegistration(TextField nomField, TextField prenomField, TextField loginField,
                                     PasswordField passwordField, PasswordField confirmPasswordField,
                                     Button registerButton) {
        String nom = nomField.getValue().trim();
        String prenom = prenomField.getValue().trim();
        String login = loginField.getValue().trim();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || login.isEmpty() || password.isEmpty()) {
            showError(I18n.t("Tous les champs obligatoires doivent être remplis"));
            registerButton.setEnabled(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(I18n.t("Les mots de passe ne correspondent pas"));
            registerButton.setEnabled(true);
            return;
        }

        if (password.length() < 6) {
            showError(I18n.t("Le mot de passe doit contenir au moins 6 caractères"));
            registerButton.setEnabled(true);
            return;
        }

        boolean exists = !userRepo.findByLogin(login).isEmpty();
        if (exists) {
            showError(I18n.t("Identifiant déjà utilisé"));
            registerButton.setEnabled(true);
            return;
        }

        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        int inserted = userRepo.insert(nom, prenom, login, hash, "OUVRIER", 1);

        if (inserted > 0) {
            Notification.show(I18n.t("Compte créé avec succès ! Vous pouvez maintenant vous connecter."), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        } else {
            showError(I18n.t("Erreur lors de la création du compte"));
            registerButton.setEnabled(true);
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
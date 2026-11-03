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
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import fr.insa.crusche.repository.UserRepository;

import java.util.Map;

@Route(value = "login")
@PageTitle("Connexion")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService = SessionService.getInstance();
    private final UserRepository userRepo = new UserRepository();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionService.isLoggedIn()) {
            event.forwardTo("");
        }
    }

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout formLayout = createLoginForm();
        formLayout.setWidth("100%");
        formLayout.setMaxWidth("400px");
        formLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-l)");
        formLayout.getStyle().set("padding", "var(--lumo-space-xl)");
        formLayout.getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        add(formLayout);
    }

    private VerticalLayout createLoginForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        H2 title = new H2(I18n.t("Connexion"));
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xlarge)");

        TextField loginField = new TextField(I18n.t("Identifiant"));
        loginField.setPlaceholder(I18n.t("Votre login"));
        loginField.setRequired(true);
        loginField.setAutofocus(true);
        loginField.setWidthFull();

        PasswordField passwordField = new PasswordField(I18n.t("Mot de passe"));
        passwordField.setPlaceholder(I18n.t("Votre mot de passe"));
        passwordField.setRequired(true);
        passwordField.setWidthFull();

        Checkbox rememberMe = new Checkbox(I18n.t("Se souvenir de moi"));

        Button loginButton = new Button(I18n.t("Se connecter"));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle().set("margin-top", "var(--lumo-space-s)");
        loginButton.addClickListener(e -> attemptLogin(loginField, passwordField, loginButton));

        passwordField.addKeyPressListener(Key.ENTER, e -> attemptLogin(loginField, passwordField, loginButton));
        loginField.addKeyPressListener(Key.ENTER, e -> attemptLogin(loginField, passwordField, loginButton));


        form.add(title, loginField, passwordField, rememberMe, loginButton);
        return form;
    }

    private void attemptLogin(TextField loginField, PasswordField passwordField, Button loginButton) {
        String login = loginField.getValue().trim();
        String password = passwordField.getValue();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            loginButton.setEnabled(true);
            return;
        }

        int userId = sessionService.authenticate(login, password);
        if (userId > 0) {
            var userData = userRepo.findByLogin(login);
            if (!userData.isEmpty()) {
                Map<String, Object> row = userData.get(0);
                sessionService.login(userId, login,
                    (String) row.get("name"),
                    (String) row.get("first_name"),
                    (String) row.get("role"));
                Notification.show("Bienvenue " + row.get("first_name") + " !")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        } else {
            showError("Identifiant ou mot de passe incorrect");
            loginButton.setEnabled(true);
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
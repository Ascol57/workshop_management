package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "invitation")
@AnonymousAllowed
public class InvitationView extends VerticalLayout implements BeforeEnterObserver {

    private String token;
    private final VerticalLayout content = new VerticalLayout();

    public InvitationView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        content.setMaxWidth("500px");
        add(content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        token = location.getQueryParameters().getParameters()
            .getOrDefault("token", List.of()).stream().findFirst().orElse(null);
        content.removeAll();
        var invitation = InvitationService.findValid(token);
        if (invitation == null) {
            content.add(new H2(I18n.t("Invitation invalide ou expirée")));
            return;
        }

        H2 title = new H2(I18n.t("Finaliser votre inscription"));
        TextField name = new TextField(I18n.t("Nom"));
        TextField firstName = new TextField(I18n.t("Prénom"));
        PasswordField password = new PasswordField(I18n.t("Mot de passe"));
        PasswordField confirmation = new PasswordField(I18n.t("Confirmer le mot de passe"));
        Button submit = new Button(I18n.t("Créer mon compte"), submitClick -> {
            if (name.getValue().isBlank() || firstName.getValue().isBlank()
                    || password.getValue().length() < 8
                    || !password.getValue().equals(confirmation.getValue())) {
                Notification.show(I18n.t("Informations invalides"));
                return;
            }
            try {
                InvitationService.complete(token, name.getValue().trim(), firstName.getValue().trim(),
                    password.getValue());
                content.removeAll();
                content.add(new H2(I18n.t("Compte créé avec succès")), new Button(
                    I18n.t("Se connecter"), click -> getUI().ifPresent(ui -> ui.navigate("login"))));
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Invitation invalide ou expirée"));
            }
        });
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(title, name, firstName, password, confirmation, submit);
    }
}

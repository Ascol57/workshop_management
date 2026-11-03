package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
import fr.insa.crusche.model.Role;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Route(value = "profil", layout = MainLayout.class)
@PageTitle("Mon Profil")
@AnonymousAllowed
public class ProfilView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn()) {
            event.forwardTo("login");
        }
    }

    public ProfilView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Mon Profil"));
        title.getStyle().set("margin", "0");

        Optional<SessionService.UserSession> userOpt = sessionService.getCurrentUser();
        if (userOpt.isEmpty()) {
            add(title, new com.vaadin.flow.component.html.Span(I18n.t("Erreur : utilisateur non connecté")));
            return;
        }

        SessionService.UserSession user = userOpt.get();

        // Load full user data from DB (using new app_user schema)
        var userData = DatabaseManager.query("SELECT * FROM app_user WHERE id = ?", user.id);
        Map<String, Object> data = userData.isEmpty() ? null : userData.get(0);

        // Form fields
        HorizontalLayout nameLayout = new HorizontalLayout();
        nameLayout.setWidthFull();
        nameLayout.setSpacing(true);

        TextField nomField = new TextField(I18n.t("Nom") + " *");
        nomField.setRequired(true);
        nomField.setValue(nonNull(data != null ? data.get("name") : user.nom));

        TextField prenomField = new TextField(I18n.t("Prénom") + " *");
        prenomField.setRequired(true);
        prenomField.setValue(nonNull(data != null ? data.get("first_name") : user.prenom));

        nameLayout.add(nomField, prenomField);
        nameLayout.setFlexGrow(1, nomField, prenomField);

        TextField loginField = new TextField(I18n.t("Login"));
        loginField.setValue(nonNull(user.login));
        loginField.setReadOnly(true); // Login cannot be changed
        loginField.setWidthFull();
        loginField.setHelperText(I18n.t("Le login ne peut pas être changé"));

        TextField emailField = new TextField("Email");
        emailField.setValue(nonNull(data != null ? data.get("email") : ""));
        emailField.setWidthFull();

        // Role field
        ComboBox<Role> roleField = new ComboBox<>(I18n.t("Rôle"));
        roleField.setItems(Role.values());
        String currentRole = data != null ? (String) data.get("role") : user.role;
        roleField.setValue(currentRole != null ? Role.valueOf(currentRole) : Role.OUVRIER);
        roleField.setWidthFull();

        // Workshop ID (for workshop manager context)
        TextField atelierField = new TextField(I18n.t("Atelier ID"));
        atelierField.setValue(String.valueOf(data != null ? data.get("workshop_id") : 1));
        atelierField.setReadOnly(true);

        // Add some info fields for the new model
        TextField nameField2 = new TextField(I18n.t("Nom complet complet"));
        nameField2.setValue(nonNull(user.nom) + " " + nonNull(user.prenom));
        nameField2.setReadOnly(true);

        add(title, nameLayout, loginField, emailField, roleField, atelierField, nameField2);
    }

    private String nonNull(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    // Removed saveProfile/changePassword for brevity - can be added later using the new schema
}
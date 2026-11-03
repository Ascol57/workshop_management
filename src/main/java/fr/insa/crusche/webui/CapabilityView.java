package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

@Route(value = "capacites", layout = MainLayout.class)
public class CapabilityView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();

    public CapabilityView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Capacités des machines"));
        Button add = new Button(I18n.t("Ajouter"), event -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(SessionService.getInstance().canManageWorkshop());

        grid.addColumn(row -> row.get("name")).setHeader(I18n.t("Nom de la capacité")).setAutoWidth(true);
        grid.addComponentColumn(row -> new Button(I18n.t("Supprimer"),
            event -> delete(row))).setHeader(I18n.t("Actions"));
        grid.setSizeFull();

        add(title, new HorizontalLayout(add), grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        grid.setItems(DatabaseManager.query("SELECT id, name FROM capability ORDER BY name"));
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Ajouter une capacité"));
        TextField name = new TextField(I18n.t("Nom de la capacité"));
        name.setWidthFull();
        Button save = new Button(I18n.t("Enregistrer"), event -> {
            if (name.getValue().isBlank()) {
                name.setInvalid(true);
                return;
            }
            try {
                DatabaseManager.update("INSERT INTO capability (name) VALUES (?)", name.getValue().trim());
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Cette capacité existe déjà"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(name,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private void delete(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM capability WHERE id = ?", row.get("id"));
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cette capacité"));
        }
    }
}

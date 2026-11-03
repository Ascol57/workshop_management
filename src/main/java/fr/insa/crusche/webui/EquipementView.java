package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "equipement", layout = MainLayout.class)
public class EquipementView extends VerticalLayout {

    private final Grid<Object> grid = new Grid<>();

    public EquipementView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Équipement"));
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        Button addButton = new Button(I18n.t("Ajouter"), e -> openDialog(null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setEnabled(SessionService.getInstance().canManageWorkshop());
        Button refreshButton = new Button(I18n.t("Actualiser"), e -> loadData());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        toolbar.add(addButton, refreshButton);

        grid.setSizeFull();
        grid.addColumn(r -> ((Map<String, Object>) r).get("id")).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(r -> ((Map<String, Object>) r).get("equipment_ref")).setHeader("Référence").setAutoWidth(true);
        grid.addColumn(r -> ((Map<String, Object>) r).get("description")).setHeader("Description").setAutoWidth(true);
        grid.addColumn(r -> ((Map<String, Object>) r).get("workshop_id")).setHeader("Atelier").setAutoWidth(true);
        grid.addComponentColumn(row -> new HorizontalLayout(
            new Button(I18n.t("Modifier"), e -> openDialog((Map<String, Object>) row)),
            new Button(I18n.t("Supprimer"), e -> delete((Map<String, Object>) row))
        )).setHeader(I18n.t("Actions")).setAutoWidth(true);

        add(title, toolbar, grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        String sql = "SELECT * FROM equipment";
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        grid.setItems(new ArrayList<>(rows));
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter un équipement") : I18n.t("Modifier l'équipement"));
        TextField reference = new TextField(I18n.t("Référence"));
        TextField description = new TextField(I18n.t("Description"));
        ComboBox<Map<String, Object>> workshop = new ComboBox<>(I18n.t("Atelier"));
        List<Map<String, Object>> workshops = DatabaseManager.query(
            "SELECT id, name FROM workshop ORDER BY name");
        workshop.setItems(workshops);
        workshop.setItemLabelGenerator(item -> String.valueOf(item.get("name")));
        reference.setWidthFull();
        description.setWidthFull();
        workshop.setWidthFull();
        if (row != null) {
            reference.setValue(String.valueOf(row.get("equipment_ref")));
            description.setValue(String.valueOf(row.get("description")));
            workshops.stream()
                .filter(item -> item.get("id").toString().equals(row.get("workshop_id").toString()))
                .findFirst()
                .ifPresent(workshop::setValue);
        }
        Button cancel = new Button(I18n.t("Annuler"), e -> dialog.close());
        Button save = new Button(I18n.t("Enregistrer"), e -> {
            try {
                if (row == null) {
                    DatabaseManager.update(
                        "INSERT INTO equipment (equipment_ref, description, workshop_id) VALUES (?, ?, ?)",
                        reference.getValue().trim(), description.getValue().trim(),
                        workshop.getValue().get("id"));
                } else {
                    DatabaseManager.update(
                        "UPDATE equipment SET equipment_ref = ?, description = ?, workshop_id = ? WHERE id = ?",
                        reference.getValue().trim(), description.getValue().trim(),
                        workshop.getValue().get("id"), row.get("id"));
                }
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(reference, description, workshop,
            new HorizontalLayout(cancel, save)));
        dialog.open();
    }

    private void delete(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM equipment WHERE id = ?", row.get("id"));
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cet équipement"));
        }
    }
}

package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "machine", layout = MainLayout.class)
public class MachineView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();

    public MachineView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Machine"));
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
        grid.addColumn(r -> r.get("id")).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(r -> r.get("type")).setHeader("Type").setAutoWidth(true);
        grid.addColumn(r -> r.get("hourly_cost")).setHeader("Coût/h").setAutoWidth(true);
        grid.addColumn(r -> r.get("x")).setHeader("X").setAutoWidth(true);
        grid.addColumn(r -> r.get("y")).setHeader("Y").setAutoWidth(true);
        grid.addColumn(r -> r.get("status")).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(r -> r.get("equipment_id")).setHeader("Équipement").setAutoWidth(true);
        grid.addComponentColumn(row -> new HorizontalLayout(
            new Button(I18n.t("Modifier"), e -> openDialog(row)),
            new Button(I18n.t("Supprimer"), e -> delete(row))
        )).setHeader(I18n.t("Actions")).setAutoWidth(true);

        add(title, toolbar, grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        String sql = "SELECT * FROM machine";
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        grid.setItems(rows);
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter une machine") : I18n.t("Modifier la machine"));
        TextField type = new TextField(I18n.t("Type"));
        NumberField hourlyCost = new NumberField(I18n.t("Coût horaire"));
        NumberField x = new NumberField("X");
        NumberField y = new NumberField("Y");
        TextField status = new TextField(I18n.t("Statut"));
        ComboBox<Map<String, Object>> equipment = new ComboBox<>(I18n.t("Équipement"));
        CheckboxGroup<Map<String, Object>> capabilities = new CheckboxGroup<>(I18n.t("Capacités"));
        List<Map<String, Object>> equipments = DatabaseManager.query(
            "SELECT id, equipment_ref, description FROM equipment ORDER BY equipment_ref");
        equipment.setItems(equipments);
        equipment.setItemLabelGenerator(item -> item.get("equipment_ref") + " - " + item.get("description"));
        type.setWidthFull();
        hourlyCost.setWidthFull();
        x.setWidthFull();
        y.setWidthFull();
        status.setWidthFull();
        hourlyCost.setMin(0);
        hourlyCost.setRequiredIndicatorVisible(true);
        x.setMin(0);
        y.setMin(0);
        equipment.setWidthFull();
        List<Map<String, Object>> capabilityRows = DatabaseManager.query(
            "SELECT id, name FROM capability ORDER BY name");
        capabilities.setItems(capabilityRows);
        capabilities.setItemLabelGenerator(item -> String.valueOf(item.get("name")));
        capabilities.setWidthFull();
        if (row != null) {
            type.setValue(String.valueOf(row.get("type")));
            hourlyCost.setValue(((Number) row.get("hourly_cost")).doubleValue());
            x.setValue(((Number) row.get("x")).doubleValue());
            y.setValue(((Number) row.get("y")).doubleValue());
            status.setValue(String.valueOf(row.get("status")));
            equipments.stream()
                .filter(item -> item.get("id").toString().equals(row.get("equipment_id").toString()))
                .findFirst()
                .ifPresent(equipment::setValue);
            capabilities.setValue(capabilityRows.stream()
                .filter(capability -> DatabaseManager.query(
                    "SELECT capability_id FROM machine_capability WHERE machine_id = ? AND capability_id = ?",
                    row.get("id"), capability.get("id")).size() > 0)
                .collect(java.util.stream.Collectors.toSet()));
        } else {
            status.setValue("AVAILABLE");
            hourlyCost.setValue(0.0);
            x.setValue(40.0);
            y.setValue(40.0);
        }
        Button cancel = new Button(I18n.t("Annuler"), e -> dialog.close());
        Button save = new Button(I18n.t("Enregistrer"), e -> {
            try {
                Object[] values = {
                    type.getValue().trim(), hourlyCost.getValue().floatValue(),
                    x.getValue().floatValue(), y.getValue().floatValue(),
                    status.getValue().trim().toUpperCase(),
                    equipment.getValue().get("id")
                };
                int machineId;
                if (row == null) {
                    machineId = DatabaseManager.updateAndGetGeneratedId(
                        "INSERT INTO machine (type, hourly_cost, x, y, status, equipment_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?)", values);
                } else {
                    machineId = ((Number) row.get("id")).intValue();
                    DatabaseManager.update(
                        "UPDATE machine SET type=?, hourly_cost=?, x=?, y=?, status=?, equipment_id=? WHERE id=?",
                        values[0], values[1], values[2], values[3], values[4], values[5], row.get("id"));
                }
                DatabaseManager.update("DELETE FROM machine_capability WHERE machine_id = ?", machineId);
                for (Map<String, Object> capability : capabilities.getValue()) {
                    DatabaseManager.update(
                        "INSERT INTO machine_capability (machine_id, capability_id) VALUES (?, ?)",
                        machineId, capability.get("id"));
                }
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(type, hourlyCost, x, y, status, equipment, capabilities,
            new HorizontalLayout(cancel, save)));
        dialog.open();
    }

    private void delete(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM machine WHERE id = ?", row.get("id"));
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cette machine"));
        }
    }
}
package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

@Route(value = "operations", layout = MainLayout.class)
public class OperationView extends VerticalLayout {

    private final ComboBox<Map<String, Object>> routing = new ComboBox<>();
    private final Grid<Map<String, Object>> grid = new Grid<>();

    public OperationView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Opérations de fabrication"));
        routing.setLabel(I18n.t("Gamme"));
        routing.setItemLabelGenerator(row -> String.valueOf(row.get("routing_ref")));
        routing.setItems(DatabaseManager.query(
            "SELECT id, routing_ref FROM routing ORDER BY routing_ref"));
        routing.addValueChangeListener(event -> loadData());

        Button add = new Button(I18n.t("Ajouter"), event -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(SessionService.getInstance().canManageWorkshop());

        grid.addColumn(row -> row.get("sequence_order")).setHeader(I18n.t("Ordre"));
        grid.addColumn(row -> row.get("operation_ref")).setHeader(I18n.t("Référence"));
        grid.addColumn(row -> row.get("description")).setHeader(I18n.t("Description"));
        grid.addColumn(row -> row.get("capability_name")).setHeader(I18n.t("Capacité"));
        grid.addColumn(row -> row.get("duration")).setHeader(I18n.t("Durée"));
        grid.addComponentColumn(row -> new HorizontalLayout(
            new Button("↑", event -> move(row, -1)),
            new Button("↓", event -> move(row, 1)),
            new Button(I18n.t("Modifier"), event -> openDialog(row)),
            new Button(I18n.t("Supprimer"), event -> delete(row))
        )).setHeader(I18n.t("Actions"));
        grid.setSizeFull();

        add(title, new HorizontalLayout(routing, add), grid);
        setFlexGrow(1, grid);
        if (!routing.getListDataView().getItems().toList().isEmpty()) {
            routing.setValue(routing.getListDataView().getItems().toList().get(0));
        }
    }

    private void loadData() {
        if (routing.getValue() == null) {
            grid.setItems(List.of());
            return;
        }
        grid.setItems(DatabaseManager.query("""
            SELECT o.id, o.sequence_order, o.operation_ref, o.description, o.duration,
                   c.name AS capability_name
            FROM operation o
            LEFT JOIN capability c ON c.id = o.capability_id
            WHERE o.routing_id = ?
            ORDER BY o.sequence_order, o.id
            """, routing.getValue().get("id")));
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter une opération") : I18n.t("Modifier l'opération"));
        TextField reference = new TextField(I18n.t("Référence"));
        TextField description = new TextField(I18n.t("Description"));
        NumberField order = new NumberField(I18n.t("Ordre"));
        NumberField duration = new NumberField(I18n.t("Durée"));
        ComboBox<Map<String, Object>> capability = new ComboBox<>(I18n.t("Capacité"));
        List<Map<String, Object>> capabilityRows = DatabaseManager.query(
            "SELECT id, name FROM capability ORDER BY name");
        capability.setItems(capabilityRows);
        capability.setItemLabelGenerator(item -> String.valueOf(item.get("name")));
        reference.setWidthFull();
        reference.setReadOnly(true);
        description.setWidthFull();
        order.setWidthFull();
        duration.setWidthFull();
        capability.setWidthFull();
        if (row != null) {
            reference.setValue(String.valueOf(row.get("operation_ref")));
            description.setValue(String.valueOf(row.get("description")));
            order.setValue(((Number) row.get("sequence_order")).doubleValue());
            duration.setValue(((Number) row.get("duration")).doubleValue());
        } else {
            reference.setValue(generateReference());
            order.setValue((double) (grid.getListDataView().getItemCount() + 1));
            duration.setValue(0.0);
        }
        Button save = new Button(I18n.t("Enregistrer"), event -> {
            try {
                if (capability.getValue() == null || routing.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                Object[] values = {
                    reference.getValue().trim(), description.getValue().trim(),
                    duration.getValue(), capability.getValue().get("id"), order.getValue().intValue()
                };
                if (row == null) {
                    DatabaseManager.update("""
                        INSERT INTO operation
                            (operation_ref, description, equipment_id, duration, routing_id,
                             capability_id, machine_id, sequence_order)
                        VALUES (?, ?, NULL, ?, ?, ?, NULL, ?)
                        """, values[0], values[1], values[2], routing.getValue().get("id"),
                        values[3], values[4]);
                } else {
                    DatabaseManager.update("""
                        UPDATE operation
                        SET operation_ref=?, description=?, equipment_id=NULL, duration=?,
                            capability_id=?, machine_id=NULL, sequence_order=?
                        WHERE id=?
                        """, values[0], values[1], values[2], values[3], values[4], row.get("id"));
                }
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(reference, description, order, duration, capability,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private String generateReference() {
        int next = DatabaseManager.query(
            "SELECT COALESCE(MAX(id), 0) + 1 AS next_ref FROM operation").get(0)
            .get("next_ref") instanceof Number number ? number.intValue() : 1;
        return "OP-" + String.format("%03d", next);
    }

    private void move(Map<String, Object> row, int direction) {
        int current = ((Number) row.get("sequence_order")).intValue();
        DatabaseManager.update(
            "UPDATE operation SET sequence_order = ? WHERE id = ?",
            Math.max(1, current + direction), row.get("id"));
        loadData();
    }

    private void delete(Map<String, Object> row) {
        DatabaseManager.update("DELETE FROM operation WHERE id = ?", row.get("id"));
        loadData();
    }
}

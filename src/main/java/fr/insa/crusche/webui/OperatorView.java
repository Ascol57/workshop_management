package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;
import fr.insa.crusche.model.OperatorStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "operateurs", layout = MainLayout.class)
public class OperatorView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();

    public OperatorView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Opérateurs"));
        Button add = new Button(I18n.t("Ajouter"), event -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setEnabled(SessionService.getInstance().canManageWorkshop());

        grid.addColumn(row -> row.get("login")).setHeader(I18n.t("Utilisateur")).setAutoWidth(true);
        grid.addColumn(row -> row.get("hourly_cost")).setHeader(I18n.t("Coût horaire")).setAutoWidth(true);
        grid.addColumn(row -> row.get("status")).setHeader(I18n.t("Statut")).setAutoWidth(true);
        grid.addColumn(row -> row.get("machines")).setHeader(I18n.t("Machines autorisées")).setAutoWidth(true);
        grid.addComponentColumn(row -> new Button(I18n.t("Modifier"),
            event -> openDialog(row))).setHeader(I18n.t("Actions"));
        grid.setSizeFull();

        add(title, new HorizontalLayout(add), grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        grid.setItems(DatabaseManager.query("""
            SELECT o.id, o.hourly_cost, o.status, u.login,
                   GROUP_CONCAT(CONCAT(m.type, ' (#', m.id, ')') ORDER BY m.id SEPARATOR ', ') AS machines
            FROM operator o
            JOIN app_user u ON u.id = o.user_id
            LEFT JOIN operator_machine om ON om.operator_id = o.id
            LEFT JOIN machine m ON m.id = om.machine_id
            GROUP BY o.id, o.hourly_cost, o.status, u.login
            ORDER BY u.login
            """));
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter un opérateur")
            : I18n.t("Modifier l'opérateur"));

        ComboBox<Map<String, Object>> user = new ComboBox<>(I18n.t("Utilisateur"));
        List<Map<String, Object>> users = DatabaseManager.query(
            "SELECT id, login, name, first_name FROM app_user ORDER BY login");
        user.setItems(users);
        user.setItemLabelGenerator(item -> item.get("login") + " - "
            + item.get("first_name") + " " + item.get("name"));

        NumberField hourlyCost = new NumberField(I18n.t("Coût horaire"));
        hourlyCost.setMin(0);
        ComboBox<OperatorStatus> status = new ComboBox<>(I18n.t("Statut"));
        status.setItems(OperatorStatus.values());

        CheckboxGroup<Map<String, Object>> machines = new CheckboxGroup<>(I18n.t("Machines autorisées"));
        List<Map<String, Object>> machineRows = DatabaseManager.query(
            "SELECT id, type FROM machine ORDER BY id");
        machines.setItems(machineRows);
        machines.setItemLabelGenerator(item -> item.get("type") + " (#" + item.get("id") + ")");

        user.setWidthFull();
        hourlyCost.setWidthFull();
        status.setWidthFull();
        machines.setWidthFull();

        if (row != null) {
            users.stream().filter(item -> item.get("id").toString()
                .equals(String.valueOf(row.get("user_id")))).findFirst().ifPresent(user::setValue);
            hourlyCost.setValue(((Number) row.get("hourly_cost")).doubleValue());
            status.setValue(OperatorStatus.valueOf(String.valueOf(row.get("status"))));
            machines.setValue(DatabaseManager.query(
                "SELECT machine_id FROM operator_machine WHERE operator_id = ?", row.get("id"))
                .stream()
                .map(item -> machineRows.stream()
                    .filter(machine -> machine.get("id").toString().equals(item.get("machine_id").toString()))
                    .findFirst().orElse(null))
                .filter(item -> item != null)
                .collect(Collectors.toSet()));
        } else {
            hourlyCost.setValue(0.0);
            status.setValue(OperatorStatus.AVAILABLE);
        }

        Button save = new Button(I18n.t("Enregistrer"), event -> {
            try {
                if (user.getValue() == null || hourlyCost.getValue() == null || status.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                int operatorId;
                if (row == null) {
                    operatorId = DatabaseManager.updateAndGetGeneratedId(
                        "INSERT INTO operator (hourly_cost, status, user_id) VALUES (?, ?, ?)",
                        hourlyCost.getValue(), status.getValue().name(), user.getValue().get("id"));
                } else {
                    operatorId = ((Number) row.get("id")).intValue();
                    DatabaseManager.update(
                        "UPDATE operator SET hourly_cost=?, status=?, user_id=? WHERE id=?",
                        hourlyCost.getValue(), status.getValue().name(), user.getValue().get("id"), operatorId);
                }
                DatabaseManager.update("DELETE FROM operator_machine WHERE operator_id = ?", operatorId);
                for (Map<String, Object> machine : machines.getValue()) {
                    DatabaseManager.update(
                        "INSERT INTO operator_machine (operator_id, machine_id) VALUES (?, ?)",
                        operatorId, machine.get("id"));
                }
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(user, hourlyCost, status, machines,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }
}

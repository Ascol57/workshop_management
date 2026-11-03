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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "objectif-general", layout = MainLayout.class)
public class GeneralGoalView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();

    public GeneralGoalView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Ordres de production"));
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
        grid.addColumn(r -> r.get("target_quantity")).setHeader(I18n.t("Quantité à produire")).setAutoWidth(true);
        grid.addColumn(r -> r.get("product_code")).setHeader(I18n.t("Produit")).setAutoWidth(true);
        grid.addColumn(r -> r.get("routing_ref")).setHeader(I18n.t("Gamme")).setAutoWidth(true);
        grid.addColumn(r -> r.get("status")).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(r -> r.get("workshop_id")).setHeader("Atelier").setAutoWidth(true);
        grid.addComponentColumn(row -> new HorizontalLayout(
            new Button(I18n.t("Modifier"), e -> openDialog(row)),
            new Button(I18n.t("Supprimer"), e -> delete(row))
        )).setHeader(I18n.t("Actions")).setAutoWidth(true);

        add(title, toolbar, grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        String sql = """
            SELECT g.id, g.target_quantity, g.status, g.workshop_id, g.product_id, g.routing_id,
                   p.code AS product_code, r.routing_ref
            FROM general_goal g
            LEFT JOIN product p ON p.id = g.product_id
            LEFT JOIN routing r ON r.id = g.routing_id
            ORDER BY g.id
            """;
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        grid.setItems(rows);
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter un ordre de production")
            : I18n.t("Modifier l'ordre de production"));
        TextField quantity = new TextField(I18n.t("Quantité cible"));
        TextField status = new TextField(I18n.t("Statut"));
        ComboBox<Map<String, Object>> workshop = new ComboBox<>(I18n.t("Atelier"));
        List<Map<String, Object>> workshops = DatabaseManager.query(
            "SELECT id, name FROM workshop ORDER BY name");
        workshop.setItems(workshops);
        workshop.setItemLabelGenerator(item -> String.valueOf(item.get("name")));
        ComboBox<Map<String, Object>> product = new ComboBox<>(I18n.t("Produit à fabriquer"));
        List<Map<String, Object>> products = DatabaseManager.query(
            "SELECT id, code, description, workshop_id FROM product ORDER BY code");
        product.setItems(products);
        product.setItemLabelGenerator(item -> item.get("code") + " - " + item.get("description"));
        ComboBox<Map<String, Object>> routing = new ComboBox<>(I18n.t("Gamme de fabrication"));
        routing.setItemLabelGenerator(item -> item.get("routing_ref") + " (#" + item.get("id") + ")");
        for (TextField field : new TextField[] {quantity, status}) {
            field.setWidthFull();
        }
        workshop.setWidthFull();
        product.setWidthFull();
        routing.setWidthFull();
        product.addValueChangeListener(event -> {
            routing.clear();
            if (event.getValue() == null) {
                routing.setItems(List.of());
                return;
            }
            List<Map<String, Object>> routings = DatabaseManager.query(
                "SELECT id, routing_ref FROM routing WHERE product_id = ? ORDER BY routing_ref",
                event.getValue().get("id"));
            routing.setItems(routings);
            if (routings.size() == 1) {
                routing.setValue(routings.get(0));
            }
        });
        if (row != null) {
            quantity.setValue(String.valueOf(row.get("target_quantity")));
            status.setValue(String.valueOf(row.get("status")));
            workshops.stream()
                .filter(item -> item.get("id").toString().equals(row.get("workshop_id").toString()))
                .findFirst()
                .ifPresent(workshop::setValue);
            products.stream()
                .filter(item -> item.get("id").toString().equals(String.valueOf(row.get("product_id"))))
                .findFirst()
                .ifPresent(product::setValue);
            List<Map<String, Object>> routings = DatabaseManager.query(
                "SELECT id, routing_ref FROM routing WHERE product_id = ? ORDER BY routing_ref",
                row.get("product_id"));
            routing.setItems(routings);
            routings.stream()
                .filter(item -> item.get("id").toString().equals(String.valueOf(row.get("routing_id"))))
                .findFirst()
                .ifPresent(routing::setValue);
        } else {
            status.setValue("IN_PROGRESS");
        }
        product.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                workshops.stream()
                    .filter(item -> item.get("id").toString()
                        .equals(String.valueOf(event.getValue().get("workshop_id"))))
                    .findFirst()
                    .ifPresent(workshop::setValue);
            }
        });
        Button save = new Button(I18n.t("Enregistrer"), e -> {
            try {
                int target = Integer.parseInt(quantity.getValue().trim());
                if (product.getValue() == null || workshop.getValue() == null
                        || routing.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                if (row == null) {
                    int goalId = DatabaseManager.updateAndGetGeneratedId(
                        "INSERT INTO general_goal "
                            + "(target_quantity, status, workshop_id, product_id, routing_id) VALUES (?, ?, ?, ?, ?)",
                        target, status.getValue().trim().toUpperCase(),
                        workshop.getValue().get("id"), product.getValue().get("id"),
                        routing.getValue().get("id"));
                    ensureSpecializedGoals(goalId, routing.getValue().get("id"));
                } else {
                    DatabaseManager.update(
                        "UPDATE general_goal SET target_quantity=?, status=?, workshop_id=?, "
                            + "product_id=?, routing_id=? "
                            + "WHERE id=?",
                        target, status.getValue().trim().toUpperCase(), workshop.getValue().get("id"),
                        product.getValue().get("id"), routing.getValue().get("id"), row.get("id"));
                    ensureSpecializedGoals(((Number) row.get("id")).intValue(),
                        routing.getValue().get("id"));
                }

                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(quantity, product, routing, status, workshop,
            new HorizontalLayout(new Button(I18n.t("Annuler"), e -> dialog.close()), save)));
        dialog.open();
    }

    private void ensureSpecializedGoals(int generalGoalId, Object routingId) {
        DatabaseManager.update("""
            DELETE sg
            FROM specialized_goal sg
            LEFT JOIN operation old_operation ON old_operation.id = sg.operation_id
            WHERE sg.general_goal_id = ?
              AND (old_operation.id IS NULL OR old_operation.routing_id <> ?)
              AND NOT EXISTS (
                  SELECT 1 FROM task existing_task
                  WHERE existing_task.specialized_goal_id = sg.id
              )
            """, generalGoalId, routingId);
        DatabaseManager.update("""
            INSERT INTO specialized_goal (step, status, general_goal_id, operation_id)
            SELECT op.description, 'IN_PROGRESS', ?, op.id
            FROM routing r
            JOIN operation op ON op.routing_id = r.id
            LEFT JOIN specialized_goal existing
                ON existing.general_goal_id = ?
                AND existing.operation_id = op.id
            WHERE r.id = ? AND existing.id IS NULL
            """, generalGoalId, generalGoalId, routingId);
    }

    private void delete(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM general_goal WHERE id = ?", row.get("id"));
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cet objectif"));
        }
    }
}
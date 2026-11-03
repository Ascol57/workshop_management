package fr.insa.crusche.webui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

@Route(value = "decomposition", layout = MainLayout.class)
public class DecompositionView extends VerticalLayout implements BeforeEnterObserver {

    private final Grid<Map<String, Object>> goalsGrid = new Grid<>();
    private final Grid<Map<String, Object>> specializedGrid = new Grid<>();
    private final Grid<Map<String, Object>> tasksGrid = new Grid<>();
    private Integer selectedGeneralGoalId;
    private Integer selectedSpecializedGoalId;

    public DecompositionView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Décomposition : Objectif Général → Spécialisé → Tâche"));
        title.getStyle().set("margin", "0");

        configureGoalsGrid();
        configureSpecializedGrid();
        configureTasksGrid();

        add(title,
            new H3(I18n.t("Objectifs Généraux")), goalsGrid,
            new H3(I18n.t("Objectifs Spécialisés")), specializedGrid,
            new H3(I18n.t("Tâches")), tasksGrid);
        setFlexGrow(1, goalsGrid, specializedGrid, tasksGrid);
        loadGoals();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SessionService.getInstance().canManageWorkshop()) {
            event.forwardTo("tache");
        }
    }

    private void configureGoalsGrid() {
        goalsGrid.setHeight("220px");
        goalsGrid.addColumn(row -> row.get("id")).setHeader("ID").setAutoWidth(true);
        goalsGrid.addColumn(row -> row.get("product_code"))
                .setHeader(I18n.t("Produit")).setAutoWidth(true);
        goalsGrid.addColumn(row -> row.get("target_quantity"))
                .setHeader(I18n.t("Quantité à produire")).setAutoWidth(true);
        goalsGrid.addColumn(row -> row.get("routing_ref"))
                .setHeader(I18n.t("Gamme")).setAutoWidth(true);
        goalsGrid.addColumn(row -> row.get("status")).setHeader(I18n.t("Statut")).setAutoWidth(true);
        goalsGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedGeneralGoalId = event.getValue() == null
                    ? null : ((Number) event.getValue().get("id")).intValue();
            selectedSpecializedGoalId = null;
            loadSpecializedGoals();
        });
    }

    private void configureSpecializedGrid() {
        specializedGrid.setHeight("220px");
        specializedGrid.addColumn(row -> row.get("id")).setHeader("ID").setAutoWidth(true);
        specializedGrid.addColumn(row -> row.get("step"))
                .setHeader(I18n.t("Étape")).setAutoWidth(true);
        specializedGrid.addColumn(row -> row.get("status"))
                .setHeader(I18n.t("Statut")).setAutoWidth(true);
        specializedGrid.addColumn(row -> row.get("completed_quantity"))
                .setHeader(I18n.t("Réalisé")).setAutoWidth(true);
        specializedGrid.addColumn(row -> row.get("remaining_quantity"))
                .setHeader(I18n.t("Restant")).setAutoWidth(true);
        specializedGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedSpecializedGoalId = event.getValue() == null
                    ? null : ((Number) event.getValue().get("id")).intValue();
            loadTasks();
        });
    }

    private void configureTasksGrid() {
        tasksGrid.setHeight("220px");
        tasksGrid.addColumn(row -> row.get("task_id"))
                .setHeader(I18n.t("Tâche")).setAutoWidth(true);
        tasksGrid.addColumn(row -> row.get("operation_description"))
                .setHeader(I18n.t("Opération")).setAutoWidth(true);
        tasksGrid.addColumn(row -> row.get("operator_name"))
                .setHeader(I18n.t("Opérateur")).setAutoWidth(true);
        tasksGrid.addColumn(row -> row.get("machine_name"))
                .setHeader(I18n.t("Machine")).setAutoWidth(true);
        tasksGrid.addColumn(row -> row.get("status"))
                .setHeader(I18n.t("Statut")).setAutoWidth(true);
    }

    private void loadGoals() {
        goalsGrid.setItems(DatabaseManager.query("""
            SELECT g.id, g.target_quantity, g.status, p.code AS product_code,
                   r.routing_ref
            FROM general_goal g
            LEFT JOIN product p ON p.id = g.product_id
            LEFT JOIN routing r ON r.id = g.routing_id
            ORDER BY g.id
            """));
    }

    private void loadSpecializedGoals() {
        if (selectedGeneralGoalId == null) {
            specializedGrid.setItems(List.of());
            tasksGrid.setItems(List.of());
            return;
        }
        specializedGrid.setItems(DatabaseManager.query(
            """
            SELECT sg.id, sg.step, sg.status, sg.operation_id,
                   COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) AS completed_quantity,
                   GREATEST(g.target_quantity -
                       COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END), 0) AS remaining_quantity
            FROM specialized_goal sg
            JOIN general_goal g ON g.id = sg.general_goal_id
            LEFT JOIN task t ON t.specialized_goal_id = sg.id
            WHERE sg.general_goal_id = ?
            GROUP BY sg.id, sg.step, sg.status, sg.operation_id, g.target_quantity
            ORDER BY sg.id
            """, selectedGeneralGoalId));
        tasksGrid.setItems(List.of());
    }

    private void loadTasks() {
        if (selectedSpecializedGoalId == null) {
            tasksGrid.setItems(List.of());
            return;
        }
        tasksGrid.setItems(DatabaseManager.query("""
            SELECT t.task_id, t.status,
                   op.description AS operation_description,
                   CONCAT(u.first_name, ' ', u.name) AS operator_name,
                   CONCAT(m.type, ' (#', m.id, ')') AS machine_name
            FROM task t
            JOIN operation op ON op.id = t.operation_id
            JOIN operator o ON o.id = t.operator_id
            JOIN app_user u ON u.id = o.user_id
            JOIN machine m ON m.id = t.machine_id
            WHERE t.specialized_goal_id = ?
            ORDER BY t.id
            """, selectedSpecializedGoalId));
    }

}

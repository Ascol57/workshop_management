package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Route(value = "tache", layout = MainLayout.class)
public class TaskView extends VerticalLayout implements BeforeEnterObserver {

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final SessionService sessionService = SessionService.getInstance();
    private Button requestTaskButton;
    private Button pauseButton;
    private Button resumeButton;

    public TaskView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t(sessionService.canManageWorkshop() ? "Tâches" : "Mes tâches"));
        title.getStyle().set("margin", "0");

        Button refreshButton = new Button(I18n.t("Actualiser"), e -> loadData());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        if (!sessionService.canManageWorkshop()) {
            requestTaskButton = new Button(I18n.t("Demander une tâche"), e -> requestNextTask());
            requestTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            pauseButton = new Button(I18n.t("Partir en pause"), e -> pauseOperator());
            resumeButton = new Button(I18n.t("Reprendre le travail"), e -> resumeOperator());
            resumeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.add(requestTaskButton, pauseButton, resumeButton);
            refreshWorkerActions();
        }
        add(title, toolbar, grid);
        setFlexGrow(1, grid);
        configureGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loadData();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(row -> row.get("task_id")).setHeader(I18n.t("Tâche")).setAutoWidth(true);
        grid.addColumn(row -> row.get("product_code")).setHeader(I18n.t("Produit")).setAutoWidth(true);
        grid.addColumn(row -> row.get("step")).setHeader(I18n.t("Étape")).setAutoWidth(true);
        grid.addColumn(row -> row.get("operation_description"))
                .setHeader(I18n.t("Opération")).setAutoWidth(true);
        grid.addColumn(row -> row.get("machine_name")).setHeader(I18n.t("Machine")).setAutoWidth(true);
        grid.addColumn(row -> row.get("status")).setHeader(I18n.t("Statut")).setAutoWidth(true);
        if (sessionService.canManageWorkshop()) {
            grid.addColumn(row -> row.get("operator_name"))
                    .setHeader(I18n.t("Opérateur")).setAutoWidth(true);
        }
        grid.addComponentColumn(this::createActions)
                .setHeader(I18n.t("Actions")).setAutoWidth(true);
    }

    private HorizontalLayout createActions(Map<String, Object> row) {
        int id = ((Number) row.get("id")).intValue();
        String status = String.valueOf(row.get("status"));
        Button start = new Button(I18n.t("Démarrer"), e -> updateStatus(id, "IN_PROGRESS"));
        start.setEnabled(sessionService.canManageWorkshop() && "PENDING".equals(status));
        Button finish = new Button(I18n.t("Terminer"), e -> updateStatus(id, "COMPLETED"));
        finish.setEnabled("IN_PROGRESS".equals(status));
        finish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return new HorizontalLayout(start, finish);
    }

    private void updateStatus(int id, String status) {
        List<Map<String, Object>> assignments = DatabaseManager.query(
            "SELECT operator_id, machine_id FROM task WHERE id = ?", id);
        if (assignments.isEmpty()) {
            Notification.show(I18n.t("Tâche introuvable"));
            return;
        }
        Map<String, Object> assignment = assignments.get(0);
        if ("IN_PROGRESS".equals(status)
                && !"AVAILABLE".equals(getCurrentOperatorStatus())) {
            Notification.show(I18n.t("Reprenez le travail avant de démarrer une tâche"));
            return;
        }
        String sql = "UPDATE task SET status = ?, start_date = "
                + ("IN_PROGRESS".equals(status) ? "COALESCE(start_date, ?)" : "start_date")
                + ", end_date = " + ("COMPLETED".equals(status) ? "?" : "end_date")
                + " WHERE id = ?";
        if ("IN_PROGRESS".equals(status)) {
            DatabaseManager.update(sql, status, new Timestamp(System.currentTimeMillis()), id);
            DatabaseManager.update("UPDATE machine SET status = 'OCCUPIED' WHERE id = ?",
                assignment.get("machine_id"));
            DatabaseManager.update("UPDATE operator SET status = 'OCCUPIED' WHERE id = ?",
                assignment.get("operator_id"));
        } else {
            DatabaseManager.update(sql, status, new Timestamp(System.currentTimeMillis()), id);
            DatabaseManager.update("UPDATE machine SET status = 'AVAILABLE' WHERE id = ?",
                assignment.get("machine_id"));
            DatabaseManager.update("UPDATE operator SET status = 'AVAILABLE' WHERE id = ?",
                assignment.get("operator_id"));
            DatabaseManager.update("""
                UPDATE specialized_goal sg
                JOIN general_goal g ON g.id = sg.general_goal_id
                SET sg.status = 'ACHIEVED'
                WHERE sg.id = (
                    SELECT completed_task.specialized_goal_id
                    FROM task completed_task WHERE completed_task.id = ?
                )
                AND (
                    SELECT COUNT(*) FROM task completed_tasks
                    WHERE completed_tasks.specialized_goal_id = sg.id
                      AND completed_tasks.status = 'COMPLETED'
                ) >= g.target_quantity
                """, id);
        }
        refreshWorkerActions();
        loadData();
    }

    private void addWorkerStatusAction(HorizontalLayout toolbar) {
        refreshWorkerActions();
    }

    private void refreshWorkerActions() {
        if (requestTaskButton == null) {
            return;
        }
        String status = getCurrentOperatorStatus();
        requestTaskButton.setEnabled("AVAILABLE".equals(status));
        pauseButton.setEnabled("AVAILABLE".equals(status));
        resumeButton.setVisible("PAUSED".equals(status));
        resumeButton.setEnabled("PAUSED".equals(status));
    }

    private String getCurrentOperatorStatus() {
        List<Map<String, Object>> rows = DatabaseManager.query("""
            SELECT o.status
            FROM operator o
            WHERE o.user_id = ?
            """, sessionService.getCurrentUserId());
        return rows.isEmpty() ? "OFFLINE" : String.valueOf(rows.get(0).get("status"));
    }

    private void pauseOperator() {
        List<Map<String, Object>> activeTasks = DatabaseManager.query("""
            SELECT t.id
            FROM task t
            JOIN operator o ON o.id = t.operator_id
            WHERE o.user_id = ? AND t.status = 'IN_PROGRESS'
            """, sessionService.getCurrentUserId());
        if (!activeTasks.isEmpty()) {
            Notification.show(I18n.t("Terminez la tâche en cours avant la pause"));
            return;
        }
        updateOperatorStatus("PAUSED");
        refreshWorkerActions();
        loadData();
    }

    private void resumeOperator() {
        if (!"PAUSED".equals(getCurrentOperatorStatus())) {
            return;
        }
        updateOperatorStatus("AVAILABLE");
        refreshWorkerActions();
        loadData();
    }

    private void requestNextTask() {
        if (!"AVAILABLE".equals(getCurrentOperatorStatus())) {
            Notification.show(I18n.t("Reprenez le travail avant de demander une tâche"));
            return;
        }

        String taskId = "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String selectionSql = """
                    SELECT sg.id AS specialized_goal_id, op.id AS operation_id, m.id AS machine_id,
                           o.id AS operator_id
                    FROM specialized_goal sg
                    JOIN general_goal g ON g.id = sg.general_goal_id
                    JOIN operation op ON op.id = sg.operation_id
                    JOIN machine_capability mc ON mc.capability_id = op.capability_id
                    JOIN machine m ON m.id = mc.machine_id
                    JOIN operator_machine om ON om.machine_id = m.id
                    JOIN operator o ON o.id = om.operator_id
                    JOIN app_user u ON u.id = o.user_id
                    WHERE u.id = ?
                      AND o.status = 'AVAILABLE'
                      AND g.status = 'IN_PROGRESS'
                      AND sg.status = 'IN_PROGRESS'
                      AND (
                          SELECT COUNT(*) FROM task sg_tasks
                          WHERE sg_tasks.specialized_goal_id = sg.id
                      ) < g.target_quantity
                      AND NOT EXISTS (
                          SELECT 1
                          FROM specialized_goal previous_goal
                          JOIN operation previous_operation
                              ON previous_operation.id = previous_goal.operation_id
                          WHERE previous_goal.general_goal_id = sg.general_goal_id
                            AND previous_operation.sequence_order < op.sequence_order
                            AND (
                                SELECT COUNT(*) FROM task previous_tasks
                                WHERE previous_tasks.specialized_goal_id = previous_goal.id
                                  AND previous_tasks.status = 'COMPLETED'
                            ) < g.target_quantity
                      )
                      AND m.status IN ('AVAILABLE', 'OPERATIONAL')
                      AND NOT EXISTS (
                          SELECT 1 FROM task active_task
                          WHERE active_task.machine_id = m.id
                            AND active_task.status IN ('PENDING', 'IN_PROGRESS')
                      )
                    ORDER BY m.hourly_cost ASC, m.id ASC, sg.id ASC
                    LIMIT 1
                    FOR UPDATE
                    """;
                Map<String, Object> assignment;
                try (PreparedStatement selection = connection.prepareStatement(selectionSql)) {
                    selection.setInt(1, sessionService.getCurrentUserId());
                    try (ResultSet result = selection.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            Notification.show(I18n.t("Aucune tâche disponible pour le moment"));
                            return;
                        }
                        assignment = Map.of(
                            "specialized_goal_id", result.getInt("specialized_goal_id"),
                            "operation_id", result.getInt("operation_id"),
                            "machine_id", result.getInt("machine_id"),
                            "operator_id", result.getInt("operator_id"));
                    }
                }

                try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO task
                        (task_id, start_date, status, operator_id, machine_id, operation_id, specialized_goal_id)
                    VALUES (?, ?, 'IN_PROGRESS', ?, ?, ?, ?)
                    """)) {
                    insert.setString(1, taskId);
                    insert.setTimestamp(2, now);
                    insert.setInt(3, ((Number) assignment.get("operator_id")).intValue());
                    insert.setInt(4, ((Number) assignment.get("machine_id")).intValue());
                    insert.setInt(5, ((Number) assignment.get("operation_id")).intValue());
                    insert.setInt(6, ((Number) assignment.get("specialized_goal_id")).intValue());
                    insert.executeUpdate();
                }
                updateWithinTransaction(connection, "UPDATE machine SET status = 'OCCUPIED' WHERE id = ?",
                    assignment.get("machine_id"));
                updateWithinTransaction(connection, "UPDATE operator SET status = 'OCCUPIED' WHERE id = ?",
                    assignment.get("operator_id"));
                connection.commit();
                Notification.show(I18n.t("Tâche attribuée"));
                refreshWorkerActions();
                loadData();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de demander une tâche", exception);
        }
    }

    private void updateWithinTransaction(Connection connection, String sql, Object value)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    private void updateOperatorStatus(String status) {
        DatabaseManager.update("""
            UPDATE operator SET status = ?
            WHERE user_id = ?
            """, status, sessionService.getCurrentUserId());
    }

    private void loadData() {
        String baseSql = """
            SELECT t.id, t.task_id, t.status, t.start_date, t.end_date,
                   sg.step, p.code AS product_code,
                   op.description AS operation_description,
                   CONCAT(m.type, ' (#', m.id, ')') AS machine_name,
                   CONCAT(u.first_name, ' ', u.name) AS operator_name
            FROM task t
            JOIN specialized_goal sg ON sg.id = t.specialized_goal_id
            JOIN general_goal g ON g.id = sg.general_goal_id
            LEFT JOIN product p ON p.id = g.product_id
            JOIN operation op ON op.id = t.operation_id
            JOIN machine m ON m.id = t.machine_id
            JOIN operator o ON o.id = t.operator_id
            JOIN app_user u ON u.id = o.user_id
            """;
        List<Map<String, Object>> rows;
        if (sessionService.canManageWorkshop()) {
            rows = DatabaseManager.query(baseSql + " ORDER BY t.status, t.id");
        } else {
            rows = DatabaseManager.query(baseSql
                    + " WHERE o.user_id = ? ORDER BY t.status, t.id",
                    sessionService.getCurrentUserId());
        }
        grid.setItems(rows);
    }
}

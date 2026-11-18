package fr.insa.crusche.webui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Route(value = "stockage", layout = MainLayout.class)
public class StorageView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();
    private final Grid<Map<String, Object>> supplyGrid = new Grid<>();

    public StorageView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Stockage et pièces"));
        Button addLocation = new Button(I18n.t("Ajouter un emplacement"),
            event -> openLocationDialog());
        addLocation.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button addPiece = new Button(I18n.t("Ajouter une pièce"), event -> openPieceDialog());
        addPiece.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button supply = new Button(I18n.t("Commander des pièces"),
            event -> openSupplyDialog());
        Button capacity = new Button(I18n.t("Définir une capacité"),
            event -> openCapacityDialog());
        Button refresh = new Button(I18n.t("Actualiser"), event -> loadData());

        grid.setSizeFull();
        grid.addColumn(row -> row.get("piece_ref")).setHeader(I18n.t("Référence pièce"));
        grid.addColumn(row -> row.get("product_code")).setHeader(I18n.t("Produit"));
        grid.addColumn(row -> row.get("storage_name")).setHeader(I18n.t("Emplacement"));
        grid.addColumn(row -> row.get("status")).setHeader(I18n.t("Statut"));
        grid.addColumn(row -> row.get("max_quantity") == null
                ? I18n.t("Illimité") : row.get("max_quantity"))
            .setHeader(I18n.t("Capacité maximale"));
        grid.addComponentColumn(row -> {
            HorizontalLayout actions = new HorizontalLayout();
            if ("ORDERED".equals(row.get("status"))) {
                actions.add(new Button(I18n.t("Réceptionner"),
                    event -> receivePiece(row)));
            }
            actions.add(new Button(I18n.t("Supprimer"), event -> deletePiece(row)));
            return actions;
        }).setHeader(I18n.t("Actions"));

        supplyGrid.setWidthFull();
        supplyGrid.addColumn(row -> row.get("product_code"))
            .setHeader(I18n.t("Pièce primaire")).setAutoWidth(true);
        supplyGrid.addColumn(row -> row.get("required_quantity"))
            .setHeader(I18n.t("Besoin")).setAutoWidth(true);
        supplyGrid.addColumn(row -> row.get("available_quantity"))
            .setHeader(I18n.t("Stock disponible")).setAutoWidth(true);
        supplyGrid.addColumn(row -> row.get("quantity_to_order"))
            .setHeader(I18n.t("À commander")).setAutoWidth(true);

        add(title, new HorizontalLayout(addLocation, addPiece, supply, capacity, refresh),
            grid, new H3(I18n.t("Besoins de pièces primaires")), supplyGrid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        grid.setItems(DatabaseManager.query("""
            SELECT p.id, p.piece_ref, p.product_id, p.status, pr.code AS product_code,
                   sl.name AS storage_name, sc.max_quantity
            FROM piece p
            JOIN product pr ON pr.id = p.product_id
            LEFT JOIN storage_location sl ON sl.id = p.storage_location_id
            LEFT JOIN storage_capacity sc
                ON sc.storage_location_id = p.storage_location_id
               AND sc.product_id = p.product_id
            ORDER BY p.piece_ref
            """));
        supplyGrid.setItems(DatabaseManager.query("""
            SELECT required_product.code AS product_code,
                   SUM(GREATEST(0, g.target_quantity - COALESCE((
                       SELECT COUNT(*)
                       FROM task completed_first_task
                       WHERE completed_first_task.specialized_goal_id = first_goal.id
                         AND completed_first_task.status = 'COMPLETED'
                   ), 0))) AS required_quantity,
                   (
                       SELECT COUNT(*)
                       FROM piece available_piece
                       WHERE available_piece.product_id = required_product.id
                         AND available_piece.status = 'AVAILABLE'
                         AND available_piece.storage_location_id IS NOT NULL
                   ) AS available_quantity,
                   GREATEST(0, SUM(GREATEST(0, g.target_quantity - COALESCE((
                       SELECT COUNT(*)
                       FROM task completed_first_task
                       WHERE completed_first_task.specialized_goal_id = first_goal.id
                         AND completed_first_task.status = 'COMPLETED'
                   ), 0))) - (
                       SELECT COUNT(*)
                       FROM piece available_piece
                       WHERE available_piece.product_id = required_product.id
                         AND available_piece.status = 'AVAILABLE'
                         AND available_piece.storage_location_id IS NOT NULL
                   )) AS quantity_to_order
            FROM general_goal g
            JOIN routing r ON r.id = g.routing_id
            JOIN operation first_operation ON first_operation.routing_id = r.id
                AND first_operation.sequence_order = (
                    SELECT MIN(operation_order.sequence_order)
                    FROM operation operation_order
                    WHERE operation_order.routing_id = r.id
                )
            LEFT JOIN specialized_goal first_goal
                ON first_goal.general_goal_id = g.id
               AND first_goal.operation_id = first_operation.id
            JOIN product required_product
                ON required_product.id = COALESCE(first_operation.required_product_id, r.product_id)
            WHERE g.status = 'IN_PROGRESS'
            GROUP BY required_product.id, required_product.code
            ORDER BY required_product.code
            """));
    }

    private void openLocationDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Ajouter un emplacement"));
        TextField name = new TextField(I18n.t("Nom"));
        ComboBox<Map<String, Object>> workshop = new ComboBox<>(I18n.t("Atelier"));
        List<Map<String, Object>> workshops = DatabaseManager.query(
            "SELECT id, name FROM workshop ORDER BY name");
        workshop.setItems(workshops);
        workshop.setItemLabelGenerator(row -> String.valueOf(row.get("name")));
        Button save = new Button(I18n.t("Enregistrer"), event -> {
            try {
                if (name.getValue().isBlank() || workshop.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                DatabaseManager.update(
                    "INSERT INTO storage_location (name, workshop_id) VALUES (?, ?)",
                    name.getValue().trim(), workshop.getValue().get("id"));
                dialog.close();
            } catch (RuntimeException exception) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(name, workshop,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private void openPieceDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Ajouter une pièce"));
        TextField reference = new TextField(I18n.t("Référence pièce"));
        ComboBox<Map<String, Object>> product = new ComboBox<>(I18n.t("Produit"));
        product.setItems(DatabaseManager.query("SELECT id, code, description FROM product ORDER BY code"));
        product.setItemLabelGenerator(row -> row.get("code") + " - " + row.get("description"));
        ComboBox<Map<String, Object>> location = new ComboBox<>(I18n.t("Emplacement"));
        location.setItems(DatabaseManager.query(
            "SELECT id, name FROM storage_location ORDER BY name"));
        location.setItemLabelGenerator(row -> String.valueOf(row.get("name")));
        Button save = new Button(I18n.t("Enregistrer"), event -> {
            try {
                if (reference.getValue().isBlank() || product.getValue() == null
                        || location.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                ensureCapacity(product.getValue().get("id"), location.getValue().get("id"));
                DatabaseManager.update("""
                    INSERT INTO piece (piece_ref, product_id, storage_location_id, status)
                    VALUES (?, ?, ?, 'AVAILABLE')
                    """, reference.getValue().trim(), product.getValue().get("id"),
                    location.getValue().get("id"));
                dialog.close();
                loadData();
            } catch (RuntimeException exception) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(reference, product, location,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private void openCapacityDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Définir une capacité"));
        ComboBox<Map<String, Object>> product = new ComboBox<>(I18n.t("Produit"));
        product.setItems(DatabaseManager.query(
            "SELECT id, code, description FROM product ORDER BY code"));
        product.setItemLabelGenerator(row -> row.get("code") + " - " + row.get("description"));
        ComboBox<Map<String, Object>> location = new ComboBox<>(I18n.t("Emplacement"));
        location.setItems(DatabaseManager.query(
            "SELECT id, name FROM storage_location ORDER BY name"));
        location.setItemLabelGenerator(row -> String.valueOf(row.get("name")));
        NumberField maximum = new NumberField(I18n.t("Capacité maximale"));
        Button save = new Button(I18n.t("Enregistrer"), event -> {
            try {
                if (product.getValue() == null || location.getValue() == null
                        || maximum.getValue() == null || maximum.getValue() < 0
                        || maximum.getValue() != Math.rint(maximum.getValue())) {
                    throw new IllegalArgumentException();
                }
                int max = maximum.getValue().intValue();
                int current = currentQuantity(product.getValue().get("id"), location.getValue().get("id"));
                if (current > max) {
                    Notification.show(I18n.t("La capacité est inférieure au stock actuel"));
                    return;
                }
                DatabaseManager.update("""
                    INSERT INTO storage_capacity (storage_location_id, product_id, max_quantity)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE max_quantity = VALUES(max_quantity)
                    """, location.getValue().get("id"), product.getValue().get("id"), max);
                dialog.close();
                loadData();
            } catch (RuntimeException exception) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(location, product, maximum,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private void openSupplyDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Commander des pièces"));
        ComboBox<Map<String, Object>> product = new ComboBox<>(I18n.t("Produit"));
        product.setItems(DatabaseManager.query(
            "SELECT id, code, description FROM product ORDER BY code"));
        product.setItemLabelGenerator(row -> row.get("code") + " - " + row.get("description"));
        NumberField quantity = new NumberField(I18n.t("Quantité (facultatif)"));
        quantity.setValue(1.0);
        TextField prefix = new TextField(I18n.t("Préfixe des références"));
        prefix.setValue("RAW");
        Button save = new Button(I18n.t("Commander"), event -> {
            try {
                if (product.getValue() == null
                        || (quantity.getValue() != null && (quantity.getValue() < 1
                            || quantity.getValue() != Math.rint(quantity.getValue())))
                        || prefix.getValue().isBlank()) {
                    throw new IllegalArgumentException();
                }
                orderPieces(product.getValue().get("id"),
                    quantity.getValue() == null ? 1 : quantity.getValue().intValue(),
                    prefix.getValue().trim());
                dialog.close();
                loadData();
                Notification.show(I18n.t("Commande enregistrée"));
            } catch (RuntimeException exception) {
                Notification.show(exception.getMessage() == null
                    ? I18n.t("Données invalides") : I18n.t(exception.getMessage()));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(product, quantity, prefix,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), save)));
        dialog.open();
    }

    private void orderPieces(Object productId, int quantity, String prefix) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement insert = connection.prepareStatement("""
                        INSERT INTO piece
                            (piece_ref, product_id, status)
                        VALUES (?, ?, 'ORDERED')
                        """)) {
                    for (int index = 1; index <= quantity; index++) {
                        insert.setString(1, prefix + "-" + UUID.randomUUID().toString()
                            .substring(0, 8).toUpperCase());
                        insert.setObject(2, productId);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer la commande", exception);
        }
    }

    private void receivePiece(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("Réceptionner la pièce"));
        ComboBox<Map<String, Object>> location = new ComboBox<>(I18n.t("Emplacement"));
        location.setItems(DatabaseManager.query(
            "SELECT id, name FROM storage_location ORDER BY name"));
        location.setItemLabelGenerator(item -> String.valueOf(item.get("name")));
        Button receive = new Button(I18n.t("Réceptionner"), event -> {
            try {
                if (location.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                ensureCapacity(row.get("product_id"), location.getValue().get("id"));
                DatabaseManager.update("""
                    UPDATE piece
                    SET status = 'AVAILABLE', storage_location_id = ?
                    WHERE id = ? AND status = 'ORDERED'
                    """, location.getValue().get("id"), row.get("id"));
                dialog.close();
                loadData();
            } catch (RuntimeException exception) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        receive.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(location,
            new HorizontalLayout(new Button(I18n.t("Annuler"), event -> dialog.close()), receive)));
        dialog.open();
    }

    private void ensureCapacity(Object productId, Object locationId) {
        List<Map<String, Object>> capacities = DatabaseManager.query("""
            SELECT max_quantity FROM storage_capacity
            WHERE product_id = ? AND storage_location_id = ?
            """, productId, locationId);
        if (!capacities.isEmpty()
                && currentQuantity(productId, locationId)
                    >= ((Number) capacities.get(0).get("max_quantity")).intValue()) {
            throw new IllegalArgumentException("La capacité est insuffisante");
        }
    }

    private int currentQuantity(Object productId, Object locationId) {
        List<Map<String, Object>> rows = DatabaseManager.query("""
            SELECT COUNT(*) AS quantity
            FROM piece
            WHERE product_id = ? AND storage_location_id = ?
              AND status <> 'COMPLETED'
            """, productId, locationId);
        return ((Number) rows.get(0).get("quantity")).intValue();
    }

    private void deletePiece(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM piece WHERE id = ? AND status = 'AVAILABLE'",
                row.get("id"));
            loadData();
        } catch (RuntimeException exception) {
            Notification.show(I18n.t("Impossible de supprimer cette pièce"));
        }
    }
}

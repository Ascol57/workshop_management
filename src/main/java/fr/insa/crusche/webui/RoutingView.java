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

@Route(value = "routing", layout = MainLayout.class)
public class RoutingView extends VerticalLayout {

    private final Grid<Map<String, Object>> grid = new Grid<>();

    public RoutingView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Gamme"));
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
        grid.addColumn(r -> r.get("routing_ref")).setHeader("Référence").setAutoWidth(true);
        grid.addColumn(r -> r.get("product_code")).setHeader(I18n.t("Produit")).setAutoWidth(true);
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
            SELECT r.id, r.routing_ref, r.product_id, p.code AS product_code
            FROM routing r
            JOIN product p ON p.id = r.product_id
            ORDER BY r.routing_ref
            """;
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        grid.setItems(rows);
    }

    private void openDialog(Map<String, Object> row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(row == null ? I18n.t("Ajouter une gamme") : I18n.t("Modifier la gamme"));
        TextField reference = new TextField(I18n.t("Référence"));
        ComboBox<Map<String, Object>> product = new ComboBox<>(I18n.t("Produit"));
        List<Map<String, Object>> products = DatabaseManager.query(
            "SELECT id, code, description FROM product ORDER BY code");
        product.setItems(products);
        product.setItemLabelGenerator(item -> item.get("code") + " - " + item.get("description"));
        reference.setWidthFull();
        product.setWidthFull();
        if (row != null) {
            reference.setValue(String.valueOf(row.get("routing_ref")));
            products.stream()
                .filter(item -> item.get("id").toString().equals(row.get("product_id").toString()))
                .findFirst()
                .ifPresent(product::setValue);
        }
        Button save = new Button(I18n.t("Enregistrer"), e -> {
            try {
                if (product.getValue() == null) {
                    throw new IllegalArgumentException();
                }
                if (row == null) {
                    DatabaseManager.update("INSERT INTO routing (routing_ref, product_id) VALUES (?, ?)",
                        reference.getValue().trim(), product.getValue().get("id"));
                } else {
                    DatabaseManager.update("UPDATE routing SET routing_ref=?, product_id=? WHERE id=?",
                        reference.getValue().trim(), product.getValue().get("id"), row.get("id"));
                }
                dialog.close();
                loadData();
            } catch (RuntimeException ex) {
                Notification.show(I18n.t("Données invalides"));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(reference, product,
            new HorizontalLayout(new Button(I18n.t("Annuler"), e -> dialog.close()), save)));
        dialog.open();
    }

    private void delete(Map<String, Object> row) {
        try {
            DatabaseManager.update("DELETE FROM routing WHERE id = ?", row.get("id"));
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cette gamme"));
        }
    }
}
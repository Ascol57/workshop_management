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
import fr.insa.crusche.model.Workshop;
import fr.ascol.database.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "atelier", layout = MainLayout.class)
public class AtelierView extends VerticalLayout {

    private final Grid<Workshop> grid = new Grid<>(Workshop.class, false);

    public AtelierView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Atelier"));
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setSpacing(true);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);

        Button addButton = new Button(I18n.t("Ajouter"), e -> openWorkshopDialog(null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setEnabled(SessionService.getInstance().canManageWorkshop());
        Button refreshButton = new Button(I18n.t("Actualiser"), e -> loadData());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        toolbar.add(addButton, refreshButton);
        toolbar.expand(refreshButton);

        grid.setSizeFull();
        grid.addColumn(Workshop::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(Workshop::getName).setHeader(I18n.t("Nom")).setAutoWidth(true);
        grid.addComponentColumn(workshop -> new HorizontalLayout(
            new Button(I18n.t("Modifier"), e -> openWorkshopDialog(workshop)),
            new Button(I18n.t("Supprimer"), e -> deleteWorkshop(workshop))
        )).setHeader(I18n.t("Actions")).setAutoWidth(true);

        add(title, toolbar, grid);
        setFlexGrow(1, grid);
        loadData();
    }

    private void loadData() {
        String sql = "SELECT id, name FROM workshop";
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        List<Workshop> workshops = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Workshop w = new Workshop();
            w.setId(((Number) row.get("id")).intValue());
            w.setName((String) row.get("name"));
            workshops.add(w);
        }
        grid.setItems(workshops);
    }

    private void openWorkshopDialog(Workshop workshop) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(workshop == null ? I18n.t("Ajouter un atelier") : I18n.t("Modifier l'atelier"));
        TextField name = new TextField(I18n.t("Nom"));
        name.setWidthFull();
        if (workshop != null) {
            name.setValue(workshop.getName());
        }
        Button cancel = new Button(I18n.t("Annuler"), e -> dialog.close());
        Button save = new Button(I18n.t("Enregistrer"), e -> {
            if (name.getValue().isBlank()) {
                name.setInvalid(true);
                name.setErrorMessage(I18n.t("Le nom est obligatoire"));
                return;
            }
            if (workshop == null) {
                DatabaseManager.update("INSERT INTO workshop (name) VALUES (?)", name.getValue().trim());
            } else {
                DatabaseManager.update("UPDATE workshop SET name = ? WHERE id = ?",
                    name.getValue().trim(), workshop.getId());
            }
            dialog.close();
            loadData();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(new VerticalLayout(name, new HorizontalLayout(cancel, save)));
        dialog.open();
    }

    private void deleteWorkshop(Workshop workshop) {
        try {
            DatabaseManager.update("DELETE FROM workshop WHERE id = ?", workshop.getId());
            loadData();
        } catch (RuntimeException ex) {
            Notification.show(I18n.t("Impossible de supprimer cet atelier"));
        }
    }
}
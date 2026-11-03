package fr.insa.crusche.webui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

@Route(value = "plan-atelier", layout = MainLayout.class)
public class WorkshopMapView extends VerticalLayout {

    private final Div mapCanvas = new Div();
    private final ComboBox<Map<String, Object>> workshopSelector = new ComboBox<>();

    public WorkshopMapView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(I18n.t("Plan de l'atelier"));
        title.getStyle().set("margin", "0");

        workshopSelector.setLabel(I18n.t("Atelier"));
        workshopSelector.setItemLabelGenerator(row -> String.valueOf(row.get("name")));
        workshopSelector.setItems(DatabaseManager.query("SELECT id, name FROM workshop ORDER BY name"));
        workshopSelector.addValueChangeListener(event -> loadLayout());

        mapCanvas.setWidthFull();
        mapCanvas.setHeight("650px");
        mapCanvas.getStyle().set("position", "relative");
        mapCanvas.getStyle().set("background-color", "#f8f9fa");
        mapCanvas.getStyle().set("border", "2px solid #ccc");
        mapCanvas.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        mapCanvas.getStyle().set("overflow", "hidden");

        add(new HorizontalLayout(title, workshopSelector), mapCanvas);
        setFlexGrow(1, mapCanvas);

        List<Map<String, Object>> workshops = DatabaseManager.query(
            "SELECT id, name FROM workshop ORDER BY name");
        if (!workshops.isEmpty()) {
            workshopSelector.setValue(workshops.get(0));
        }
    }

    private void loadLayout() {
        mapCanvas.removeAll();
        Map<String, Object> workshop = workshopSelector.getValue();
        if (workshop == null) {
            return;
        }

        List<Map<String, Object>> machines = DatabaseManager.query(
            "SELECT m.id, m.type, m.x, m.y, m.width, m.height, m.status "
                + "FROM machine m JOIN equipment e ON e.id = m.equipment_id "
                + "WHERE e.workshop_id = ? ORDER BY m.id", workshop.get("id"));

        for (Map<String, Object> machine : machines) {
            addMachine(machine);
        }
    }

    private void addMachine(Map<String, Object> machine) {
        int id = ((Number) machine.get("id")).intValue();
        boolean canEdit = SessionService.getInstance().canManageWorkshop();
        Div machineDiv = new Div();
        machineDiv.setText(machine.get("type") + " (#" + id + ")");
        machineDiv.getElement().setAttribute("draggable", Boolean.toString(canEdit));
        machineDiv.getStyle().set("position", "absolute");
        machineDiv.getStyle().set("left", number(machine.get("x")) + "px");
        machineDiv.getStyle().set("top", number(machine.get("y")) + "px");
        machineDiv.getStyle().set("width", numberOrDefault(machine.get("width"), 140) + "px");
        machineDiv.getStyle().set("height", numberOrDefault(machine.get("height"), 60) + "px");
        machineDiv.getStyle().set("box-sizing", "border-box");
        machineDiv.getStyle().set("padding", "12px");
        machineDiv.getStyle().set("border", "1px solid #333");
        machineDiv.getStyle().set("border-radius", "8px");
        machineDiv.getStyle().set("background-color", color(String.valueOf(machine.get("status"))));
        machineDiv.getStyle().set("cursor", canEdit ? "move" : "default");
        if (canEdit) {
            Div resizeHandle = new Div();
            resizeHandle.getStyle().set("position", "absolute");
            resizeHandle.getStyle().set("right", "0");
            resizeHandle.getStyle().set("bottom", "0");
            resizeHandle.getStyle().set("width", "14px");
            resizeHandle.getStyle().set("height", "14px");
            resizeHandle.getStyle().set("cursor", "nwse-resize");
            resizeHandle.getStyle().set("background", "rgba(0,0,0,0.25)");
            machineDiv.add(resizeHandle);

            machineDiv.getElement().executeJs("""
                const box = $0;
                const handle = box.lastElementChild;
                let mode = null;
                let startX = 0;
                let startY = 0;
                let startLeft = 0;
                let startTop = 0;
                let startWidth = 0;
                let startHeight = 0;
                const begin = (event, resize) => {
                    event.preventDefault();
                    event.stopPropagation();
                    mode = resize ? 'resize' : 'move';
                    startX = event.clientX;
                    startY = event.clientY;
                    startLeft = box.offsetLeft;
                    startTop = box.offsetTop;
                    startWidth = box.offsetWidth;
                    startHeight = box.offsetHeight;
                    document.addEventListener('pointermove', move);
                    document.addEventListener('pointerup', end, {once: true});
                };
                const move = event => {
                    if (mode === 'move') {
                        box.style.left = Math.max(0, startLeft + event.clientX - startX) + 'px';
                        box.style.top = Math.max(0, startTop + event.clientY - startY) + 'px';
                    } else if (mode === 'resize') {
                        box.style.width = Math.max(80, startWidth + event.clientX - startX) + 'px';
                        box.style.height = Math.max(40, startHeight + event.clientY - startY) + 'px';
                    }
                };
                const end = () => {
                    document.removeEventListener('pointermove', move);
                    box.dispatchEvent(new CustomEvent('machine-layout-changed', {
                        bubbles: true,
                        detail: {
                            x: box.offsetLeft,
                            y: box.offsetTop,
                            width: box.offsetWidth,
                            height: box.offsetHeight
                        }
                    }));
                    mode = null;
                };
                box.addEventListener('pointerdown', event => begin(event, false));
                handle.addEventListener('pointerdown', event => begin(event, true));
                """);
            machineDiv.getElement().addEventListener("machine-layout-changed", event -> {
                var data = event.getEventData();
                DatabaseManager.update("UPDATE machine SET x = ?, y = ?, width = ?, height = ? WHERE id = ?",
                    data.get("event.detail.x").asDouble(),
                    data.get("event.detail.y").asDouble(),
                    data.get("event.detail.width").asDouble(),
                    data.get("event.detail.height").asDouble(), id);
                Notification.show(I18n.t("Plan de la machine enregistré"));
            }).addEventData("event.detail.x")
              .addEventData("event.detail.y")
              .addEventData("event.detail.width")
              .addEventData("event.detail.height");
        }
        mapCanvas.add(machineDiv);
    }

    private static float number(Object value) {
        return value instanceof Number ? ((Number) value).floatValue() : 0;
    }

    private static float numberOrDefault(Object value, float defaultValue) {
        return value instanceof Number ? ((Number) value).floatValue() : defaultValue;
    }

    private static String color(String status) {
        return switch (status) {
            case "OPERATIONAL", "AVAILABLE" -> "#d9f7df";
            case "BROKEN" -> "#ffd9d9";
            case "UNDER_MAINTENANCE" -> "#fff1c7";
            default -> "#e5e7eb";
        };
    }
}

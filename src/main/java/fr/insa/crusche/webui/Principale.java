package fr.insa.crusche.webui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fr.ascol.database.DatabaseManager;

import java.util.List;
import java.util.Map;

/**
 * Page d'accueil - redirige selon l'état :
 * - Pas d'utilisateurs → /setup (création admin)
 * - Utilisateurs mais pas connecté → /login
 * - Connecté → tableau de bord
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Workshop Management")
@AnonymousAllowed
public class Principale extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier s'il y a des utilisateurs
        List<Map<String, Object>> users = DatabaseManager.query("SELECT COUNT(*) as cnt FROM app_user");
        long count = users.isEmpty() ? 0 : ((Number) users.get(0).get("cnt")).longValue();
        
        if (count == 0) {
            // Aucun utilisateur → setup initial
            event.forwardTo("setup");
        } else if (!sessionService.isLoggedIn()) {
            // Des utilisateurs existent mais pas connecté → login
            event.forwardTo("login");
        }
        // Sinon : connecté, on affiche le dashboard
    }

    public Principale() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        H1 title = new H1(I18n.t("Tableau de bord"));
        title.getStyle().set("margin", "0");

        Paragraph welcome = new Paragraph();
        var user = sessionService.getCurrentUser();
        if (user.isPresent()) {
            welcome.setText("Bienvenue, " + user.get().prenom + " " + user.get().nom + " !");
        } else {
            welcome.setText("Bienvenue dans Workshop Management");
        }
        welcome.getStyle().set("color", "var(--lumo-secondary-text-color)");
        welcome.getStyle().set("font-size", "var(--lumo-font-size-l)");

        // Quick stats
        VerticalLayout statsLayout = createStatsCards();
        statsLayout.setWidth("100%");
        statsLayout.setMaxWidth("800px");

        add(title, welcome, statsLayout);
        setFlexGrow(1, statsLayout);
    }

    private VerticalLayout createStatsCards() {
        VerticalLayout stats = new VerticalLayout();
        stats.setPadding(false);
        stats.setSpacing(true);
        stats.setWidthFull();

        H3 statsTitle = new H3(I18n.t("Aperçu rapide"));
        statsTitle.getStyle().set("margin", "var(--lumo-space-l) 0 var(--lumo-space-m) 0");

        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");

        addCountCard(cards, I18n.t("Ateliers"), "workshop", VaadinIcon.BUILDING, "var(--lumo-primary-color)");
        addCountCard(cards, I18n.t("Machines"), "machine", VaadinIcon.COG, "var(--lumo-success-color)");
        addCountCard(cards, I18n.t("Produits"), "product", VaadinIcon.PACKAGE, "var(--lumo-error-color)");
        addCountCard(cards, I18n.t("Tâches"), "task", VaadinIcon.TASKS, "var(--lumo-warning-color)");

        stats.add(statsTitle, cards);
        return stats;
    }

    private void addCountCard(HorizontalLayout cards, String label, String table,
                              VaadinIcon icon, String color) {
        var result = DatabaseManager.query("SELECT COUNT(*) as cnt FROM " + table);
        long count = result.isEmpty() ? 0 : ((Number) result.get(0).get("cnt")).longValue();
        cards.add(createStatCard(label, String.valueOf(count), icon, color));
    }

    private Card createStatCard(String title, String value, VaadinIcon icon, String color) {
        Card card = new Card();
        card.setWidth("100%");
        card.setMinWidth("180px");
        card.getStyle().set("flex", "1 1 180px");
        card.getStyle().set("background", "linear-gradient(135deg, " + color + "15, " + color + "05)");
        card.getStyle().set("border-left", "4px solid " + color);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);

        Icon vaadinIcon = new Icon(icon);
        vaadinIcon.setSize("48px");
        vaadinIcon.getStyle().set("color", color);

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "var(--lumo-font-size-xxxlarge)");
        valueSpan.getStyle().set("font-weight", "700");
        valueSpan.getStyle().set("color", color);

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-m)");
        titleSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        content.add(vaadinIcon, valueSpan, titleSpan);
        card.add(content);
        return card;
    }
}
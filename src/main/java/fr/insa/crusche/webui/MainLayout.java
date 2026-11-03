package fr.insa.crusche.webui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private HorizontalLayout headerLayout;
    private VerticalLayout drawerLayout;

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        SessionService sessionService = SessionService.getInstance();
        String path = event.getLocation().getPath();
        if (sessionService.isLoggedIn() && !sessionService.canManageWorkshop()
                && !path.equals("tache") && !path.equals("profil")) {
            event.forwardTo("tache");
            return;
        }
        // Refresh header and drawer on each navigation to reflect current login state
        updateHeader();
        updateDrawer();
    }

    private void createHeader() {
        headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.getStyle().set("padding", "var(--lumo-space-l) var(--lumo-space-xl)");

        addToNavbar(headerLayout);
        updateHeader();
    }

    private void updateHeader() {
        headerLayout.removeAll();

        SessionService sessionService = SessionService.getInstance();

        H1 logo = new H1("Workshop Management - " + I18n.t("Atelier"));
        logo.getStyle().set("font-size", "var(--lumo-font-size-large)");
        logo.getStyle().set("margin", "0");

        DrawerToggle toggle = new DrawerToggle();

        // User menu
        MenuBar userMenu = new MenuBar();
        if (sessionService.isLoggedIn()) {
            MenuItem userItem = userMenu.addItem("");
            HorizontalLayout userLayout = new HorizontalLayout();
            userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            userLayout.setSpacing(true);

            Avatar avatar = new Avatar(sessionService.getCurrentUserPrenom() + " " + sessionService.getCurrentUserNom());
            avatar.setThemeName("xsmall");
            Span name = new Span(sessionService.getCurrentUserPrenom() + " " + sessionService.getCurrentUserNom());
            name.getStyle().set("font-weight", "500");

            userLayout.add(avatar, name);
            userItem.add(userLayout);
            userItem.getSubMenu().addItem(I18n.t("Profil"), e -> getUI().ifPresent(ui -> ui.navigate("profil")));
            userItem.getSubMenu().addItem("Déconnexion", e -> {
                sessionService.logout();
                getUI().ifPresent(ui -> ui.navigate("login"));
            });
        } else {
            userMenu.addItem(I18n.t("Connexion"), e -> getUI().ifPresent(ui -> ui.navigate("login")));
        }

        MenuBar languageMenu = new MenuBar();
        MenuItem languageItem = languageMenu.addItem(I18n.isFrench() ? "FR" : "EN");
        languageItem.getSubMenu().addItem("English", e -> changeLanguage("en"));
        languageItem.getSubMenu().addItem("Français", e -> changeLanguage("fr"));

        headerLayout.add(toggle, logo, userMenu, languageMenu);
        headerLayout.expand(logo);
    }

    private void createDrawer() {
        drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(true);
        drawerLayout.setSpacing(true);
        drawerLayout.setWidthFull();

        addToDrawer(drawerLayout);
        updateDrawer();
    }

    private void updateDrawer() {
        drawerLayout.removeAll();

        SessionService sessionService = SessionService.getInstance();

        if (sessionService.isLoggedIn() && !sessionService.canManageWorkshop()) {
            RouterLink taskLink = new RouterLink(I18n.t("Mes tâches"), fr.insa.crusche.webui.TaskView.class);
            taskLink.getStyle().set("display", "block");
            taskLink.getStyle().set("color", "var(--lumo-primary-text-color)");
            drawerLayout.add(taskLink);
            return;
        }

        // Navigation links - Workshop management system
        RouterLink atelierLink = new RouterLink(I18n.t("Atelier"), fr.insa.crusche.webui.AtelierView.class);
        atelierLink.getStyle().set("display", "block");
        atelierLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink equipementLink = new RouterLink(I18n.t("Équipement"), fr.insa.crusche.webui.EquipementView.class);
        equipementLink.getStyle().set("display", "block");
        equipementLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink machineLink = new RouterLink(I18n.t("Machine"), fr.insa.crusche.webui.MachineView.class);
        machineLink.getStyle().set("display", "block");
        machineLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink capabilityLink = new RouterLink(I18n.t("Capacités"), fr.insa.crusche.webui.CapabilityView.class);
        capabilityLink.getStyle().set("display", "block");
        capabilityLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink produitLink = new RouterLink(I18n.t("Produit"), fr.insa.crusche.webui.ProduitView.class);
        produitLink.getStyle().set("display", "block");
        produitLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink mapLink = new RouterLink(I18n.t("Plan de l'atelier"),
                fr.insa.crusche.webui.WorkshopMapView.class);
        mapLink.getStyle().set("display", "block");
        mapLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink routingLink = new RouterLink(I18n.t("Gamme"), fr.insa.crusche.webui.RoutingView.class);
        routingLink.getStyle().set("display", "block");
        routingLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink operationLink = new RouterLink(I18n.t("Étapes de fabrication"),
                fr.insa.crusche.webui.OperationView.class);
        operationLink.getStyle().set("display", "block");
        operationLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink operatorLink = new RouterLink(I18n.t("Opérateurs"),
                fr.insa.crusche.webui.OperatorView.class);
        operatorLink.getStyle().set("display", "block");
        operatorLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink generalGoalLink = new RouterLink(I18n.t("Objectif Général"), fr.insa.crusche.webui.GeneralGoalView.class);
        generalGoalLink.getStyle().set("display", "block");
        generalGoalLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink taskLink = new RouterLink(I18n.t("Tâche"), fr.insa.crusche.webui.TaskView.class);
        taskLink.getStyle().set("display", "block");
        taskLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        RouterLink decompositionLink = new RouterLink(I18n.t("Décomposition"),
                fr.insa.crusche.webui.DecompositionView.class);
        decompositionLink.getStyle().set("display", "block");
        decompositionLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        drawerLayout.add(atelierLink, equipementLink, machineLink, capabilityLink, produitLink, mapLink,
                routingLink, operationLink, operatorLink, generalGoalLink, decompositionLink, taskLink);

        if (sessionService.isLoggedIn() && sessionService.isAdmin()) {
            RouterLink adminLink = new RouterLink(I18n.t("Administration"), fr.insa.crusche.webui.AdminView.class);
            adminLink.getStyle().set("display", "block");
            adminLink.getStyle().set("color", "var(--lumo-primary-text-color)");
            adminLink.getStyle().set("margin-top", "var(--lumo-space-l)");
            drawerLayout.add(adminLink);
        }

    }

    private void changeLanguage(String locale) {
        I18n.setLocale(locale);
        getUI().ifPresent(ui -> ui.getPage().reload());
    }
}
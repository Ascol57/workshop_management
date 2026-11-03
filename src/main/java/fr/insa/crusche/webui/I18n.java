package fr.insa.crusche.webui;

import com.vaadin.flow.server.VaadinSession;
import java.util.Map;

/**
 * Minimal UI translation service. English is the default language.
 */
public final class I18n {

    private static final String LOCALE_KEY = I18n.class.getName() + ".locale";
    private static final Map<String, String> ENGLISH = Map.ofEntries(
        Map.entry("Atelier", "Workshop"),
        Map.entry("Équipement", "Equipment"),
        Map.entry("Machine", "Machine"),
        Map.entry("Produit", "Product"),
        Map.entry("Gamme", "Routing"),
        Map.entry("Objectif Général", "General goal"),
        Map.entry("Tâche", "Task"),
        Map.entry("Tournois", "Tournaments"),
        Map.entry("Joueurs", "Players"),
        Map.entry("Matchs & Rondes", "Matches & rounds"),
        Map.entry("Administration", "Administration"),
        Map.entry("Connexion", "Login"),
        Map.entry("Inscription", "Sign up"),
        Map.entry("Déconnexion", "Log out"),
        Map.entry("Profil", "Profile"),
        Map.entry("Tableau de bord", "Dashboard"),
        Map.entry("Aperçu rapide", "Quick overview"),
        Map.entry("Ateliers", "Workshops"),
        Map.entry("Machines", "Machines"),
        Map.entry("Produits", "Products"),
        Map.entry("Tâches", "Tasks"),
        Map.entry("Actualiser", "Refresh"),
        Map.entry("Identifiant", "Username"),
        Map.entry("Mot de passe", "Password"),
        Map.entry("Votre login", "Your username"),
        Map.entry("Votre mot de passe", "Your password"),
        Map.entry("Se souvenir de moi", "Remember me"),
        Map.entry("Se connecter", "Log in"),
        Map.entry("Pas encore de compte ? S'inscrire", "No account yet? Sign up"),
        Map.entry("Veuillez remplir tous les champs", "Please fill in all fields"),
        Map.entry("Identifiant ou mot de passe incorrect", "Invalid username or password"),
        Map.entry("Configuration initiale", "Initial setup"),
        Map.entry("Créer l'administrateur et continuer", "Create administrator and continue"),
        Map.entry("Nom", "Last name"),
        Map.entry("Prénom", "First name"),
        Map.entry("Login", "Username"),
        Map.entry("Administrateur", "Administrator"),
        Map.entry("ADMINISTRATEUR", "ADMINISTRATOR"),
        Map.entry("Première configuration", "Initial setup")
        ,Map.entry("Aucun utilisateur trouvé. Créez le compte administrateur initial.",
            "No users found. Create the initial administrator account.")
        ,Map.entry("⚠️ Cette page n'apparaît qu'une seule fois", "⚠️ This page is shown only once")
        ,Map.entry("Informations du compte", "Account information")
        ,Map.entry("Identifiants administrateur", "Administrator credentials")
        ,Map.entry("Identifiant unique pour la connexion", "Unique username used to log in")
        ,Map.entry("Minimum 8 caractères recommandés", "At least 8 characters recommended")
        ,Map.entry("Confirmer le mot de passe", "Confirm password")
        ,Map.entry("Confirmer le mot de passe *", "Confirm password *")
        ,Map.entry("Ce compte aura le rôle ADMINISTRATEUR avec tous les droits sur l'application.",
            "This account will have the ADMINISTRATOR role with full application access.")
        ,Map.entry("Tous les champs obligatoires (*) doivent être remplis",
            "All required fields (*) must be filled in")
        ,Map.entry("Les mots de passe ne correspondent pas", "Passwords do not match")
        ,Map.entry("Le mot de passe doit contenir au moins 8 caractères",
            "The password must contain at least 8 characters")
        ,Map.entry("Administrateur créé avec succès ! Redirection vers la connexion...",
            "Administrator created successfully! Redirecting to login...")
        ,Map.entry("Erreur lors de la création de l'administrateur",
            "Error creating administrator")
        ,Map.entry("Créer un compte", "Create an account")
        ,Map.entry("Identifiant (login)", "Username")
        ,Map.entry("Unique, sera utilisé pour la connexion", "Unique username used to log in")
        ,Map.entry("S'inscrire", "Sign up")
        ,Map.entry("Déjà un compte ? Se connecter", "Already have an account? Log in")
        ,Map.entry("Tous les champs obligatoires doivent être remplis",
            "All required fields must be filled in")
        ,Map.entry("Le mot de passe doit contenir au moins 6 caractères",
            "The password must contain at least 6 characters")
        ,Map.entry("Identifiant déjà utilisé", "Username already in use")
        ,Map.entry("Compte créé avec succès ! Vous pouvez maintenant vous connecter.",
            "Account created successfully! You can now log in.")
        ,Map.entry("Erreur lors de la création du compte", "Error creating account")
        ,Map.entry("Mon Profil", "My profile")
        ,Map.entry("Le login ne peut pas être changé", "The username cannot be changed")
        ,Map.entry("Nom complet complet", "Full name")
        ,Map.entry("Rôle", "Role")
        ,Map.entry("Atelier ID", "Workshop ID")
        ,Map.entry("Erreur : utilisateur non connecté", "Error: user is not logged in")
        ,Map.entry("Gestion des ateliers", "Workshop management")
        ,Map.entry("Gestion des utilisateurs", "User management")
        ,Map.entry("Utilisateurs", "Users")
        ,Map.entry("Migrations BDD", "Database migrations")
        ,Map.entry("Vérifier et appliquer les migrations", "Check and apply migrations")
        ,Map.entry("Clear DB and redo all migrations", "Clear DB and redo all migrations")
        ,Map.entry("Annuler", "Cancel")
        ,Map.entry("Enregistrer", "Save")
        ,Map.entry("Supprimer", "Delete")
        ,Map.entry("Modifier", "Edit")
        ,Map.entry("Ajouter", "Add")
        ,Map.entry("Ajouter un atelier", "Add a workshop")
        ,Map.entry("Modifier l'atelier", "Edit workshop")
        ,Map.entry("Ajouter un équipement", "Add equipment")
        ,Map.entry("Modifier l'équipement", "Edit equipment")
        ,Map.entry("Ajouter une machine", "Add machine")
        ,Map.entry("Modifier la machine", "Edit machine")
        ,Map.entry("Ajouter un produit", "Add product")
        ,Map.entry("Modifier le produit", "Edit product")
        ,Map.entry("Ajouter une gamme", "Add routing")
        ,Map.entry("Modifier la gamme", "Edit routing")
        ,Map.entry("Ajouter un objectif", "Add goal")
        ,Map.entry("Modifier l'objectif", "Edit goal")
        ,Map.entry("Ordres de production", "Production orders")
        ,Map.entry("Quantité à produire", "Quantity to produce")
        ,Map.entry("Gamme de fabrication", "Manufacturing routing")
        ,Map.entry("Réalisé", "Completed")
        ,Map.entry("Restant", "Remaining")
        ,Map.entry("Réinitialiser les objectifs et tâches", "Reset goals and tasks")
        ,Map.entry("Cette action supprimera tous les objectifs généraux, objectifs spécialisés et tâches.",
            "This action will delete all general goals, specialized goals, and tasks.")
        ,Map.entry("Les produits, gammes, machines et utilisateurs seront conservés.",
            "Products, routings, machines, and users will be kept.")
        ,Map.entry("Saisissez RESET pour confirmer", "Type RESET to confirm")
        ,Map.entry("Saisissez exactement RESET pour confirmer", "Type exactly RESET to confirm")
        ,Map.entry("Réinitialiser", "Reset")
        ,Map.entry("Objectifs et tâches réinitialisés", "Goals and tasks reset")
        ,Map.entry("Impossible de réinitialiser les objectifs et tâches",
            "Unable to reset goals and tasks")
        ,Map.entry("Produit à fabriquer", "Product to manufacture")
        ,Map.entry("Ajouter un ordre de production", "Add a production order")
        ,Map.entry("Modifier l'ordre de production", "Edit production order")
        ,Map.entry("Impossible de supprimer cette gamme", "Unable to delete this routing")
        ,Map.entry("Impossible de supprimer cet objectif", "Unable to delete this goal")
        ,Map.entry("Le nom est obligatoire", "The name is required")
        ,Map.entry("Données invalides", "Invalid data")
        ,Map.entry("Impossible de supprimer cet atelier", "Unable to delete this workshop")
        ,Map.entry("Impossible de supprimer cet équipement", "Unable to delete this equipment")
        ,Map.entry("Impossible de supprimer ce produit", "Unable to delete this product")
        ,Map.entry("Impossible de supprimer cette machine", "Unable to delete this machine")
        ,Map.entry("Position de la machine enregistrée", "Machine position saved")
        ,Map.entry("Plan de la machine enregistré", "Machine layout saved")
        ,Map.entry("Capacités", "Capabilities")
        ,Map.entry("Capacités des machines", "Machine capabilities")
        ,Map.entry("Ajouter une capacité", "Add a capability")
        ,Map.entry("Nom de la capacité", "Capability name")
        ,Map.entry("Cette capacité existe déjà", "This capability already exists")
        ,Map.entry("Impossible de supprimer cette capacité", "Unable to delete this capability")
        ,Map.entry("Opérations", "Operations")
        ,Map.entry("Étapes de fabrication", "Manufacturing steps")
        ,Map.entry("Opérations de fabrication", "Manufacturing operations")
        ,Map.entry("Ordre", "Order")
        ,Map.entry("Capacité", "Capability")
        ,Map.entry("Machine compatible", "Compatible machine")
        ,Map.entry("Ajouter une opération", "Add an operation")
        ,Map.entry("Modifier l'opération", "Edit operation")
        ,Map.entry("Durée", "Duration")
        ,Map.entry("Opérateurs", "Operators")
        ,Map.entry("Utilisateur", "User")
        ,Map.entry("Machines autorisées", "Allowed machines")
        ,Map.entry("Ajouter un opérateur", "Add an operator")
        ,Map.entry("Modifier l'opérateur", "Edit operator")
        ,Map.entry("Invitations", "Invitations")
        ,Map.entry("Inviter un utilisateur", "Invite a user")
        ,Map.entry("Adresse e-mail", "Email address")
        ,Map.entry("Générer le lien d'invitation", "Generate invitation link")
        ,Map.entry("Lien d'invitation", "Invitation link")
        ,Map.entry("Lien généré : copiez-le et envoyez-le à la personne invitée",
            "Link generated: copy it and send it to the invited person")
        ,Map.entry("Finaliser votre inscription", "Complete your registration")
        ,Map.entry("Créer mon compte", "Create my account")
        ,Map.entry("Invitation invalide ou expirée", "Invalid or expired invitation")
        ,Map.entry("Informations invalides", "Invalid information")
        ,Map.entry("Compte créé avec succès", "Account created successfully")
        ,Map.entry("Décomposition : Objectif Général → Spécialisé → Tâche",
            "Breakdown: General goal → Specialized goal → Task")
        ,Map.entry("Objectifs Généraux", "General goals")
        ,Map.entry("Quantité cible", "Target quantity")
        ,Map.entry("Statut", "Status")
        ,Map.entry("Objectifs Spécialisés (décomposition du général)",
            "Specialized goals (breakdown of the general goal)")
        ,Map.entry("Étape", "Step")
        ,Map.entry("Objectif général", "General goal")
        ,Map.entry("Tâches (générées par objectif spécialisé)",
            "Tasks (generated by specialized goal)")
        ,Map.entry("ID Tâche", "Task ID")
        ,Map.entry("Démarrer", "Start")
        ,Map.entry("Terminer", "Finish")
        ,Map.entry("Actions", "Actions")
        ,Map.entry("Opérateur", "Operator")
        ,Map.entry("Opération", "Operation")
        ,Map.entry("Objectif Sp.", "Specialized goal")
        ,Map.entry("Décomposer le général sélectionné", "Break down selected general goal")
        ,Map.entry("Étape (ex: préparation, production, finition)",
            "Step (e.g. preparation, production, finishing)")
        ,Map.entry("Créer objectif + tâches", "Create goal + tasks")
        ,Map.entry("Mes tâches", "My tasks")
        ,Map.entry("Demander une tâche", "Request a task")
        ,Map.entry("Partir en pause", "Take a break")
        ,Map.entry("Passer à la tâche suivante", "Go to next task")
        ,Map.entry("Reprendre le travail", "Resume work")
        ,Map.entry("Reprenez le travail avant de démarrer une tâche",
            "Resume work before starting a task")
        ,Map.entry("Terminez la tâche en cours avant la pause",
            "Finish the current task before taking a break")
        ,Map.entry("Aucune tâche suivante disponible", "No next task available")
        ,Map.entry("Aucune tâche disponible pour le moment", "No task available at the moment")
        ,Map.entry("Tâche attribuée", "Task assigned")
        ,Map.entry("Reprenez le travail avant de demander une tâche",
            "Resume work before requesting a task")
        ,Map.entry("Tâche introuvable", "Task not found")
        ,Map.entry("Plan de l'atelier", "Workshop floor plan")
    );

    private I18n() {
    }

    public static String t(String french) {
        return isFrench() ? french : ENGLISH.getOrDefault(french, french);
    }

    public static boolean isFrench() {
        return "fr".equals(getLocale());
    }

    public static String getLocale() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return "en";
        }
        String locale = (String) session.getAttribute(LOCALE_KEY);
        return locale == null ? "en" : locale;
    }

    public static void setLocale(String locale) {
        if (!"fr".equals(locale) && !"en".equals(locale)) {
            throw new IllegalArgumentException("Unsupported locale: " + locale);
        }
        VaadinSession.getCurrent().setAttribute(LOCALE_KEY, locale);
    }
}

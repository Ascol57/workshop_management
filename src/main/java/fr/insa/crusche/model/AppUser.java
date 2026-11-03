package fr.insa.crusche.model;

/**
 * Base user class of the workshop management system.
 * A user belongs to a workshop and can be a WorkshopManager or an Operator.
 */
public class AppUser {

    private int id;
    private String name;
    private String firstName;
    private String login;
    private String passwordHash;
    private Role role;
    private int workshopId;

    public AppUser() {
    }

    public AppUser(int id, String name, String firstName, String login, String passwordHash, Role role, int workshopId) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.workshopId = workshopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }
}

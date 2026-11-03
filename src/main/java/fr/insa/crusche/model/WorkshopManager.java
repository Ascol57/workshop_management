package fr.insa.crusche.model;

/**
 * A workshop manager (ShopManager) is a user who creates general goals,
 * assigns tasks and consults statistics.
 */
public class WorkshopManager {

    private int id;
    private int userId;

    public WorkshopManager() {
    }

    public WorkshopManager(int id, int userId) {
        this.id = id;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

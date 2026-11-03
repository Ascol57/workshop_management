package fr.insa.crusche.model;

/**
 * A general production goal set by the workshop manager for a product.
 */
public class GeneralGoal {

    private int id;
    private int targetQuantity;
    private GoalStatus status;
    private int workshopId;
    private int productId; // Optional link to product
    private java.util.List<SpecializedGoal> specializedGoals = new java.util.ArrayList<>();

    public GeneralGoal() {
    }

    public GeneralGoal(int id, int targetQuantity, GoalStatus status, int workshopId) {
        this.id = id;
        this.targetQuantity = targetQuantity;
        this.status = status;
        this.workshopId = workshopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(int targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public java.util.List<SpecializedGoal> getSpecializedGoals() {
        return specializedGoals;
    }

    public void setSpecializedGoals(java.util.List<SpecializedGoal> specializedGoals) {
        this.specializedGoals = specializedGoals;
    }
}
package fr.insa.crusche.model;

/**
 * An operator works tasks on machines of a workshop.
 */
public class Operator {

    private int id;
    private int userId;
    private float hourlyCost;
    private OperatorStatus status;
    private java.util.List<String> machineRefs = new java.util.ArrayList<>();

    public Operator() {
    }

    public Operator(int id, int userId, float hourlyCost, OperatorStatus status) {
        this.id = id;
        this.userId = userId;
        this.hourlyCost = hourlyCost;
        this.status = status;
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

    public float getHourlyCost() {
        return hourlyCost;
    }

    public void setHourlyCost(float hourlyCost) {
        this.hourlyCost = hourlyCost;
    }

    public OperatorStatus getStatus() {
        return status;
    }

    public void setStatus(OperatorStatus status) {
        this.status = status;
    }

    public java.util.List<String> getMachineRefs() {
        return machineRefs;
    }

    public void setMachineRefs(java.util.List<String> machineRefs) {
        this.machineRefs = machineRefs;
    }
}

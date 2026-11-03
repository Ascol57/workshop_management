package fr.insa.crusche.model;

/**
 * A machine is an equipment that performs operations.
 */
public class Machine {

    private int id;
    private String type;
    private float hourlyCost;
    private float x;
    private float y;
    private MachineStatus status;
    private int equipmentId;

    public Machine() {
    }

    public Machine(int id, String type, float hourlyCost, float x, float y, MachineStatus status, int equipmentId) {
        this.id = id;
        this.type = type;
        this.hourlyCost = hourlyCost;
        this.x = x;
        this.y = y;
        this.status = status;
        this.equipmentId = equipmentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getHourlyCost() {
        return hourlyCost;
    }

    public void setHourlyCost(float hourlyCost) {
        this.hourlyCost = hourlyCost;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public float getReliability() {
        return 0.95f; // Placeholder
    }
}
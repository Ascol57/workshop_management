package fr.insa.crusche.model;

/**
 * An operation is a step in a routing performed on a machine.
 */
public class Operation {

    private int id;
    private String operationRef;
    private String description;
    private String equipmentRef;
    private float duration;
    private int equipmentId;
    private int routingId;

    public Operation() {
    }

    public Operation(int id, String operationRef, String description, String equipmentRef, float duration, int equipmentId, int routingId) {
        this.id = id;
        this.operationRef = operationRef;
        this.description = description;
        this.equipmentRef = equipmentRef;
        this.duration = duration;
        this.equipmentId = equipmentId;
        this.routingId = routingId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperationRef() {
        return operationRef;
    }

    public void setOperationRef(String operationRef) {
        this.operationRef = operationRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEquipmentRef() {
        return equipmentRef;
    }

    public void setEquipmentRef(String equipmentRef) {
        this.equipmentRef = equipmentRef;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getRoutingId() {
        return routingId;
    }

    public void setRoutingId(int routingId) {
        this.routingId = routingId;
    }
}
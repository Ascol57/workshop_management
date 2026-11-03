package fr.insa.crusche.model;

/**
 * A routing (gamme) is a sequence of operations to manufacture a product.
 */
public class Routing {

    private int id;
    private String routingRef;
    private int productId;
    private java.util.List<Operation> operations = new java.util.ArrayList<>();
    private java.util.List<Equipment> equipmentList = new java.util.ArrayList<>();

    public Routing() {
    }

    public Routing(int id, String routingRef, int productId) {
        this.id = id;
        this.routingRef = routingRef;
        this.productId = productId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoutingRef() {
        return routingRef;
    }

    public void setRoutingRef(String routingRef) {
        this.routingRef = routingRef;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public java.util.List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(java.util.List<Operation> operations) {
        this.operations = operations;
    }

    public java.util.List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(java.util.List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public float routingCost() {
        return (float) operations.stream().mapToDouble(Operation::getDuration).sum() * 50f; // placeholder
    }

    public float routingDuration() {
        return (float) operations.stream().mapToDouble(Operation::getDuration).sum();
    }
}
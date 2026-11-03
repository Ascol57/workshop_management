package fr.insa.crusche.model;

/**
 * A workstation groups one or more machines.
 */
public class Workstation {

    private int id;
    private int equipmentId;
    private java.util.List<Machine> machines = new java.util.ArrayList<>();

    public Workstation() {
    }

    public Workstation(int id, int equipmentId) {
        this.id = id;
        this.equipmentId = equipmentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public java.util.List<Machine> getMachines() {
        return machines;
    }

    public void setMachines(java.util.List<Machine> machines) {
        this.machines = machines;
    }
}
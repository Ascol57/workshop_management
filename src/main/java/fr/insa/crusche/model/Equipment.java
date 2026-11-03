package fr.insa.crusche.model;

/**
 * Base equipment entity - abstract, inherited by Machine and Workstation.
 */
public class Equipment {

    private int id;
    private String equipmentRef;
    private String description;
    private int workshopId;

    public Equipment() {
    }

    public Equipment(int id, String equipmentRef, String description, int workshopId) {
        this.id = id;
        this.equipmentRef = equipmentRef;
        this.description = description;
        this.workshopId = workshopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEquipmentRef() {
        return equipmentRef;
    }

    public void setEquipmentRef(String equipmentRef) {
        this.equipmentRef = equipmentRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }
}

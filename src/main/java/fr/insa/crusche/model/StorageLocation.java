package fr.insa.crusche.model;

/**
 * A physical storage location in a workshop.
 */
public class StorageLocation {

    private int id;
    private String name;
    private int workshopId;

    public StorageLocation() {
    }

    public StorageLocation(int id, String name, int workshopId) {
        this.id = id;
        this.name = name;
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

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }
}

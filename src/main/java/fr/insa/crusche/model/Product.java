package fr.insa.crusche.model;

/**
 * A product manufactured in a workshop.
 */
public class Product {

    private int id;
    private String code;
    private String description;
    private int workshopId;

    public Product() {
    }

    public Product(int id, String code, String description, int workshopId) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.workshopId = workshopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
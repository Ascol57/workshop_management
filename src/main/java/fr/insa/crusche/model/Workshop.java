package fr.insa.crusche.model;

/**
 * Atelier (Workshop) - a production workshop that contains equipment, products,
 * users and sets general production goals.
 */
public class Workshop {

    private int id;
    private String name;

    public Workshop() {
    }

    public Workshop(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Workshop(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return name;
    }
}

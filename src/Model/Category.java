package Model;

import javafx.beans.property.SimpleStringProperty;

public class Category {
    private final SimpleStringProperty name;
    private final SimpleStringProperty imagePath;

    public Category() {
        this.name = new SimpleStringProperty();
        this.imagePath = new SimpleStringProperty();
    }

    public Category(String name, String imagePath) {
        this.name = new SimpleStringProperty(name);
        this.imagePath = new SimpleStringProperty(imagePath);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty imagePathProperty() {
        return imagePath;
    }
}

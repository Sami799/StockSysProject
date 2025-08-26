package Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Item {
    private final SimpleStringProperty ID;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty quantity;
    private final SimpleDoubleProperty price;
    private final SimpleDoubleProperty total;
    private final SimpleStringProperty imagePath;
    private final SimpleDoubleProperty discount; // Add discount property
    private IntegerProperty currentQuantity = new SimpleIntegerProperty();
    private IntegerProperty demandedQuantity = new SimpleIntegerProperty();

    // No-argument constructor
    public Item() {
        this.ID = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.quantity = new SimpleDoubleProperty();
        this.price = new SimpleDoubleProperty();
        this.total = new SimpleDoubleProperty();
        this.imagePath = new SimpleStringProperty();
        this.discount = new SimpleDoubleProperty(0); // Initialize discount to 0
    }

    // Constructor with parameters
    @JsonCreator
    public Item(@JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("quantity") double quantity,
                @JsonProperty("price") double price,
                @JsonProperty("total") double total,
                @JsonProperty("imagePath") String imagePath) {
        this.ID = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.total = new SimpleDoubleProperty(total);
        this.imagePath = new SimpleStringProperty(imagePath);
        this.discount = new SimpleDoubleProperty(0); // Initialize discount to 0
    }

    @JsonProperty("id")
    public String getID() {
        return ID.get();
    }
    public void setID(String id) {
        this.ID.set(id);
    }
    public SimpleStringProperty IDProperty() {
        return ID;
    }

    @JsonProperty("name")
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public SimpleStringProperty nameProperty() {
        return name;
    }

    @JsonProperty("quantity")
    public double getQuantity() {
        return quantity.get();
    }
    public void setQuantity(double quantity) {
        this.quantity.set(quantity);
    }
    public SimpleDoubleProperty quantityProperty() {
        return quantity;
    }

    @JsonProperty("price")
    public double getPrice() {
        return price.get();
    }
    public void setPrice(double price) {
        this.price.set(price);
    }
    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    @JsonProperty("total")
    public double getTotal() {
        return total.get();
    }
    public void setTotal(double total) {
        this.total.set(total);
    }
    public SimpleDoubleProperty totalProperty() {
        return total;
    }

    @JsonProperty("imagePath")
    public String getImagePath() {
        return imagePath.get();
    }
    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }
    public SimpleStringProperty imagePathProperty() {
        return imagePath;
    }

    @JsonProperty("discount")
    public double getDiscount() {
        return discount.get();
    }
    public void setDiscount(double discount) {
        this.discount.set(discount);
    }
    public SimpleDoubleProperty discountProperty() {
        return discount;
    }

    public int getCurrentQuantity() {
        return currentQuantity.get();
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity.set(currentQuantity);
    }

    public IntegerProperty currentQuantityProperty() {
        return currentQuantity;
    }

    public int getDemandedQuantity() {
        return demandedQuantity.get();
    }

    public void setDemandedQuantity(int demandedQuantity) {
        this.demandedQuantity.set(demandedQuantity);
    }

    public IntegerProperty demandedQuantityProperty() {
        return demandedQuantity;
    }
}

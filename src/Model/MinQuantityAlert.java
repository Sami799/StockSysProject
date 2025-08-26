package Model;

public class MinQuantityAlert {
    private String id;
    private String quantity;

    public MinQuantityAlert() {
    }

    public MinQuantityAlert(String id, String quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}

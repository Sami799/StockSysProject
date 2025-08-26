package Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class Goods {
    private final SimpleStringProperty goodsId;
    private final SimpleStringProperty supplierId;
    private final SimpleStringProperty supplierName;
    private final SimpleStringProperty phoneNumber;
    private final SimpleStringProperty goodsDate;
    private List<Item> items;
    private final SimpleStringProperty documentsFolderPath;
    private final SimpleStringProperty currentQuantity;
    private final SimpleStringProperty demandedQuantity;

    @JsonCreator
    public Goods(@JsonProperty("goodsId") String goodsId,
                 @JsonProperty("supplierId") String supplierId,
                 @JsonProperty("supplierName") String supplierName,
                 @JsonProperty("phoneNumber") String phoneNumber,
                 @JsonProperty("goodsDate") String goodsDate,
                 @JsonProperty("items") List<Item> items,
                 @JsonProperty("documentsFolderPath") String documentsFolderPath,
                 @JsonProperty("currentQuantity") String currentQuantity,
                 @JsonProperty("demandedQuantity") String demandedQuantity) {
        this.goodsId = new SimpleStringProperty(goodsId);
        this.supplierId = new SimpleStringProperty(supplierId);
        this.supplierName = new SimpleStringProperty(supplierName);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.goodsDate = new SimpleStringProperty(goodsDate);
        this.items = items;
        this.documentsFolderPath = new SimpleStringProperty(documentsFolderPath);
        this.currentQuantity = new SimpleStringProperty(currentQuantity);
        this.demandedQuantity = new SimpleStringProperty(demandedQuantity);
    }

    public String getGoodsId() {
        return goodsId.get();
    }

    public SimpleStringProperty goodsIdProperty() {
        return goodsId;
    }

    public String getSupplierId() {
        return supplierId.get();
    }

    public SimpleStringProperty supplierIdProperty() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName.get();
    }

    public SimpleStringProperty supplierNameProperty() {
        return supplierName;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public SimpleStringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getGoodsDate() {
        return goodsDate.get();
    }

    public SimpleStringProperty goodsDateProperty() {
        return goodsDate;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getDocumentsFolderPath() {
        return documentsFolderPath.get();
    }

    public void setDocumentsFolderPath(String documentsFolderPath) {
        this.documentsFolderPath.set(documentsFolderPath);
    }

    public SimpleStringProperty documentsFolderPathProperty() {
        return documentsFolderPath;
    }

    public String getCurrentQuantity() {
        return currentQuantity.get();
    }

    public void setCurrentQuantity(String currentQuantity) {
        this.currentQuantity.set(currentQuantity);
    }

    public SimpleStringProperty currentQuantityProperty() {
        return currentQuantity;
    }

    public String getDemandedQuantity() {
        return demandedQuantity.get();
    }

    public void setDemandedQuantity(String demandedQuantity) {
        this.demandedQuantity.set(demandedQuantity);
    }

    public SimpleStringProperty demandedQuantityProperty() {
        return demandedQuantity;
    }
}

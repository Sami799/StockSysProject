package Model;

import Model.Item;

import java.util.List;

public class Order {
    private String orderId;
    private String customerID;
    private String customerName;
    private String phoneNumber;
    private double totalPrice;
    private String orderDate;
    private List<Item> items;
    private String panelsImagesFolderPath;

    public Order() {

    }
    public Order(String orderId, String customerID, String customerName, String phoneNumber,
                 double totalPrice, String orderDate, List<Item> items , String panelsImagesFolderPath) {
        this.orderId = orderId;
        this.customerID = customerID;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.items = items;
        this.panelsImagesFolderPath=panelsImagesFolderPath;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
    public String getPanelsImagesFolderPath() {
        return panelsImagesFolderPath;
    }
    public void setPanelsImagesFolderPath(String panelsImagesFolderPath) {
        this.panelsImagesFolderPath = panelsImagesFolderPath;
    }
}

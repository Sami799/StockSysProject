package Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import Utils.SceneUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {

    private static final String JSON_FILE_PATH = "src/history_orders.json";

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker tillDatePicker;

    @FXML
    private TextField customerNameField;

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, String> orderIdColumn;

    @FXML
    private TableColumn<Order, String> customerNameColumn;

    @FXML
    private TableColumn<Order, String> phoneNumberColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private Label totalPriceLabel;

    private ObservableList<Order> ordersList = FXCollections.observableArrayList();

    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        ordersTable.setItems(ordersList);

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        tillDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        customerNameField.textProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
    }

    private void updateOrdersTable() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate tillDate = tillDatePicker.getValue();
        String customerName = customerNameField.getText().toLowerCase();

        if (fromDate == null || tillDate == null) {
            return;
        }

        if (fromDate.isAfter(tillDate)) {
            Stage stage = (Stage)  fromDatePicker.getScene().getWindow();
            Alert alert = new Alert(AlertType.ERROR, "From Date cannot be after Till Date.");
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        calculateTotalPriceAndPopulateTable(fromDate, tillDate, customerName);
    }

    private void calculateTotalPriceAndPopulateTable(LocalDate fromDate, LocalDate tillDate, String customerName) {
        double totalPrice = 0.0;
        ordersList.clear();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE_PATH));

            List<Order> filteredOrders = FXCollections.observableArrayList();

            for (JsonNode orderNode : rootNode) {
                String orderDateStr = orderNode.get("orderDate").asText();
                LocalDate orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);

                if ((orderDate.isEqual(fromDate) || orderDate.isAfter(fromDate)) &&
                        (orderDate.isEqual(tillDate) || orderDate.isBefore(tillDate))) {

                    String orderCustomerName = orderNode.get("customerName").asText().toLowerCase();
                    if (orderCustomerName.contains(customerName)) {
                        double orderTotalPrice = orderNode.get("totalPrice").asDouble();
                        totalPrice += orderTotalPrice;

                        Order order = new Order(
                                orderNode.get("orderId").asText(),
                                orderNode.get("customerName").asText(),
                                orderNode.get("phoneNumber").asText(),
                                orderTotalPrice,
                                orderDateStr
                        );
                        filteredOrders.add(order);
                    }
                }
            }
            ordersList.setAll(filteredOrders);

        } catch (IOException e) {
            e.printStackTrace();
        }

        totalPriceLabel.setText("Total Price: " + totalPrice);
    }

    @FXML
    private void handleBackToHome(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(stage, "/View/HomeScreen.fxml", true);
    }

    public static class Order {
        private String orderId;
        private String customerName;
        private String phoneNumber;
        private double totalPrice;
        private String orderDate;

        public Order(String orderId, String customerName, String phoneNumber, double totalPrice, String orderDate) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.totalPrice = totalPrice;
            this.orderDate = orderDate;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getOrderDate() {
            return orderDate;
        }
    }
}

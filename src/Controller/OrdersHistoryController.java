package Controller;

import Model.Goods;
import Model.Order;
import Utils.SceneUtils;
import com.sun.org.apache.xpath.internal.operations.Or;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersHistoryController {

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, String> orderIdColumn;

    @FXML
    private TableColumn<Order, String> customerNameColumn;

    @FXML
    private TableColumn<Order, String> phoneNumberColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, Void> FoldersColumn;

    @FXML
    private TextField searchBar;

    @FXML
    private DatePicker datePicker;

    private final String jsonFilePath = "src/history_orders.json";
    private ObservableList<Order> ordersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        addViewButtonToTable();
        ordersTable.setItems(loadOrders());

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterOrders());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> filterOrders());
    }
    private void addViewButtonToTable() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<TableColumn<Order, Void>, TableCell<Order, Void>>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<Order, Void>() {

                    private final Button btn = new Button("View");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Order order = getTableView().getItems().get(getIndex());
                            openDocumentsFolder(order.getPanelsImagesFolderPath());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        FoldersColumn.setCellFactory(cellFactory);
    }
    private void openDocumentsFolder(String folderPath) {
        if (folderPath != null && !folderPath.isEmpty()) {
            try {
                File folder = new File(folderPath);
                if (folder.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(folder);
                } else {
                    showAlert("Folder Not Found", "The specified folder does not exist.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while opening the folder.");
            }
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) searchBar.getScene().getWindow();
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private ObservableList<Order> loadOrders() {
        ordersList.clear();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(jsonFilePath));
            List<Order> orders = objectMapper.readValue(inputStream, new TypeReference<List<Order>>() {});
            ordersList.addAll(orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ordersList;
    }

    private void filterOrders() {
        String searchText = searchBar.getText().toLowerCase();
        LocalDate selectedDate = datePicker.getValue();

        ObservableList<Order> filteredOrders = FXCollections.observableArrayList();
        for (Order order : ordersList) {
            boolean matchesDate = (selectedDate == null) || order.getOrderDate().contains(selectedDate.format(DateTimeFormatter.ISO_DATE));
            boolean matchesName = order.getCustomerName().toLowerCase().contains(searchText);
            if (matchesDate && matchesName) {
                filteredOrders.add(order);
            }
        }
        ordersTable.setItems(filteredOrders);
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }

    @FXML
    private void resetButtonClicked(ActionEvent event) {
        ordersTable.setItems(loadOrders());
        searchBar.clear();
        datePicker.setValue(null);
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

package Controller;

import Model.Customer;
import Model.Item;
import Model.Order;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CartController {

    @FXML
    private TableView<Item> cartTable;
    @FXML
    private TableColumn<Item, String> imageColumn;
    @FXML
    private TableColumn<Item, String> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, Integer> quantityColumn;
    @FXML
    private TableColumn<Item, Double> priceColumn;
    @FXML
    private TableColumn<Item, Double> discountColumn;
    @FXML
    private TableColumn<Item, Double> totalColumn;
    @FXML
    private TableColumn<Item, Void> removeColumn;
    @FXML
    private TextField barcodeField;
    @FXML
    private TextField customerField;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private ToolBar customerToolBar;

    private ObservableList<Item> cartItems;
    private final String jsonFilePath = "src/items.json";
    private final String minQuantityAlertFilePath = "src/MinQuantityAlert.json";
    private final String openOrdersFilePath = "src/open_orders.json";
    private final String customersFilePath = "src/customers.json";
    private final String historyOrdersFilePath = "src/history_orders.json";
    private String currentOrderKey;
    private Customer currentCustomer;
    private Order currentOrder;
    private final Map<String, Double> initialQuantitiesMap = new HashMap<>();
    private final Map<String, Double> cartInitialQuantitiesMap = new HashMap<>();
    private final Map<String, Double> minQuantityAlertMap = new HashMap<>();

    @FXML
    public void initialize() {
        cartItems = FXCollections.observableArrayList();
        cartTable.setItems(cartItems);
        loadInitialQuantities();
        loadMinQuantityAlerts();
        imageColumn.setPrefWidth(200);
        idColumn.setPrefWidth(200);
        nameColumn.setPrefWidth(200);
        quantityColumn.setPrefWidth(200);
        priceColumn.setPrefWidth(200);
        discountColumn.setPrefWidth(200);
        totalColumn.setPrefWidth(200);

        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imagePathProperty());
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty((int) cellData.getValue().getQuantity()).asObject());
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        discountColumn.setCellValueFactory(cellData -> cellData.getValue().discountProperty().asObject());
        totalColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(calculateTotalWithDiscount(cellData.getValue())).asObject());

        imageColumn.setCellFactory(param -> new TableCell<Item, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    File imageFile = new File("src/Images", imagePath);
                    if (imageFile.exists()) {
                        imageView.setImage(new Image(imageFile.toURI().toString()));
                    } else {
                        imageView.setImage(new Image("/Images/empty.jpg"));
                    }
                    imageView.setFitWidth(100); // Adjust size as needed
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            int newQuantity = event.getNewValue();
            double availableQuantity = initialQuantitiesMap.getOrDefault(item.getID(), 0.0);
            double initialQuantity = cartInitialQuantitiesMap.getOrDefault(item.getID(), item.getQuantity());
            double quantityChange = newQuantity - initialQuantity;

            if (newQuantity > availableQuantity + initialQuantity) {
                showAlert("Insufficient Stock", "The entered quantity exceeds the available stock.");
                // Revert to the previous quantity
                event.getTableView().refresh();
                return;
            }

            // Only proceed if there's an actual change in quantity
            if (quantityChange != 0) {
                // Update the initial quantity map with the new quantity
                cartInitialQuantitiesMap.put(item.getID(), (double) newQuantity);
                item.setQuantity(newQuantity);
                // Apply the correct quantity change in the JSON file
                updateItemQuantityInJson(item.getID(), -quantityChange);
                updateTotalPrice();
                cartTable.refresh();

                // Update the order with the updated item quantity
                saveOrUpdateCurrentOrder();
            }
        });

        discountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        discountColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            item.setDiscount(event.getNewValue());
            updateTotalPrice();
            cartTable.refresh();

            // Update the order with the updated discount
            saveOrUpdateCurrentOrder();
        });

        removeColumn.setPrefWidth(200);
        addRemoveButtonToTable();
        updateTotalPrice();

        // Set the barcode field action to handle Enter key
        barcodeField.setOnAction(this::addItemClicked);
        barcodeField.setVisible(false);

        customerField.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                openCustomerDialog();
            }
        });
    }

    private void loadInitialQuantities() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (List<Item> items : categories.values()) {
                for (Item item : items) {
                    initialQuantitiesMap.put(item.getID(), item.getQuantity());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMinQuantityAlerts() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(minQuantityAlertFilePath);
            List<Map<String, String>> minQuantityAlerts = objectMapper.readValue(file, new TypeReference<List<Map<String, String>>>() {});
            for (Map<String, String> alert : minQuantityAlerts) {
                minQuantityAlertMap.put(alert.get("id"), Double.parseDouble(alert.get("quantity")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initOrderData(Order order) {
        currentOrder=order;
        currentOrderKey = order.getOrderId();
        currentCustomer = findCustomerByNameAndPhoneNumber(order.getCustomerName(), order.getPhoneNumber());
        cartItems.clear();
        cartItems.addAll(order.getItems());
        for (Item item : order.getItems()) {
            cartInitialQuantitiesMap.put(item.getID(), item.getQuantity());
        }
        customerField.setText(order.getCustomerName());
        customerField.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Show the barcode field and make it ready for input
        barcodeField.setVisible(true);
        barcodeField.requestFocus();

        // Hide the search button
        Button searchButton = (Button) customerToolBar.lookup("#searchButton");
        if (searchButton != null) {
            searchButton.setVisible(false);
        }
        updateTotalPrice();
    }

    // Method to fetch full customer details
    private Customer findCustomerByNameAndPhoneNumber(String name, String phoneNumber) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(customersFilePath);
            if (file.exists()) {
                List<Customer> customers = objectMapper.readValue(file, new TypeReference<List<Customer>>() {});
                for (Customer customer : customers) {
                    if (customer.getName().equals(name) && customer.getPhoneNumber().equals(phoneNumber)) {
                        return customer;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle this case as needed
    }

    private void addRemoveButtonToTable() {
        removeColumn.setCellFactory(param -> new TableCell<Item, Void>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    Item item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                    updateTotalPrice();

                    // Update the order with the item removed
                    saveOrUpdateCurrentOrder();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    @FXML
    private void addItemClicked(ActionEvent event) {
        if (currentCustomer == null) {
            showAlert("Customer Not Selected", "Please select a customer before adding items.");
            return;
        }

        String barcode = barcodeField.getText();
        if (barcode != null && !barcode.isEmpty()) {
            Item item = findItemByBarcode(barcode);
            if (item != null) {
                addToCart(item);
                updateTotalPrice();
                barcodeField.clear();

                // Update the order with the new item and save it
                saveOrUpdateCurrentOrder();
            } else {
                showAlert("Item Not Found", "The item with the provided barcode does not exist.");
            }
        } else {
            showAlert("Invalid Input", "Please enter a valid barcode.");
        }
    }

    @FXML
    private void searchCustomerClicked(ActionEvent event) {
        if (currentOrderKey != null) {
            // If the order key is already set, we are editing an existing order
            return;
        }

        openCustomerDialog(); // This will handle customer selection
    }

    @FXML
    private void openCustomerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/CustomerDialog.fxml")); // Adjust the path as needed
            Parent root = loader.load();
            CustomerDialogController controller = loader.getController();
            controller.setCartController(this); // Set reference to CartController

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
            stage.setTitle("Select Customer");
            stage.setScene(new Scene(root));
            Stage currentStage = (Stage) customerField.getScene().getWindow();
            stage.initOwner(currentStage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSelectedCustomer(Customer customer) {
        currentCustomer = customer;
        customerField.setText(customer.getName());
        customerField.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, null)));
        barcodeField.setVisible(true);
        barcodeField.requestFocus();
    }

    @FXML
    private void checkoutClicked(ActionEvent event) {
        if (currentCustomer == null) {
            showAlert("Customer Not Selected", "Please select a customer before checking out.");
            return;
        }

        // Retrieve existing orders
        List<Order> orders = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(openOrdersFilePath);
            if (file.exists()) {
                orders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Find the existing order date if the order already exists
        String existingDate = null;
        if (currentOrderKey != null) {
            for (Order order : orders) {
                if (order.getOrderId().equals(currentOrderKey)) {
                    existingDate = order.getOrderDate();
                    break;
                }
            }
        }

        String orderDate = getCurrentDate(existingDate);
        Order updatedOrder = new Order(
                currentOrderKey != null ? currentOrderKey : generateOrderId(),
                currentCustomer.getID(),
                currentCustomer.getName(),
                currentCustomer.getPhoneNumber(),
                calculateTotalPrice(),
                orderDate,
                new ArrayList<>(cartItems) ,
                null
        );

        saveOrUpdateOrder(updatedOrder);

        showAlert("Checkout", "Order has been opened for customer: " + currentCustomer.getName());

        // Clear the cart and reset the form
        cartItems.clear();
        updateTotalPrice();
        customerField.clear();
        customerField.setBackground(null);
        if (barcodeField != null) {
            barcodeField.clear();
            barcodeField.setVisible(false);
        }

        currentOrderKey = null;
        currentCustomer = null;

        // Navigate to the home screen
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenOrders.fxml", true);
    }

    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    private String getCurrentDate(String existingDate) {
        if (existingDate != null && !existingDate.isEmpty()) {
            return existingDate;
        } else {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
    }

    private boolean isOrderInOpenOrders(String orderId) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File("src/open_orders.json");
            if (file.exists()) {
                List<Order> orders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
                for (Order existingOrder : orders) {
                    if (existingOrder.getOrderId().equals(orderId)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private double calculateTotalPrice() {
        return cartItems.stream().mapToDouble(Item::getTotal).sum();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        if(!isOrderInOpenOrders(currentOrderKey)){
            revertItemQuantities();
        }
        // Navigate to the home screen
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenOrders.fxml", true);
    }

    private void revertItemQuantities() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (List<Item> items : categories.values()) {
                for (Item item : items) {
                    if (initialQuantitiesMap.containsKey(item.getID())) {
                        item.setQuantity(initialQuantitiesMap.get(item.getID()));
                    }
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Item findItemByBarcode(String barcode) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (List<Item> items : categories.values()) {
                for (Item item : items) {
                    if (item.getID().equals(barcode)) {
                        Item newItem = new Item(item.getID(), item.getName(), 1, item.getPrice(), item.getPrice(), item.getImagePath());
                        newItem.setDiscount(0.0);  // Initialize discount to 0
                        return newItem;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addToCart(Item newItem) {
        boolean itemExists = false;
        double availableQuantity = initialQuantitiesMap.getOrDefault(newItem.getID(), 0.0);

        for (Item item : cartItems) {
            if (item.getID().equals(newItem.getID())) {
                if (item.getQuantity() < availableQuantity) {
                    item.setQuantity(item.getQuantity() + 1);
                } else {
                    showAlert("Insufficient Stock", "Cannot add more than available stock.");
                }
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            if (availableQuantity > 0) {
                newItem.setQuantity(1);
                cartItems.add(newItem);
                newItem.setDiscount(0.0);  // Initialize discount to 0
            } else {
                showAlert("Out of Stock", "Item with ID " + newItem.getID() + " is out of stock!");
            }
        }

        // Track the initial quantity of the item in the cart
        if (!cartInitialQuantitiesMap.containsKey(newItem.getID())) {
            cartInitialQuantitiesMap.put(newItem.getID(), (double) newItem.getQuantity());
        }

        // Update item quantity in JSON file
        updateItemQuantityInJson(newItem.getID(), -1);

        updateTotalPrice();
        cartTable.refresh();
    }

    private void removeFromCart(Item item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cartItems.remove(item);
            cartInitialQuantitiesMap.remove(item.getID()); // Remove from initial quantities map when removed
        }

        // Update item quantity in JSON file
        updateItemQuantityInJson(item.getID(), 1);

        updateTotalPrice();
        cartTable.refresh();

        // Update the order with the item removed
        saveOrUpdateCurrentOrder();
    }

    private void updateItemQuantityInJson(String itemId, double quantityChange) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (List<Item> items : categories.values()) {
                for (Item item : items) {
                    if (item.getID().equals(itemId)) {
                        double newQuantity = item.getQuantity() + quantityChange;
                        if (newQuantity < 0) {
                            showAlert("Insufficient Stock", "Cannot reduce item quantity below zero.");
                            newQuantity = 0;
                        }
                        item.setQuantity(newQuantity);
                        checkAndShowAlerts(itemId, newQuantity);
                        break;
                    }
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkAndShowAlerts(String itemId, double newQuantity) {
        if (newQuantity == 0) {
            showAlert("Out of Stock", "Item with ID " + itemId + " is out of stock!");
        } else if (minQuantityAlertMap.containsKey(itemId) && newQuantity < minQuantityAlertMap.get(itemId)) {
            showAlert("Low Stock Alert", "Item with ID " + itemId + " is getting low on stock!");
        }
    }

    private void updateTotalPrice() {
        for (Item item : cartItems) {
            double discount = item.getDiscount();
            double discountMultiplier = 1 - (discount / 100);
            item.setTotal(item.getQuantity() * item.getPrice() * discountMultiplier);
        }
        double total = cartItems.stream().mapToDouble(Item::getTotal).sum();
        totalPriceLabel.setText(String.format("%.2f", total));
    }

    private double calculateTotalWithDiscount(Item item) {
        double discount = item.getDiscount();
        double discountMultiplier = 1 - (discount / 100);
        return item.getQuantity() * item.getPrice() * discountMultiplier;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) barcodeField.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    private void saveOrUpdateOrder(Order order) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        List<Order> orders = new ArrayList<>();

        // Read existing orders from the JSON file
        try {
            File file = new File(openOrdersFilePath);
            if (file.exists()) {
                orders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update the existing order
        boolean orderExists = false;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId().equals(order.getOrderId())) {
                orders.set(i, order);
                orderExists = true;
                break;
            }
        }

        if (!orderExists) {
            orders.add(order);
        }

        // Write all orders back to the JSON file
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(openOrdersFilePath), orders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveOrUpdateCurrentOrder() {
        if (currentOrderKey != null && currentCustomer != null) {
            String existingDate = null;

            // Find the existing order date if the order already exists
            List<Order> orders = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            try {
                File file = new File(openOrdersFilePath);
                if (file.exists()) {
                    orders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
                    for (Order order : orders) {
                        if (order.getOrderId().equals(currentOrderKey)) {
                            existingDate = order.getOrderDate();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String orderDate = getCurrentDate(existingDate);
            Order updatedOrder = new Order(
                    currentOrderKey,
                    currentCustomer.getID(),
                    currentCustomer.getName(),
                    currentCustomer.getPhoneNumber(),
                    calculateTotalPrice(),
                    orderDate,
                    new ArrayList<>(cartItems),
                    null
            );

            saveOrUpdateOrder(updatedOrder);
        }
    }
}

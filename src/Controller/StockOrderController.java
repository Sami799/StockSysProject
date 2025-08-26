package Controller;

import Model.Goods;
import Model.Item;
import Model.Supplier;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class StockOrderController {

    @FXML
    private TableView<Item> orderGoodsTable;
    @FXML
    private TableColumn<Item, String> orderImageColumn;
    @FXML
    private TableColumn<Item, String> orderIdColumn;
    @FXML
    private TableColumn<Item, String> orderNameColumn;
    @FXML
    private TableColumn<Item, Integer> orderQuantityColumn;
    @FXML
    private TableColumn<Item, Integer> orderDemandedQuantityColumn;
    @FXML
    private TableColumn<Item, Void> orderRemoveColumn;
    @FXML
    private TextField orderBarcodeField;
    @FXML
    private TextField orderSupplierField;
    @FXML
    private Label orderTotalQuantityLabel;
    @FXML
    private ToolBar orderToolBar;

    private ObservableList<Item> orderGoodsItems;
    private final String jsonFilePath = "src/items.json";
    private final String openOrdersFilePath = "src/open_goods.json";
    private Supplier currentSupplier;
    private Goods currentOrder;
    private Map<String, Item> itemMap;

    @FXML
    public void initialize() {
        orderGoodsItems = FXCollections.observableArrayList();
        orderGoodsTable.setItems(orderGoodsItems);
        loadInitialQuantities();
        setupTableColumns();
        setupBarcodeField();
        setupSupplierField();
        orderBarcodeField.setVisible(false); // Barcode field hidden initially
    }

    public void initOrderData(Goods order) {
        this.currentOrder = order;
        orderGoodsItems.setAll(order.getItems());
        updateTotalQuantity();

        // Set the supplier field and style
        this.currentSupplier = new Supplier(
                order.getSupplierId(),
                order.getSupplierName(),
                order.getPhoneNumber()
        );
        orderSupplierField.setText(currentOrder.getSupplierName());
        orderSupplierField.setStyle("-fx-background-color: orange;");
        orderBarcodeField.setVisible(true);
        orderBarcodeField.requestFocus();
    }

    private void loadInitialQuantities() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {
            });
            itemMap = new HashMap<>();
            for (List<Item> items : categories.values()) {
                for (Item item : items) {
                    itemMap.put(item.getID(), item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        orderImageColumn.setPrefWidth(200);
        orderIdColumn.setPrefWidth(200);
        orderNameColumn.setPrefWidth(200);
        orderQuantityColumn.setPrefWidth(200);
        orderDemandedQuantityColumn.setPrefWidth(250);
        orderRemoveColumn.setPrefWidth(200);

        orderImageColumn.setCellValueFactory(cellData -> cellData.getValue().imagePathProperty());
        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        orderNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        orderQuantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty((int) cellData.getValue().getQuantity()).asObject());
        orderDemandedQuantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty((int) cellData.getValue().getDemandedQuantity()).asObject());

        orderImageColumn.setCellFactory(param -> new TableCell<Item, String>() {
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
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        orderDemandedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        orderDemandedQuantityColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            int newQuantity = event.getNewValue();
            item.setDemandedQuantity(newQuantity);
            updateTotalQuantity();
            orderGoodsTable.refresh();
        });

        orderRemoveColumn.setCellFactory(new Callback<TableColumn<Item, Void>, TableCell<Item, Void>>() {
            @Override
            public TableCell<Item, Void> call(final TableColumn<Item, Void> param) {
                final TableCell<Item, Void> cell = new TableCell<Item, Void>() {

                    private final Button btn = new Button("Remove");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Item item = getTableView().getItems().get(getIndex());
                            orderGoodsItems.remove(item);
                            updateTotalQuantity();
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
        });
    }

    private void setupBarcodeField() {
        orderBarcodeField.setOnAction(event -> addItemClicked(event));
    }

    private void setupSupplierField() {
        orderSupplierField.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                openSupplierDialog();
            }
        });
    }

    private void updateTotalQuantity() {
        int totalQuantity = orderGoodsItems.stream().mapToInt(Item::getDemandedQuantity).sum();
        orderTotalQuantityLabel.setText("Total Quantity: " + totalQuantity);
    }

    @FXML
    private void addItemClicked(ActionEvent event) {
        String barcode = orderBarcodeField.getText();
        orderBarcodeField.clear();

        // Check if item already exists
        for (Item item : orderGoodsItems) {
            if (item.getID().equals(barcode)) {
                item.setDemandedQuantity(item.getDemandedQuantity() + 1);
                updateTotalQuantity();
                orderGoodsTable.refresh();
                return;
            }
        }

        // If item does not exist, add a new item
        Item newItem = itemMap.get(barcode);
        if (newItem != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter Demanded Quantity");
            dialog.setHeaderText("Enter the demanded quantity for the item");
            dialog.setContentText("Quantity:");
            Stage stage = (Stage) orderSupplierField.getScene().getWindow();
            dialog.initOwner(stage);
            dialog.showAndWait().ifPresent(quantity -> {
                try {
                    int demandedQuantity = Integer.parseInt(quantity);
                    if (demandedQuantity > 0) {
                        newItem.setDemandedQuantity(demandedQuantity);
                        newItem.setCurrentQuantity(0);  // Ensure currentQuantity is 0 initially
                        orderGoodsItems.add(newItem);
                        updateTotalQuantity();
                    } else {
                        showAlert("Invalid Input", "Quantity must be a positive number.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Quantity must be a number.");
                }
            });
        } else {
            showAlert("Invalid Input", "Item not found.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) orderSupplierField.getScene().getWindow();
        alert.initOwner(stage);
        alert.showAndWait();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }

    public void updateSelectedSupplier(Supplier supplier) {
        this.currentSupplier = supplier;
        orderSupplierField.setText(supplier.getName());
        orderSupplierField.setStyle("-fx-background-color: orange;");
        orderBarcodeField.setVisible(true); // Show the barcode field after selecting a supplier
        orderBarcodeField.requestFocus();
    }

    @FXML
    private void openSupplierDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/SupplierDialog.fxml"));
            Parent root = loader.load();
            SupplierDialogController controller = loader.getController();
            controller.setStockOrderController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Select Supplier");
            stage.setScene(new Scene(root));
            Stage currentStage = (Stage) orderSupplierField.getScene().getWindow();
            stage.initOwner(currentStage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void placeOrderClicked(ActionEvent event) {
        if (currentSupplier == null) {
            showAlert("Supplier Not Selected", "Please select a supplier before placing an order.");
            return;
        }

        Goods newOrder = new Goods(
                generateOrderId(),
                currentSupplier.getID(),
                currentSupplier.getName(),
                currentSupplier.getPhoneNumber(),
                getCurrentDate(),
                new ArrayList<>(orderGoodsItems),
                null,  // Assuming you do not have a documents folder path yet
                "0",  // Current quantity as "0"
                String.valueOf(orderGoodsItems.stream().mapToInt(Item::getDemandedQuantity).sum())  // Sum of demanded quantities as String
        );

        saveOrder(newOrder);
        sendOrderEmail(currentSupplier.getEmail(), newOrder);

        showAlert("Order Placed", "Order has been placed for supplier: " + currentSupplier.getName());

        // Clear the form
        orderGoodsItems.clear();
        updateTotalQuantity();
        orderSupplierField.clear();
        orderSupplierField.setStyle(null);
        orderBarcodeField.clear();
        orderBarcodeField.setVisible(false);

        currentSupplier = null;

        // Navigate to the open orders screen
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenReceivedGoods.fxml", true);
    }

    private String generateOrderId() {
        return "O" + System.currentTimeMillis();
    }

    private String getCurrentDate() {
        return java.time.LocalDate.now().toString();
    }

    private void saveOrder(Goods order) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        List<Goods> openOrders = new ArrayList<>();

        try {
            File file = new File(openOrdersFilePath);
            if (file.exists()) {
                if (file.length() != 0) {
                    openOrders = objectMapper.readValue(file, new TypeReference<List<Goods>>() {
                    });
                } else {
                    System.out.println("Open goods file is empty: " + openOrdersFilePath);
                }
            }

            // Ensure currentQuantity for each item is set to 0
            for (Item item : order.getItems()) {
                item.setCurrentQuantity(0);
            }

            openOrders.add(order);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, openOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendOrderEmail(String recipientEmail, Goods order) {
        final String username = System.getenv("MAILGUN_USERNAME");
        final String password = System.getenv("MAILGUN_API_KEY");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.mailgun.org");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("New Order from ExtraLine");

            StringBuilder emailContent = new StringBuilder();
            emailContent.append("Hello ").append(currentSupplier.getName()).append(",\n\n");
            emailContent.append("We would like to place an order for the following items:\n\n");


            for (Item item : order.getItems()) {
                emailContent.append(item.getName())
                        .append(" (ID: ").append(item.getID()).append(")\n")
                        .append("Quantity: ").append(item.getDemandedQuantity()).append("\n\n");
            }
            emailContent.append("Order ID: ").append(order.getGoodsId()).append("\n\n");
            emailContent.append("Thank you in advance,\n");
            emailContent.append("ExtraLine");

            message.setText(emailContent.toString());

            Transport.send(message);

        }  catch (SendFailedException e) {
            System.err.println("Failed to send email due to server rejection: " + e.getMessage());
            showAlert("Error", "Failed to send email due to server rejection. Please check the recipient's email address.");
            // Additional handling or user notification logic here
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
            showAlert("Error", "Failed to send email due to an unexpected error. Please try again later.");
            // Additional handling or user notification logic here
        }
    }
}

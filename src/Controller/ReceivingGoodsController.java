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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceivingGoodsController {

    @FXML
    private TableView<Item> goodsTable;
    @FXML
    private TableColumn<Item, String> imageColumn;
    @FXML
    private TableColumn<Item, String> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, Integer> quantityColumn;
    @FXML
    private TableColumn<Item, Integer> currentQuantityColumn;
    @FXML
    private TableColumn<Item, Integer> demandedQuantityColumn;
    @FXML
    private TextField supplierField;
    @FXML
    private TextField goodsIdField;
    @FXML
    private TextField barcodeField;
    @FXML
    private Label totalQuantityLabel;
    @FXML
    private ToolBar supplierToolBar;
    private Map<String, Integer> previousQuantities = new HashMap<>();

    private ObservableList<Item> goodsItems;
    private final String jsonFilePath = "src/items.json";
    private final String openGoodsFilePath = "src/open_goods.json";
    private Supplier currentSupplier;
    private Goods currentGoods;
    private Map<String, Item> itemMap;
    private Map<String, Integer> initialQuantities = new HashMap<>();

    @FXML
    public void initialize() {
        goodsItems = FXCollections.observableArrayList();
        goodsTable.setItems(goodsItems);
        loadInitialQuantities();
        setupTableColumns();
        supplierField.setEditable(false);
        goodsIdField.setEditable(false);
    }

    public void initGoodsData(Goods goods) {
        if (goods == null) {
            showAlert("Error", "Goods data is not available.");
            return;
        }

        this.currentGoods = goods;
        goodsItems.setAll(goods.getItems());
        updateTotalQuantity();

        // Set the supplier field and style
        this.currentSupplier = new Supplier(
                goods.getSupplierId(),
                goods.getSupplierName(),
                goods.getPhoneNumber()
        );
        supplierField.setText(currentGoods.getSupplierName());
        goodsIdField.setText(currentGoods.getGoodsId());
        supplierField.setStyle("-fx-background-color: orange;");
        goodsIdField.setStyle("-fx-background-color: orange;");

        // Store initial quantities
        for (Item item : goods.getItems()) {
            initialQuantities.put(item.getID(), item.getCurrentQuantity());
        }
    }

    private void loadInitialQuantities() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
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
        imageColumn.setPrefWidth(200);
        idColumn.setPrefWidth(200);
        nameColumn.setPrefWidth(200);
        quantityColumn.setPrefWidth(200);
        currentQuantityColumn.setPrefWidth(200);
        demandedQuantityColumn.setPrefWidth(200);

        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imagePathProperty());
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Update the quantityColumn to use itemMap for quantities
        quantityColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            Item jsonItem = itemMap.get(item.getID());
            return new SimpleIntegerProperty(jsonItem != null ? (int)jsonItem.getQuantity() : 0).asObject();
        });

        currentQuantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty((int) cellData.getValue().getCurrentQuantity()).asObject());
        demandedQuantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty((int) cellData.getValue().getDemandedQuantity()).asObject());

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
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        currentQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        currentQuantityColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            int newQuantity = event.getNewValue();
            int initialQuantity = initialQuantities.getOrDefault(item.getID(), (int)item.getQuantity());
            if (newQuantity < initialQuantity) {
                showAlert("Invalid Quantity", "Current quantity cannot be less than the initial quantity of " + initialQuantity + ".");
                goodsTable.refresh();
            } else if (newQuantity > item.getDemandedQuantity()) {
                showAlert("Invalid Quantity", "Current quantity cannot be more than the demanded quantity.");
                goodsTable.refresh();
            } else {
                item.setCurrentQuantity(newQuantity);
                updateTotalQuantity();
                goodsTable.refresh();
            }
        });

        demandedQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        demandedQuantityColumn.setOnEditCommit(event -> {
            Item item = event.getRowValue();
            int newDemandedQuantity = event.getNewValue();
            item.setDemandedQuantity(newDemandedQuantity);
            goodsTable.refresh();
        });
    }

    private void updateTotalQuantity() {
        int totalQuantity = goodsItems.stream().mapToInt(Item::getCurrentQuantity).sum();
        totalQuantityLabel.setText("Total Quantity: " + totalQuantity);

        // Load items.json and update the total quantity
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (Item goodsItem : goodsItems) {
                int previousQuantity = previousQuantities.getOrDefault(goodsItem.getID(), (int)goodsItem.getCurrentQuantity());
                int newQuantity = goodsItem.getCurrentQuantity();
                int difference = newQuantity - previousQuantity;

                for (List<Item> items : categories.values()) {
                    for (Item item : items) {
                        if (item.getID().equals(goodsItem.getID())) {
                            item.setQuantity(item.getQuantity() + difference);
                        }
                    }
                }
                previousQuantities.put(goodsItem.getID(), newQuantity);  // Update previous quantity to the new quantity
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBarcodeInput(ActionEvent event) {
        String barcode = barcodeField.getText();
        Item item = goodsItems.stream().filter(i -> i.getID().equals(barcode)).findFirst().orElse(null);
        if (item != null) {
            int newQuantity = item.getCurrentQuantity() + 1;
            if (newQuantity <= item.getDemandedQuantity()) {
                item.setCurrentQuantity(newQuantity);
                updateTotalQuantity();
                goodsTable.refresh();
            } else {
                showAlert("Invalid Quantity", "Current quantity cannot be more than the demanded quantity.");
            }
        } else {
            showAlert("Item Not Found", "The item with ID " + barcode + " is not in the current goods.");
        }
        barcodeField.clear();
    }

    @FXML
    private void receiveGoodsClicked(ActionEvent event) {
        if (currentSupplier == null) {
            showAlert("Supplier Not Selected", "Please select a supplier before receiving goods.");
            return;
        }

        // Confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Quantities");
        confirmationAlert.setHeaderText("Are you sure?");
        confirmationAlert.setContentText("Are you sure about all the quantities you have added?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationAlert.getButtonTypes().setAll(yesButton, noButton);

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                processReceivedGoods(event);
            }
        });
    }

    private void processReceivedGoods(ActionEvent event) {
        currentGoods.setItems(new ArrayList<>(goodsItems));
        currentGoods.setCurrentQuantity(String.valueOf(goodsItems.stream().mapToInt(Item::getCurrentQuantity).sum()));
        currentGoods.setDemandedQuantity(String.valueOf(goodsItems.stream().mapToInt(Item::getDemandedQuantity).sum()));
        updateGoods(currentGoods);
        // Clear the form
        goodsItems.clear();
        updateTotalQuantity();
        supplierField.clear();
        supplierField.setStyle(null);
        goodsIdField.clear();

        currentSupplier = null;

        // Navigate to the home screen
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenReceivedGoods.fxml", true);
    }

    private void updateGoods(Goods updatedGoods) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(openGoodsFilePath);
            List<Goods> openGoods = new ArrayList<>();
            if (file.exists()) {
                openGoods = objectMapper.readValue(file, new TypeReference<List<Goods>>() {});
            }
            for (int i = 0; i < openGoods.size(); i++) {
                if (openGoods.get(i).getGoodsId().equals(updatedGoods.getGoodsId())) {
                    openGoods.set(i, updatedGoods);
                    break;
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, openGoods);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        processReceivedGoods(event);
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenReceivedGoods.fxml", true);
    }

    @FXML
    private void refreshPage(ActionEvent event) {
        loadInitialQuantities();
        goodsTable.refresh();
        updateTotalQuantity();
    }

    public void updateSelectedSupplier(Supplier supplier) {
        this.currentSupplier = supplier;
        supplierField.setText(supplier.getName());
        supplierField.setStyle("-fx-background-color: orange;");
    }
}

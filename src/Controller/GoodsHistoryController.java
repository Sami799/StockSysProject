package Controller;

import Model.Goods;
import Model.Item;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoodsHistoryController {
    @FXML
    private TextField searchBar;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Goods> goodsTable;
    @FXML
    private TableColumn<Goods, String> goodsIdColumn;
    @FXML
    private TableColumn<Goods, String> supplierIdColumn;
    @FXML
    private TableColumn<Goods, String> supplierNameColumn;
    @FXML
    private TableColumn<Goods, String> phoneNumberColumn;
    @FXML
    private TableColumn<Goods, String> goodsDateColumn;
    @FXML
    private TableColumn<Goods, Void> viewColumn;
    @FXML
    private TableColumn<Goods, Void> viewItemsColumn;

    private ObservableList<Goods> goodsHistory;

    private final String historyGoodsFilePath = "src/GoodsHistory.json";

    @FXML
    public void initialize() {
        System.out.println("Initializing GoodsHistoryController...");
        goodsHistory = FXCollections.observableArrayList();
        goodsTable.setItems(goodsHistory);

        goodsIdColumn.setCellValueFactory(new PropertyValueFactory<>("goodsId"));
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        goodsDateColumn.setCellValueFactory(new PropertyValueFactory<>("goodsDate"));

        addViewButtonToTable();
        addViewItemsButtonToTable();
        loadGoodsHistory();
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterGoods());
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> filterGoods());
    }

    private void filterGoods() {
        String searchText = searchBar.getText().toLowerCase();
        LocalDate selectedDate = datePicker.getValue();

        ObservableList<Goods> filteredOrders = FXCollections.observableArrayList();
        for (Goods good : goodsHistory) {
            boolean matchesDate = (selectedDate == null) || good.getGoodsDate().contains(selectedDate.format(DateTimeFormatter.ISO_DATE));
            boolean matchesName = good.getSupplierName().toLowerCase().contains(searchText);
            if (matchesDate && matchesName) {
                filteredOrders.add(good);
            }
        }
        goodsTable.setItems(filteredOrders);
    }

    private void addViewButtonToTable() {
        Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>> cellFactory = new Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>>() {
            @Override
            public TableCell<Goods, Void> call(final TableColumn<Goods, Void> param) {
                final TableCell<Goods, Void> cell = new TableCell<Goods, Void>() {

                    private final Button btn = new Button("View");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Goods selectedGoods = getTableView().getItems().get(getIndex());
                            openDocumentsFolder(selectedGoods.getDocumentsFolderPath());
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

        viewColumn.setCellFactory(cellFactory);
    }

    private void addViewItemsButtonToTable() {
        Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>> cellFactory = new Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>>() {
            @Override
            public TableCell<Goods, Void> call(final TableColumn<Goods, Void> param) {
                final TableCell<Goods, Void> cell = new TableCell<Goods, Void>() {

                    private final Button btn = new Button("View Items");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Goods selectedGoods = getTableView().getItems().get(getIndex());
                            showItemsDialog(selectedGoods);
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

        viewItemsColumn.setCellFactory(cellFactory);
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

    private void loadGoodsHistory() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = Paths.get(historyGoodsFilePath).toFile();
            if (file.exists()) {
                if (file.length() == 0) {
                    System.out.println("History goods file is empty: " + historyGoodsFilePath);
                } else {
                    List<Goods> goodsList = objectMapper.readValue(file, new TypeReference<List<Goods>>() {});
                    goodsHistory.setAll(goodsList);
                    System.out.println("Loaded goods: " + goodsList.size());
                }
            } else {
                System.out.println("History goods file does not exist: " + historyGoodsFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showItemsDialog(Goods goods) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Items for " + goods.getGoodsId());
        dialog.setHeaderText("Items for " + goods.getGoodsId());

        TextArea textArea = new TextArea();
        StringBuilder itemsString = new StringBuilder();
        for (Item item : goods.getItems()) {
            itemsString.append(item.getName()).append(" (Current Quantity: ").append(item.getCurrentQuantity()).append(")\n");
        }
        textArea.setText(itemsString.toString());
        textArea.setEditable(false);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().getStyleClass().add("custom-dialog-pane");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Design.css").toExternalForm());
        Stage stage = (Stage) searchBar.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }
}

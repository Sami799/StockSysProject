package Controller;

import Model.Supplier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SupplierDialogController {

    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, String> IDColumn;
    @FXML
    private TableColumn<Supplier, String> nameColumn;
    @FXML
    private TableColumn<Supplier, String> phoneColumn;
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    @FXML
    private Button selectButton;
    @FXML
    private Button cancelButton;
    private Supplier selectedSupplier;

    private ReceivingGoodsController receivingGoodsController; // Reference to ReceivingGoodsController
    private StockOrderController stockOrderController; // Reference to StockOrderController

    private final String suppliersFilePath = "src/suppliers.json";
    private ObservableList<Supplier> supplierData;

    @FXML
    public void initialize() {
        supplierData = FXCollections.observableArrayList();
        loadSuppliers();

        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        supplierTable.setItems(supplierData);
    }

    private void loadSuppliers() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(suppliersFilePath);
            if (file.exists()) {
                List<Supplier> suppliers = objectMapper.readValue(file, new TypeReference<List<Supplier>>() {});
                supplierData = FXCollections.observableArrayList(suppliers);
                supplierTable.setItems(supplierData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReceivingGoodsController(ReceivingGoodsController receivingGoodsController) {
        this.receivingGoodsController = receivingGoodsController;
    }

    public void setStockOrderController(StockOrderController stockOrderController) {
        this.stockOrderController = stockOrderController;
    }

    public Supplier getSelectedSupplier() {
        return selectedSupplier;
    }

    @FXML
    private void handleSelect() {
        selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        if (selectedSupplier != null) {
            if (receivingGoodsController != null) {
                receivingGoodsController.updateSelectedSupplier(selectedSupplier); // Notify ReceivingGoodsController
            } else if (stockOrderController != null) {
                stockOrderController.updateSelectedSupplier(selectedSupplier); // Notify StockOrderController
            }
            Stage stage = (Stage) selectButton.getScene().getWindow();
            stage.close();
        } else {
            showAlert("You have to select a supplier.");
        }
    }

    @FXML
    private void handleCancel() {
        selectedSupplier = null;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) selectButton.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}

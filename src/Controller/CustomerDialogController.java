package Controller;

import Model.Customer;
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

public class CustomerDialogController {

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> IDColumn;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private Button selectButton;
    @FXML
    private Button cancelButton;
    private Customer selectedCustomer;

    private CartController cartController; // Reference to CartController

    private final String customersFilePath = "src/customers.json";
    private ObservableList<Customer> customerData;

    @FXML
    public void initialize() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadCustomers();
    }

    private void loadCustomers() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(customersFilePath);
            if (file.exists()) {
                List<Customer> customers = objectMapper.readValue(file, new TypeReference<List<Customer>>() {});
                customerData = FXCollections.observableArrayList(customers);
                customerTable.setItems(customerData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    @FXML
    private void handleSelect() {
        if (customerTable != null) {
            selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                cartController.updateSelectedCustomer(selectedCustomer); // Notify CartController
                Stage stage = (Stage) selectButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert("You have to select the item that you want to delete");
                System.out.println("No customer selected.");
            }
        } else {
            System.out.println("Customer table is null.");
        }
    }

    @FXML
    private void handleCancel() {
        selectedCustomer = null;
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


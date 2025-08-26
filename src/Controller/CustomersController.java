package Controller;

import Model.Customer;
import Utils.SceneUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

public class CustomersController {
    @FXML
    private TableView<Customer> tableView;
    @FXML
    private TableColumn<Customer, String> IDColumn;
    @FXML
    private TableColumn<Customer, String> FullNameColumn;
    @FXML
    private TableColumn<Customer, String> PhoneNumberColumn;
    @FXML
    private TableColumn<Customer, String> EmailColumn;
    @FXML
    private TextField searchBar;
    private final String jsonFilePath = "src/customers.json";
    private ObservableList<Customer> customersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        FullNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        PhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        EmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableView.setItems(loadCustomers());

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterCustomers());
    }

    private ObservableList<Customer> loadCustomers() {
        customersList.clear();
        try {
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8).trim();

            if (content.isEmpty()) {
                // If the file is empty, initialize it with an empty JSON array
                content = "[]";
                Files.write(Paths.get(jsonFilePath), content.getBytes(StandardCharsets.UTF_8));
            }

            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Customer customer = new Customer(
                        jsonObject.getString("ID"),
                        jsonObject.getString("Name"),
                        jsonObject.getString("Phone number"),
                        jsonObject.optString("email", "")
                );
                customersList.add(customer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Load Error", "Could not load customer data.");
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            showAlert("Error", "JSON Error", "Invalid JSON format in customer data.");
        }
        return customersList;
    }

    private void filterCustomers() {
        String searchText = searchBar.getText().toLowerCase();
        if (searchText != null && !searchText.isEmpty()) {
            ObservableList<Customer> filteredCustomers = FXCollections.observableArrayList();
            for (Customer customer : customersList) {
                if (customer.getID().toLowerCase().contains(searchText) ||
                        customer.getName().toLowerCase().contains(searchText) ||
                        customer.getPhoneNumber().toLowerCase().contains(searchText) ||
                        customer.getEmail().toLowerCase().contains(searchText)) {
                    filteredCustomers.add(customer);
                }
            }
            tableView.setItems(filteredCustomers);
        } else {
            tableView.setItems(customersList);
        }
    }

    @FXML
    private void addCustomerClicked(ActionEvent event) {
        Stage stage = (Stage) searchBar.getScene().getWindow();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.setTitle("Add Customer");

        VBox dialogContent = new VBox(10);
        dialogContent.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("Phone Number");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        dialogContent.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Phone Number:"), phoneNumberField,
                new Label("Email:"), emailField
        );

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(400, 300);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String id = generateNextId();
                String name = nameField.getText();
                String phoneNumber = phoneNumberField.getText();
                String email = emailField.getText();

                // Validate input
                if (email.isEmpty() || !validateEmail(email)) {
                    showAlert("Invalid Input", "Invalid Email", "Email must be valid.");
                    emailField.clear();
                    continue;
                }
                if (!validateUniqueEmail(email)) {
                    showAlert("Invalid Input", "Duplicate email", "Email already exists.");
                    emailField.clear();
                    continue;
                }
                if (phoneNumber.isEmpty() || !validatePhoneNumber(phoneNumber)) {
                    showAlert("Invalid Input", "Invalid Phone Number", "Phone number must be valid.");
                    phoneNumberField.clear();
                    continue;
                }
                if (!validateUniquePhoneNumber(phoneNumber)) {
                    showAlert("Invalid Input", "Duplicate Phone Number", "Phone number already exists.");
                    phoneNumberField.clear();
                    continue;
                }
                if (name.isEmpty()) {
                    showAlert("Invalid Input", "Empty Name", "Name cannot be empty.");
                    continue;
                }

                Customer newCustomer = new Customer(id, name, phoneNumber, email);
                customersList.add(newCustomer);
                saveCustomersToJson();
                tableView.setItems(customersList); // Refresh the table view
                break; // Exit the loop if input is valid
            } else {
                break; // Exit the loop if cancel is clicked
            }
        }
    }

    private String generateNextId() {
        int maxId = customersList.stream()
                .mapToInt(customer -> Integer.parseInt(customer.getID()))
                .max()
                .orElse(0);
        return String.valueOf(maxId + 1);
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean validateUniqueEmail(String email) {
        return customersList.stream().noneMatch(customer -> customer.getEmail().equals(email));
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        String phoneRegex = "^(050|051|052|053|054|055|058|04)\\d{7}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phoneNumber).matches();
    }

    private boolean validateUniquePhoneNumber(String phoneNumber) {
        return customersList.stream().noneMatch(customer -> customer.getPhoneNumber().equals(phoneNumber));
    }

    private void saveCustomersToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Customer customer : customersList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID", customer.getID());
            jsonObject.put("Name", customer.getName());
            jsonObject.put("Phone number", customer.getPhoneNumber());
            jsonObject.put("email", customer.getEmail());
            jsonArray.put(jsonObject);
        }
        try {
            Files.write(Paths.get(jsonFilePath), jsonArray.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Save Error", "Could not save customer data.");
        }
    }

    private void showAlert(String title, String header, String content) {
        Stage stage = (Stage) searchBar.getScene().getWindow();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void resetButtonClicked(ActionEvent event) {
        tableView.setItems(loadCustomers());
        searchBar.clear();
    }

    @FXML
    private void removeCustomerClicked(ActionEvent event) {
        Customer selectedCustomer = tableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert("No Selection", null, "Please select a customer in the table before attempting to delete.");
            return;
        }
        confirmAndDeleteCustomer(selectedCustomer.getID());
    }

    private void confirmAndDeleteCustomer(String ID) {
        Stage stage = (Stage) searchBar.getScene().getWindow();
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete customer with ID " + ID + "?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.initOwner(stage);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteCustomer(ID);
            }
        });
    }

    private void deleteCustomer(String ID) {
        try {
            JSONArray jsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get(jsonFilePath))));
            JSONArray updatedArray = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject customerObj = jsonArray.getJSONObject(i);
                if (!customerObj.getString("ID").equalsIgnoreCase(ID)) {
                    updatedArray.put(customerObj);
                }
            }
            Files.write(Paths.get(jsonFilePath), updatedArray.toString(4).getBytes(StandardCharsets.UTF_8));
            tableView.setItems(loadCustomers()); // Refresh the table view
            showAlert("Deletion Successful", null, "Customer with ID " + ID + " was successfully deleted.");
        } catch (IOException e) {
            showAlert("Error", "Deletion Failed", "Failed to delete customer due to: " + e.getMessage());
        }
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }
}

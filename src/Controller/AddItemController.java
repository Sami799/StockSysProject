package Controller;

import Model.Item;
import Model.MinQuantityAlert;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddItemController {

    @FXML
    private TextField itemIdField;
    @FXML
    private TextField itemNameField;
    @FXML
    private TextField itemQuantityField;
    @FXML
    private TextField itemDeadlineField;
    @FXML
    private Label fileLabel;
    @FXML
    private Button uploadButton;

    @FXML
    private TextField itemPriceField;

    private String category;
    private File selectedFile;
    private final String jsonFilePath = "src/items.json";
    private final String minQuantityAlertFilePath = "src/MinQuantityAlert.json";
    private final String defaultImagePath = "empty.jpg";  // Set your default image path here

    public void setCategory(String category) {
        this.category = category;
    }

    @FXML
    private void uploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );
        selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                fileLabel.setText(selectedFile.getName());
            } else {
                showAlert("Please select a valid image file (jpg, jpeg, png).");
                selectedFile = null;
                fileLabel.setText("No file selected");
            }
        } else {
            fileLabel.setText("No file selected");
        }
    }

    @FXML
    private void addItemClicked(ActionEvent event) {
        String id = itemIdField.getText();
        String name = itemNameField.getText();
        String quantityStr = itemQuantityField.getText();
        String deadline = itemDeadlineField.getText();
        String priceStr = itemPriceField.getText();
        String imagePath = (selectedFile == null) ? defaultImagePath : saveImage(selectedFile);

        if (id.isEmpty() || name.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            showAlert("All fields except image must be filled.");
            return;
        }
        if (!quantityStr.matches("\\d+") || !priceStr.matches("\\d+(\\.\\d{1,2})?")) {
            showAlert("Quantity and price must be valid numbers.");
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);
        double total = quantity * price;  // Calculate total

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});

            Item newItem = new Item(id, name, quantity, price, total, imagePath);

            // Check if the ID exists in any category
            boolean idExists = categories.values().stream()
                    .flatMap(List::stream)
                    .anyMatch(item -> item.getID().equals(id));
            boolean nameExists = categories.values().stream()
                    .flatMap(List::stream)
                    .anyMatch(item -> item.getName().equalsIgnoreCase(name));

            if (idExists) {
                showAlert("An item with this ID already exists in one of the categories.");
                itemIdField.clear();
            } else if (nameExists) {
                showAlert("An item with this name already exists in one of the categories.");
                itemNameField.clear();
            } else {
                if (categories.containsKey(category)) {
                    List<Item> items = categories.get(category);
                    items.add(newItem);
                    objectMapper.writeValue(file, categories);
                    addMinQuantityAlert(newItem.getID(), deadline, objectMapper);
                    showAlert("Item added successfully.");

                    // Navigate back to ImageDisplay page
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    SceneUtils.changeSceneWithParams(currentStage, "/View/Items.fxml", items, category, true, true);
                } else {
                    showAlert("Category not found.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("An error occurred while adding the item.");
        }
    }
    private void addMinQuantityAlert(String itemId, String deadline, ObjectMapper objectMapper) throws IOException {
        File file = new File(minQuantityAlertFilePath);
        List<MinQuantityAlert> alerts;

        // Read existing alerts or create a new list if the file does not exist
        if (file.exists()) {
            alerts = objectMapper.readValue(file, new TypeReference<List<MinQuantityAlert>>() {});
        } else {
            alerts = new ArrayList<>();
        }

        // Add the new alert
        alerts.add(new MinQuantityAlert(itemId, deadline));

        // Write the updated list back to the file
        objectMapper.writeValue(file, alerts);
    }

    private String saveImage(File imageFile) {
        String destinationPath = "src/Images/" + imageFile.getName();
        try {
            if (!Files.exists(Paths.get(destinationPath))) {
                Files.copy(imageFile.toPath(), Paths.get(destinationPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile.getName(); // Return only the file name
    }

    @FXML
    private void backClicked(ActionEvent event) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});

            List<Item> items = categories.getOrDefault(category, new ArrayList<>());
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneUtils.changeSceneWithParams(currentStage, "/View/Items.fxml", items, category, true, true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("An error occurred while loading the category.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) itemIdField.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}

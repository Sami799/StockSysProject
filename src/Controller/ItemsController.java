package Controller;

import Model.Item;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsController {

    @FXML
    private TextField searchBar;
    private String category;

    private ObservableList<Item> items;

    @FXML
    private TableView<Item> tableView;
    @FXML
    private TableColumn<Item, String> imageColumn;
    @FXML
    private TableColumn<Item, String> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, String> quantityColumn;

    private final String jsonFilePath = "src/items.json";

    public void initData(List<Item> items, String category) {
        imageColumn.setPrefWidth(300);
        idColumn.setPrefWidth(300);
        nameColumn.setPrefWidth(300);
        quantityColumn.setPrefWidth(300);
        this.items = FXCollections.observableArrayList(items);
        this.category = category;
        tableView.setItems(this.items);

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

        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imagePathProperty());
        idColumn.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        quantityColumn.setCellValueFactory(cellData -> Bindings.createStringBinding(() ->
                String.valueOf(cellData.getValue().getQuantity()), cellData.getValue().quantityProperty()));

        // Adjust row height
        tableView.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setPrefHeight(120); // Set your desired row height
            return row;
        });
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @FXML
    public void initialize() {
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void filterItems() {
        if (items == null) {
            showAlert("No items available to search.");
            return;
        }

        String searchText = searchBar.getText();
        if (searchText != null && !searchText.isEmpty()) {
            List<Item> filteredItems = getItemsForCategoryAndSearchText(searchText);

            if (filteredItems.isEmpty()) {
                showAlert("No items found for: " + searchText);
            } else {
                tableView.setItems(FXCollections.observableArrayList(filteredItems));
            }
        } else {
            tableView.setItems(items);
        }
    }

    private List<Item> getItemsForCategoryAndSearchText(String searchText) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});

            // Search for the item by ID or name in the current category
            if (categories.containsKey(category)) {
                return categories.get(category).stream()
                        .filter(item -> item.getID().toLowerCase().contains(searchText.toLowerCase()) ||
                                item.getName().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList(); // Return an empty list in case of an error
        }
    }

    @FXML
    private void resetButtonClicked(ActionEvent event) {
        if (items != null) {
            tableView.setItems(items);
            searchBar.clear();
        }
    }

    @FXML
    private void deleteItemClicked(ActionEvent event) {
        Item selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("You have to select the item that you want to delete");
            return;
        }

        items.remove(selectedItem);
        tableView.setItems(items);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});

            if (categories.containsKey(category)) {
                categories.get(category).removeIf(item -> item.getID().equals(selectedItem.getID()));
                objectMapper.writeValue(file, categories);
                showAlert("Item deleted successfully.");
            } else {
                showAlert("Category not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("An error occurred while deleting the item.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) searchBar.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Categories.fxml", true);  // Adjust the path as necessary
    }

    @FXML
    private void addItemPageClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeSceneWithParams(currentStage, "/View/AddItem.fxml", category, true, true);
    }
}

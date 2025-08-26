package Controller;

import Model.Category;
import Model.Item;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CategoriesController {

    @FXML
    private Label clockLabel;
    @FXML
    private TextField searchBar;
    @FXML
    private Button resetButton;
    @FXML
    private GridPane categoriesGrid;
    private final String jsonFilePath = "src/items.json";
    private final String categoriesJsonFilePath = "src/categories.json";
    private String selectedCategory;
    private int currentRow = 0;
    private int currentCol = 0;

    @FXML
    public void initialize() {
        initClock();
        loadAllCategories();
        categoriesGrid.setPadding(new Insets(5, 20, 0, 20)); // Reduced top padding to move up the buttons
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterCategories());
    }

    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadAllCategories() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(categoriesJsonFilePath);
            List<Category> categories = objectMapper.readValue(file, new TypeReference<List<Category>>() {});
            categoriesGrid.getChildren().clear();
            currentRow = 0;
            currentCol = 0;
            for (Category category : categories) {
                addCategoryButton(category.getName(), category.getImagePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCategoryButton(String categoryName, String imagePath) {
        VBox categoryBox = new VBox(10);
        categoryBox.setAlignment(Pos.CENTER);

        // Check if the image path is valid
        Image image;
        File imageFile = new File("src/Images", imagePath);
        if (!imageFile.exists()) {
            imagePath = "empty.jpg";  // Use the correct relative path for the default image
            image = new Image("file:src/Images/empty.jpg");
        } else {
            image = new Image(imageFile.toURI().toString());
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150); // Increased size
        imageView.setFitHeight(150); // Increased size
        imageView.setPreserveRatio(true);

        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("category-label");

        Button categoryButton = new Button();
        categoryButton.setOnAction(event -> loadCategoryImages(event, categoryName));
        categoryButton.setGraphic(imageView);
        categoryButton.getStyleClass().add("category-button");

        categoryBox.getChildren().addAll(categoryButton, categoryLabel);

        categoriesGrid.add(categoryBox, currentCol, currentRow);
        GridPane.setHalignment(categoryBox, HPos.CENTER);

        currentCol++;
        if (currentCol == 6) {
            currentCol = 0;
            currentRow++;
        }
    }

    @FXML
    private void homePageClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }

    private void loadCategoryImages(ActionEvent event, String category) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        List<Item> imagePaths = getImagePathsForCategory(category);
        SceneUtils.changeSceneWithParams(currentStage, "/View/Items.fxml", imagePaths, category, true, true);
    }

    private List<Item> getImagePathsForCategory(String category) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(jsonFilePath);
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            return categories.getOrDefault(category, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void filterCategories() {
        String searchText = searchBar.getText().toLowerCase();
        if (searchText != null && !searchText.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                File file = new File(categoriesJsonFilePath);
                List<Category> categories = objectMapper.readValue(file, new TypeReference<List<Category>>() {});
                categoriesGrid.getChildren().clear();
                currentRow = 0;
                currentCol = 0;
                for (Category category : categories) {
                    if (category.getName().toLowerCase().contains(searchText)) {
                        addCategoryButton(category.getName(), category.getImagePath());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadAllCategories();
        }
    }

    @FXML
    private void resetButtonClicked(ActionEvent event) {
        searchBar.clear();
        loadAllCategories();
    }

    @FXML
    private void addCategoryClicked(ActionEvent event) {
        // Create the custom dialog.
        Dialog<Pair<String, File>> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Enter the new category details:");

        // Set the button types.
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the category name and image path labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryName = new TextField();
        categoryName.setPromptText("Category Name");
        Button uploadButton = new Button("Upload Image");
        Label fileLabel = new Label("No file selected");

        grid.add(new Label("Category Name:"), 0, 0);
        grid.add(categoryName, 1, 0);
        grid.add(uploadButton, 0, 1);
        grid.add(fileLabel, 1, 1);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        uploadButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                    fileLabel.setText(selectedFile.getName());
                    dialog.setResult(new Pair<>(categoryName.getText(), selectedFile));
                } else {
                    showAlert("Invalid File", "Please select a valid image file (jpg, jpeg, png).");
                    fileLabel.setText("No file selected");
                }
            } else {
                fileLabel.setText("No file selected");
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a category name and image file pair when the Add button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(categoryName.getText(), null); // Make sure this is handled
            }
            return null;
        });
        Stage stage = (Stage) categoriesGrid.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<Pair<String, File>> result = dialog.showAndWait();

        result.ifPresent(categoryImagePair -> {
            String category = categoryImagePair.getKey().trim();
            if (category.isEmpty()) {
                showAlert("Invalid Input", "Category name cannot be empty.");
                return;
            }
            File imageFile = categoryImagePair.getValue();
            String imagePathValue;
            if (imageFile == null) {
                imagePathValue = "empty.jpg"; // Ensure the default image is set correctly
            } else {
                imagePathValue = saveImage(imageFile);
            }
            addCategory(category, imagePathValue);
        });
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

    private void addCategory(String category, String imagePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Update categories.json
            File categoriesFile = new File(categoriesJsonFilePath);
            List<Category> categories = objectMapper.readValue(categoriesFile, new TypeReference<List<Category>>() {});

            // Check if the category already exists in categories.json
            boolean categoryExists = categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(category));
            if (!categoryExists) {
                Category newCategory = new Category(category, imagePath);
                categories.add(newCategory);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(categoriesFile, categories);

                addCategoryButton(category, imagePath);  // Add the new category to the UI immediately
                showAlert("Category Added", "The category '" + category + "' has been added.");
            } else {
                showAlert("Category Exists", "The category '" + category + "' already exists in categories.");
            }

            // Update items.json
            File itemsFile = new File(jsonFilePath);
            Map<String, List<Item>> items = objectMapper.readValue(itemsFile, new TypeReference<Map<String, List<Item>>>() {});

            // Check if the category already exists in items.json
            if (!items.containsKey(category)) {
                items.put(category, new ArrayList<>()); // Add an empty list for the new category
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(itemsFile, items);
            } else {
                showAlert("Category Exists", "The category '" + category + "' already exists in items.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while adding the category.");
        }
    }

    @FXML
    private void removeCategoryClicked(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Category");
        dialog.setHeaderText("Enter the category name to remove:");
        dialog.setContentText("Category Name:");
        Stage stage = (Stage) categoriesGrid.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::removeCategory);
    }

    private void removeCategory(String category) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Remove from items.json
            File itemsFile = new File(jsonFilePath);
            Map<String, List<Item>> items = objectMapper.readValue(itemsFile, new TypeReference<Map<String, List<Item>>>() {});

            boolean itemRemoved = false;
            if (items.containsKey(category)) {
                items.remove(category);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(itemsFile, items);
                itemRemoved = true;
            }

            // Remove from categories.json
            File categoriesFile = new File(categoriesJsonFilePath);
            List<Category> categories = objectMapper.readValue(categoriesFile, new TypeReference<List<Category>>() {});

            boolean categoryRemoved = categories.removeIf(c -> c.getName().equalsIgnoreCase(category));
            if (categoryRemoved) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(categoriesFile, categories);
            }

            // Refresh the categories view if either removal was successful
            if (itemRemoved || categoryRemoved) {
                loadAllCategories();
                showAlert("Category Removed", "The category '" + category + "' has been removed.");
            } else {
                showAlert("Category Not Found", "The category '" + category + "' does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while removing the category.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) searchBar.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        searchBar.clear();
        alert.showAndWait();
    }
}

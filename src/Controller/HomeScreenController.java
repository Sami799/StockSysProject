package Controller;

import Utils.SceneUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeScreenController {

    @FXML
    private Label clockLabel;
    @FXML
    private Label userLabel;
    @FXML
    private VBox buttonContainer; // Container for buttons
    @FXML
    private TextField searchField;

    private final String minQuantityAlertFilePath = "src/MinQuantityAlert.json";
    private final String itemsFilePath = "src/items.json";

    public void initialize() {
        initClock();
        String loggedInUsername = UserData.getLoggedInUsername();
        setUserLabel(loggedInUsername);
        configureButtons(loggedInUsername);
        startQuantityCheck();
    }

    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void startQuantityCheck() {
        Timeline quantityCheck = new Timeline(new KeyFrame(Duration.ZERO, e -> checkQuantities()), new KeyFrame(Duration.minutes(1)));
        quantityCheck.setCycleCount(Animation.INDEFINITE);
        quantityCheck.play();
    }

    private void checkQuantities() {
        try {
            String itemsJsonContent = new String(Files.readAllBytes(Paths.get(itemsFilePath)));
            String minQuantityAlertJsonContent = new String(Files.readAllBytes(Paths.get(minQuantityAlertFilePath)));

            JSONObject itemsJson = new JSONObject(itemsJsonContent);
            JSONArray minQuantityAlertArray = new JSONArray(minQuantityAlertJsonContent);

            boolean lowStockFound = false;

            for (int i = 0; i < minQuantityAlertArray.length(); i++) {
                JSONObject minQuantityAlert = minQuantityAlertArray.getJSONObject(i);
                String minQuantityAlertId = minQuantityAlert.getString("id");
                int minQuantity = minQuantityAlert.getInt("quantity");

                for (String key : itemsJson.keySet()) {
                    JSONArray itemsArray = itemsJson.getJSONArray(key);
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject item = itemsArray.getJSONObject(j);
                        String itemId = item.getString("id");
                        int itemQuantity = item.getInt("quantity");

                        if (itemId.equals(minQuantityAlertId) && itemQuantity < minQuantity) {
                            lowStockFound = true;
                            break;
                        }
                    }
                    if (lowStockFound) break;
                }
                if (lowStockFound) break;
            }

            if (lowStockFound) {
                // Adjust button style here dynamically
                buttonContainer.lookup("#btnNotifications").setStyle("-fx-background-color: red;");
            } else {
                buttonContainer.lookup("#btnNotifications").setStyle("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUserLabel(String username) {
        try {
            String workersJsonContent = new String(Files.readAllBytes(Paths.get("src/users.json")));
            JSONArray workersArray = new JSONArray(workersJsonContent);

            for (int i = 0; i < workersArray.length(); i++) {
                JSONObject worker = workersArray.getJSONObject(i);
                if (username.equals(worker.getString("username"))) {
                    userLabel.setText("Logged in as: " + worker.getString("username"));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchItem() {
        String barcode = searchField.getText();
        if (barcode.isEmpty()) {
            showAlert("Error", "Please enter a barcode.");
            return;
        }

        try {
            String itemsJsonContent = new String(Files.readAllBytes(Paths.get(itemsFilePath)));
            JSONObject itemsJson = new JSONObject(itemsJsonContent);

            JSONObject foundItem = null;

            for (String key : itemsJson.keySet()) {
                JSONArray itemsArray = itemsJson.getJSONArray(key);
                for (int j = 0; j < itemsArray.length(); j++) {
                    JSONObject item = itemsArray.getJSONObject(j);
                    if (barcode.equals(item.getString("id"))) {
                        foundItem = item;
                        break;
                    }
                }
                if (foundItem != null) break;
            }

            if (foundItem != null) {
                showItemDetails(foundItem);
            } else {
                showAlert("Item Not Found", "No item found with the given barcode.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while searching for the item.");
        }
    }

    private void showItemDetails(JSONObject item) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Item Details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        // Image handling
        ImageView imageView = new ImageView();
        try {
            String imagePath = item.getString("imagePath"); // Image path from JSON
            File imageFile = new File("src/Images", imagePath); // Ensure this path is correct
            Image image = new Image(imageFile.toURI().toString());
            imageView.setImage(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            grid.add(imageView, 0, 0, 2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change label color to white and font size to 14px
        Label nameValueLabel = new Label(item.getString("name"));
        nameValueLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change value color to white and font size to 14px

        Label quantityLabel = new Label("Quantity:");
        quantityLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change label color to white and font size to 14px
        Label quantityValueLabel = new Label(String.valueOf(item.getDouble("quantity")));
        quantityValueLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change value color to white and font size to 14px

        Label priceLabel = new Label("Price:");
        priceLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change label color to white and font size to 14px
        Label priceValueLabel = new Label(String.valueOf(item.getDouble("price")));
        priceValueLabel.setStyle("-fx-text-fill: black; -fx-font-size: 22px; -fx-font-weight: bold"); // Change value color to white and font size to 14px

        grid.add(nameLabel, 0, 1);
        grid.add(nameValueLabel, 1, 1);

        grid.add(quantityLabel, 0, 2);
        grid.add(quantityValueLabel, 1, 2);

        grid.add(priceLabel, 0, 3);
        grid.add(priceValueLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);
        //dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStyleClass().add("root1");
        dialog.getDialogPane().getStyleClass().add("custom-dialog-pane");
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        dialog.getDialogPane().lookupButton(closeButtonType).getStyleClass().add("button-close");
       dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Design.css").toExternalForm());
        Stage stage = (Stage) searchField.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        searchField.clear();
        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) searchField.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    @FXML
    private void configureButtons(String username) {
        buttonContainer.getChildren().clear();

        HBox searchBox = new HBox();
        searchBox.setAlignment(javafx.geometry.Pos.CENTER);
        searchBox.setSpacing(10);

        searchField = new TextField();
        searchField.setPromptText("Search by Barcode");
        searchField.getStyleClass().add("search-bar");
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchItem();
            }
        });

        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> searchItem());
        searchButton.getStyleClass().add("search-button");

        searchBox.getChildren().addAll(searchField, searchButton);
        buttonContainer.getChildren().add(searchBox);

        HBox firstRow = new HBox(10);
        firstRow.setAlignment(javafx.geometry.Pos.CENTER);
        Button btnReports = createButton("Sales Report", event -> reportsClicked(event));
        firstRow.getChildren().add(btnReports);

        if (username.equals("admin")) {
            Button btnWorkers = createButton("Workers", event -> workersClicked(event));
            firstRow.getChildren().add(btnWorkers);
        }

        HBox secondRow = new HBox(10);
        secondRow.setAlignment(javafx.geometry.Pos.CENTER);
        Button btnItems = createButton("Category", event -> CategoriesClicked(event));
        Button btnNotifications = createButton("Low on Stock", event -> notificationsClicked(event));
        btnNotifications.setId("btnNotifications"); // Assign an ID for styling purposes
        secondRow.getChildren().addAll(btnItems, btnNotifications);

        HBox thirdRow = new HBox(10);
        thirdRow.setAlignment(javafx.geometry.Pos.CENTER);
        Button cartBtn = createButton("Cart", event -> cartClicked(event));
        Button ordersHistoryBtn = createButton("Orders history", event -> ordersHistoryClicked(event));
        Button customersBtn = createButton("Customers", event -> CustomersBtnClicked(event));
        thirdRow.getChildren().addAll(cartBtn, ordersHistoryBtn, customersBtn);

        HBox fourthRow = new HBox(10);
        fourthRow.setAlignment(javafx.geometry.Pos.CENTER);
        Button btnGoodsReceive = createButton("Goods Receive", event -> goodsReceiveClicked(event));
        Button btnGoodsHistory = createButton("Goods History", event -> goodsHistoryClicked(event));
        Button btnSuppliers = createButton("Suppliers", event -> suppliersClicked(event));
        Button orderStockBtn = createButton("Order Stock", event -> orderStock(event));
        fourthRow.getChildren().addAll(btnGoodsReceive, btnGoodsHistory, btnSuppliers, orderStockBtn);

        Button btnLogout = createButton("Logout", event -> LogoutClicked(event));
        btnLogout.getStyleClass().add("large-menu-button");
        btnLogout.setId("btnLogout"); // Assign an ID for styling purposes

        buttonContainer.getChildren().addAll(firstRow, secondRow, thirdRow, fourthRow, btnLogout);
    }

    private Button createButton(String text, EventHandler<ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.setOnAction(eventHandler);
        button.getStyleClass().add("large-menu-button");
        return button;
    }

    @FXML
    private void workersClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Workers.fxml", true);
    }

    @FXML
    private void ordersHistoryClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OrdersHistory.fxml", true);
    }

    @FXML
    private void CategoriesClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Categories.fxml", true);
    }

    @FXML
    private void notificationsClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/MinQuantityAlerts.fxml", true);
    }

    @FXML
    private void LogoutClicked(ActionEvent event) {
        Stage stage = (Stage) buttonContainer.getScene().getWindow();
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.initOwner(stage);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to Logout?");
        confirmationAlert.setContentText("");

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmationAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                logout(); // Clear the saved credentials
                SceneUtils.changeScene(stage, "/View/Login.fxml", true);
            }
        });
    }


    private void logout() {
        try {
            Files.deleteIfExists(Paths.get("src/credentials.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void CustomersBtnClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Customers.fxml", true);
    }

    @FXML
    private void cartClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenOrders.fxml", true); // Adjust the path as necessary
    }

    @FXML
    private void reportsClicked(ActionEvent event) {
        SceneUtils.changeScene((Stage) ((Node) event.getSource()).getScene().getWindow(), "/View/Reports.fxml", true);
    }

    @FXML
    private void suppliersClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Suppliers.fxml", true);
    }

    @FXML
    private void goodsReceiveClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/OpenReceivedGoods.fxml", true);
    }

    @FXML
    private void orderStock(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/StockOrder.fxml", true);
    }

    @FXML
    private void goodsHistoryClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/GoodsHistory.fxml", true);
    }
}

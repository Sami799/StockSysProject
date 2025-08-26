package Controller;

import Model.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import Utils.SceneUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MinQuantityAlertsController {

    private final String jsonFilePath = "src/MinQuantityAlert.json";

    @FXML
    private TableView<Item> lowStockTable;
    @FXML
    private TableColumn<Item, String> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, String> quantityColumn;
    @FXML
    private TableColumn<Item, String> minQuantityColumn;
    @FXML
    private TableColumn<Item, String> imageColumn;

    @FXML
    public void initialize() {
        imageColumn.setPrefWidth(300);
        idColumn.setPrefWidth(300);
        nameColumn.setPrefWidth(300);
        quantityColumn.setPrefWidth(300);
        minQuantityColumn.setPrefWidth(300);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        minQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));

        imageColumn.setCellFactory(column -> new TableCell<Item, String>() {
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
                    }else {
                        imageView.setImage(new Image("/Images/empty.jpg"));
                    }
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        ObservableList<Item> lowStockItems = checkLowStock();
        lowStockTable.setItems(lowStockItems);
    }

    private ObservableList<Item> checkLowStock() {
        ObservableList<Item> lowStockItems = FXCollections.observableArrayList();
        try {
            String itemsJsonContent = new String(Files.readAllBytes(Paths.get("src/items.json")));
            String minQuantityAlertJsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            JSONObject itemsJson = new JSONObject(itemsJsonContent);
            JSONArray minQuantityAlertArray = new JSONArray(minQuantityAlertJsonContent);

            for (int i = 0; i < minQuantityAlertArray.length(); i++) {
                JSONObject minQuantityAlert = minQuantityAlertArray.getJSONObject(i);
                String minQuantityAlertId = minQuantityAlert.getString("id");
                int minQuantity = minQuantityAlert.getInt("quantity");

                for (String key : itemsJson.keySet()) {
                    JSONArray itemsArray = itemsJson.getJSONArray(key);
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject item = itemsArray.getJSONObject(j);
                        String itemId = item.getString("id");
                        String itemName = item.getString("name");
                        int itemQuantity = item.getInt("quantity");
                        String itemImageUrl = item.getString("imagePath");

                        if (itemId.equals(minQuantityAlertId) && itemQuantity < minQuantity) {
                            lowStockItems.add(new Item(itemId, itemName, String.valueOf(itemQuantity), String.valueOf(minQuantity), itemImageUrl));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lowStockItems;
    }

    @FXML
    private void homeClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }
    public static class Item {
        private final String id;
        private final String quantity;
        private final String minQuantity;
        private final String name;
        private final String imageUrl;

        public Item(String id, String name, String quantity, String minQuantity, String imageUrl) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.minQuantity = minQuantity;
            this.imageUrl = imageUrl;
        }

        public String getId() {
            return id;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getMinQuantity() {
            return minQuantity;
        }

        public String getName() {
            return name;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

}

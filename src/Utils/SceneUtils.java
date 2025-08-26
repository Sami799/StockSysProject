package Utils;

import Controller.AddItemController;
import Controller.CartController;
import Controller.ItemsController;
import Controller.ReceivingGoodsController;
import Model.Item;
import Model.Order;
import Model.Goods;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SceneUtils {

    public static void changeScene(Stage stage, String fxmlPath, boolean keepFullScreen) {
        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(SceneUtils.class.getResource(fxmlPath));
                if (stage.getScene() != null) {
                    stage.getScene().setRoot(root);
                } else {
                    stage.setScene(new Scene(root));
                }
                stage.setFullScreen(keepFullScreen);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error loading scene", "Failed to load the requested scene.", stage, keepFullScreen);
            }});}


    public static void changeSceneWithParams(Stage currentStage, String fxmlPath, List<Item> items, String category, boolean resizable, boolean keepFullScreen) {
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
        try {
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Root is null for fxmlPath: " + fxmlPath);
            }

            // Pass data to the controller of the next scene
            ItemsController controller = loader.getController();
            controller.initData(items, category);
            currentStage.getScene().setRoot(root);
            currentStage.setResizable(resizable);
            currentStage.setFullScreen(keepFullScreen);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML with params: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void changeSceneWithParams(Stage currentStage, String fxmlPath, String category, boolean resizable, boolean keepFullScreen) {
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
        try {
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Root is null for fxmlPath: " + fxmlPath);
            }

            // Pass data to the controller of the next scene
            AddItemController controller = loader.getController();
            controller.setCategory(category);
            currentStage.getScene().setRoot(root);
            currentStage.setResizable(resizable);
            currentStage.setFullScreen(keepFullScreen);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML with params: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void changeSceneWithOrder(Stage currentStage, String fxmlPath, Order order, boolean resizable, boolean keepFullScreen) {
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
        try {
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Root is null for fxmlPath: " + fxmlPath);
            }

            // Pass data to the controller of the next scene
            CartController controller = loader.getController();
            controller.initOrderData(order);
            currentStage.getScene().setRoot(root);
            currentStage.setResizable(resizable);
            currentStage.setFullScreen(keepFullScreen);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML with order: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void changeSceneWithGoods(Stage stage, String fxmlPath, Goods goods , boolean resizable ,  boolean keepFullScreen) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Root is null for fxmlPath: " + fxmlPath);
            }
            ReceivingGoodsController controller = loader.getController();
            controller.initGoodsData(goods);
            stage.getScene().setRoot(root);
            stage.setResizable(resizable);
            stage.setFullScreen(keepFullScreen);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML with goods: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void changeSceneForAddOrder(Stage currentStage, String fxmlPath, boolean resizable, boolean keepFullScreen) {
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
        try {
            Parent root = loader.load();
            if (root == null) {
                System.out.println("Root is null for fxmlPath: " + fxmlPath);
            }

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setResizable(resizable);
            currentStage.setFullScreen(keepFullScreen);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load FXML for add order: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void showAlert(String title, String content, Stage stage, boolean keepFullScreen) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.initOwner(stage);
            alert.showAndWait();

            if (keepFullScreen) {
                stage.setFullScreen(true);
            }
        });
    }
}

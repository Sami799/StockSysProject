package Controller;

import Model.User;
import Utils.SceneUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class WorkersController {
    @FXML
    private Button RemoveWorker;
    @FXML
    private TableView<User> tableView;
    @FXML
    private TableColumn<User, String> userfullnameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> passwordColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private Label clockLabel;
    @FXML
    private TextField searchBar;
    private final String jsonFilePath = "src/users.json";
    private ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        userfullnameColumn.setCellValueFactory(new PropertyValueFactory<>("FullName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableView.setItems(loadUsers());
        initClock();
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());
    }

    private void filterUsers() {
        String searchText = searchBar.getText().toLowerCase();
        if (searchText != null && !searchText.isEmpty()) {
            ObservableList<User> filteredUsers = FXCollections.observableArrayList();
            for (User user : users) {
                if (user.getFullName().toLowerCase().contains(searchText) ||
                        user.getUsername().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText)) {
                    filteredUsers.add(user);
                }
            }
            tableView.setItems(filteredUsers);
        } else {
            tableView.setItems(users);
        }
    }

    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private ObservableList<User> loadUsers() {
        users.clear();
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
                User user = new User(
                        jsonObject.getString("FullName"),
                        jsonObject.getString("username"),
                        jsonObject.getString("password"),
                        jsonObject.optString("email", "")
                );
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Load Error", "Could not load user data.");
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            showAlert("Error", "JSON Error", "Invalid JSON format in user data.");
        }
        return users;
    }

    @FXML
    private void removeWorkerClicked(ActionEvent event) {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "No Worker Selected", "Please select a worker in the table before attempting to delete.");
            return;  // Early return to prevent further execution
        }
        if ("admin".equalsIgnoreCase(selectedUser.getUsername())) {
            showAlert("Invalid Operation", "Cannot Delete Admin", "The admin user cannot be deleted.");
            return;  // Prevent deletion of admin user
        }
        confirmAndDeleteUser(selectedUser.getUsername());
    }

    private void confirmAndDeleteUser(String username) {
        Stage stage = (Stage) RemoveWorker.getScene().getWindow();
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete user " + username + "?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.initOwner(stage);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                deleteUser(username);
            }
        });
    }

    private void deleteUser(String username) {
        try {
            JSONArray jsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get(jsonFilePath))));
            JSONArray updatedArray = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userObj = jsonArray.getJSONObject(i);
                if (!userObj.getString("username").equalsIgnoreCase(username)) {
                    updatedArray.put(userObj);
                }
            }
            Files.write(Paths.get(jsonFilePath), updatedArray.toString(4).getBytes(StandardCharsets.UTF_8));
            tableView.setItems(loadUsers()); // Refresh the table view
            showAlert("Deletion Successful", "User Deleted", "User " + username + " was successfully deleted.");
        } catch (IOException e) {
            showAlert("Error", "Deletion Failed", "Failed to delete user due to: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Stage stage = (Stage) RemoveWorker.getScene().getWindow();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "../View/HomeScreen.fxml", true);
    }

    @FXML
    private void addWorkerClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/SignUp.fxml", true);
    }
}

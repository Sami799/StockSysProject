package Controller;

import Utils.SceneUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button LoginBtn;
    @FXML
    private TextField PlayerName;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private Pane contentPane;
    @FXML
    private TextField visiblePassword;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label clockLabel;

    private JSONArray users;
    private final String CREDENTIALS_FILE = "src/credentials.json";

    public LoginController() {
        // Constructor remains empty
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reloadUsers();
        initClock();
        visiblePassword.setVisible(false);
        visiblePassword.setManaged(false);

        // Add event filter for Enter key on password field
        passwordLogin.setOnAction(event -> LoginClicked(event));
        PlayerName.setOnAction(event -> LoginClicked(event));
        visiblePassword.setOnAction(event -> {
            passwordLogin.setText(visiblePassword.getText());
            LoginClicked(event);
        });

        // Load the logo image
        Image logoImage = new Image(getClass().getResourceAsStream("/Icons/ExtraLineLogo.png"));
        logoImageView.setImage(logoImage);

        // Schedule auto-login to run after the scene has been set
        Platform.runLater(this::autoLogin);
    }

    /*Time Display*/
    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public void reloadUsers() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get("src/users.json")));
            users = new JSONArray(jsonString);  // Directly load into JSONArray
        } catch (Exception e) {
            System.err.println("Failed to load users: " + e.getMessage());
            users = new JSONArray();
        }
    }

    private void loadSavedCredentials() {
        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                String credentialsString = new String(Files.readAllBytes(Paths.get(CREDENTIALS_FILE)));
                JSONObject credentials = new JSONObject(credentialsString);
                PlayerName.setText(credentials.getString("username"));
                passwordLogin.setText(credentials.getString("password"));
                rememberMeCheckbox.setSelected(true);
            }
        } catch (IOException e) {
            System.err.println("Failed to load saved credentials: " + e.getMessage());
        }
    }

    private void autoLogin() {
        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                String credentialsString = new String(Files.readAllBytes(Paths.get(CREDENTIALS_FILE)));
                JSONObject credentials = new JSONObject(credentialsString);
                String username = credentials.getString("username");
                String password = credentials.getString("password");

                if (checkCredentials(username, password)) {
                    UserData.setLoggedInUsername(username);
                    Stage currentStage = (Stage) contentPane.getScene().getWindow();
                    SceneUtils.changeScene(currentStage, "../View/HomeScreen.fxml", true);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to auto-login: " + e.getMessage());
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (passwordLogin.isVisible()) {
            visiblePassword.setText(passwordLogin.getText());
            visiblePassword.setVisible(true);
            visiblePassword.setManaged(true);
            passwordLogin.setVisible(false);
            passwordLogin.setManaged(false);
        } else {
            passwordLogin.setText(visiblePassword.getText());
            passwordLogin.setVisible(true);
            passwordLogin.setManaged(true);
            visiblePassword.setVisible(false);
            visiblePassword.setManaged(false);
        }
    }

    @FXML
    private void LoginClicked(ActionEvent event) {
        String playerName = PlayerName.getText();
        String password = passwordLogin.getText();
        if (playerName.isEmpty() || password.isEmpty()) {
            showAlert("Error", "You have to fill all fields. Please enter your name and password");
            return;
        }

        boolean isAuthenticated = checkCredentials(playerName, password);

        if (isAuthenticated) {
            UserData.setLoggedInUsername(playerName);
            // Save the "Remember Me" option
            if (rememberMeCheckbox.isSelected()) {
                saveCredentials(playerName, password);
            }
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneUtils.changeScene(currentStage, "../View/HomeScreen.fxml", true);
        } else {
            showAlert("Error", "Invalid username or password. Please try again.");
        }
    }

    private boolean checkCredentials(String username, String password) {
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (username.equals(user.getString("username")) && password.equals(user.getString("password"))) {
                return true;
            }
        }
        return false;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage primaryStage = (Stage) contentPane.getScene().getWindow();
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private void saveCredentials(String username, String password) {
        try {
            JSONObject credentials = new JSONObject();
            credentials.put("username", username);
            credentials.put("password", password);
            Files.write(Paths.get(CREDENTIALS_FILE), credentials.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save credentials: " + e.getMessage());
        }
    }

    public void logout() {
        try {
            Files.deleteIfExists(Paths.get(CREDENTIALS_FILE));
        } catch (IOException e) {
            System.err.println("Failed to delete credentials: " + e.getMessage());
        }
    }
}

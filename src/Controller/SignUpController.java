package Controller;

import Utils.SceneUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SignUpController {
    @FXML
    private TextField PlayerFullName;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private TextField PlayerGmail;
    @FXML
    private Button SignUpSubmit;
    @FXML
    private TextField PlayerName;
    @FXML
    private PasswordField passwordLogin;
    @FXML
    private Pane contentPane;
    @FXML
    private Label clockLabel;
    @FXML
    private TextField visiblePassword, visibleConfirmPassword;

    @FXML
    private Label labelMinChars, labelUpper, labelLower, labelDigit, labelSpecialChar;
    private JSONArray users;
    private final String jsonFilePath = "src/users.json";

    public SignUpController() {
        loadUsersFromJson();
    }

    @FXML
    public void initialize() {
        initClock();
        visiblePassword.setVisible(false);
        visiblePassword.setManaged(false);
        visibleConfirmPassword.setVisible(false);
        visibleConfirmPassword.setManaged(false);
        passwordLogin.textProperty().addListener((obs, oldText, newText) -> updatePasswordLabels());
        visiblePassword.textProperty().addListener((obs, oldText, newText) -> updatePasswordLabels());
    }
    private void updatePasswordLabels() {
        String password = passwordLogin.isVisible() ? passwordLogin.getText() : visiblePassword.getText();
        labelMinChars.setStyle("-fx-text-fill: " + (password.length() >= 8 ? "green" : "red") + ";");
        labelUpper.setStyle("-fx-text-fill: " + (password.matches(".*[A-Z].*") ? "green" : "red") + ";");
        labelLower.setStyle("-fx-text-fill: " + (password.matches(".*[a-z].*") ? "green" : "red") + ";");
        labelDigit.setStyle("-fx-text-fill: " + (password.matches(".*\\d.*") ? "green" : "red") + ";");
        labelSpecialChar.setStyle("-fx-text-fill: " + (password.matches(".*[!@#$%^&*()].*") ? "green" : "red") + ";");
    }
    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    private String checkPasswordStrength(String password) {
        StringBuilder missingComponents = new StringBuilder();

        if (!password.matches(".*\\d.*")) {
            missingComponents.append("digit, ");
        }
        if (!password.matches(".*[a-z].*")) {
            missingComponents.append("lowercase letter, ");
        }
        if (!password.matches(".*[A-Z].*")) {
            missingComponents.append("uppercase letter, ");
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            missingComponents.append("special character, ");
        }
        if (password.length() < 8) {
            missingComponents.append("minimum 8 characters, ");
        }

        if (missingComponents.length() > 0) {
            // Remove the last comma and space
            missingComponents.setLength(missingComponents.length() - 2);
            missingComponents.insert(0, "Password missing ");
        }

        return missingComponents.toString();
    }

    @FXML
    private void homePageClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);  // Adjust the path as necessary
    }

    private void loadUsersFromJson() {
        File jsonFile = new File(jsonFilePath);
        if (jsonFile.exists()) {
            try {
                String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                users = new JSONArray(jsonString);
            } catch (IOException e) {
                users = new JSONArray();
                e.printStackTrace();
            }
        } else {
            users = new JSONArray();
            System.err.println("users.json not found in src directory");
        }
    }

    @FXML
    private void SignUpSubmitClicked(ActionEvent event) {
        String playerFullName = PlayerFullName.getText().trim();
        String playerName = PlayerName.getText().trim();
        String password = passwordLogin.isVisible() ? passwordLogin.getText().trim() : visiblePassword.getText().trim();
        String Gmail = PlayerGmail.getText().trim();
        String confirmPasswordText = confirmPassword.isVisible() ? confirmPassword.getText().trim() : visibleConfirmPassword.getText().trim();
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (playerFullName.isEmpty() || playerName.isEmpty() || password.isEmpty() || Gmail.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!Gmail.matches(emailRegex)) {
            showAlert("Error", "Invalid email address.");
            PlayerGmail.clear();
            return;
        }

        if (!password.equals(confirmPasswordText)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        String passwordStrengthMessage = checkPasswordStrength(password);
        if (!passwordStrengthMessage.isEmpty()) {
            showAlert("Error", passwordStrengthMessage);
            return;
        }

        if (userExists(playerName)) {
            showAlert("Error", "Username " + playerName + " already exists. Please choose another one.");
            clearFields();
            return;
        }

        if (emailExists(Gmail)) {
            showAlert("Error", "Email " + Gmail + " is already in use. Please use another email.");
            clearFields();
            return;
        }

        JSONObject newUser = new JSONObject();
        newUser.put("FullName",playerFullName);
        newUser.put("username", playerName);
        newUser.put("password", password);
        newUser.put("email", Gmail);
        users.put(newUser);

        saveUsersToJson();
        sendWelcomeEmail(Gmail);
        showAlert("Success", "User " + playerFullName + " registered successfully.");
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "../View/HomeScreen.fxml", true);
    }

    private void clearFields() {
        PlayerName.clear();
        passwordLogin.clear();
        confirmPassword.clear();
        PlayerGmail.clear();
    }

    private void sendWelcomeEmail(String recipientEmail) {
        String playerFullName = PlayerFullName.getText().trim();
        String playerName = PlayerName.getText().trim();
        String password1 = passwordLogin.isVisible() ? passwordLogin.getText().trim() : visiblePassword.getText().trim();
        final String username = System.getenv("MAILGUN_USERNAME");
        final String password = System.getenv("MAILGUN_API_KEY");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.mailgun.org");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Welcome to ExtraLine!");
            message.setText("Dear " + playerFullName + ",\n\nWelcome to ExtraLine! We are excited to have you on board.\nLogin details:\nUsername: " + playerName + "\nPassword: " + password1);

            Transport.send(message);
            showAlert("Success", "Welcome email sent to " + recipientEmail);

        } catch (SendFailedException e) {
            System.err.println("Failed to send email due to server rejection: " + e.getMessage());
            showAlert("Error", "Failed to send email due to server rejection. Please check the recipient's email address.");
            // Additional handling or user notification logic here
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
            showAlert("Error", "Failed to send email due to an unexpected error. Please try again later.");
            // Additional handling or user notification logic here
        }
    }




    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage primaryStage = (Stage) contentPane.getScene().getWindow();
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private boolean userExists(String username) {
        for (Object userObj : users) {
            JSONObject user = (JSONObject) userObj;
            if (username.equalsIgnoreCase(user.getString("username"))) {
                return true;
            }
        }
        return false;
    }

    private void saveUsersToJson() {
        try (FileWriter file = new FileWriter(jsonFilePath)) {
            file.write(users.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean emailExists(String email) {
        for (Object userObj : users) {
            JSONObject user = (JSONObject) userObj;
            if (email.equalsIgnoreCase(user.getString("email"))) {
                return true;
            }
        }
        return false;
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
    private void toggleConfirmPasswordVisibility(ActionEvent event) {
        if (confirmPassword.isVisible()) {
            visibleConfirmPassword.setText(confirmPassword.getText());
            visibleConfirmPassword.setVisible(true);
            visibleConfirmPassword.setManaged(true);
            confirmPassword.setVisible(false);
            confirmPassword.setManaged(false);
        } else {
            confirmPassword.setText(visibleConfirmPassword.getText());
            confirmPassword.setVisible(true);
            confirmPassword.setManaged(true);
            visibleConfirmPassword.setVisible(false);
            visibleConfirmPassword.setManaged(false);
        }
    }
}

package Controller;

import Model.Goods;
import Model.Item;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.SendFailedException;

public class OpenReceivedGoodsController {

    @FXML
    private Button BackBtn;

    @FXML
    private TableView<Goods> goodsTable;
    @FXML
    private TableColumn<Goods, String> goodsIdColumn;
    @FXML
    private TableColumn<Goods, String> supplierIdColumn;
    @FXML
    private TableColumn<Goods, String> supplierNameColumn;
    @FXML
    private TableColumn<Goods, String> phoneNumberColumn;
    @FXML
    private TableColumn<Goods, String> goodsDateColumn;
    @FXML
    private TableColumn<Goods, Void> itemsColumn;
    @FXML
    private TableColumn<Goods, String> currentQuantityColumn;
    @FXML
    private TableColumn<Goods, String> demandedQuantityColumn;
    @FXML
    private TableColumn<Goods, Void> uploadColumn;
    @FXML
    private TableColumn<Goods, Void> completeColumn;

    private ObservableList<Goods> openGoods;
    private Map<String, String> supplierEmailMap;

    private final String openGoodsFilePath = "src/open_goods.json";
    private final String historyGoodsFilePath = "src/GoodsHistory.json";
    private final String suppliersFilePath = "src/suppliers.json";

    @FXML
    public void initialize() {
        openGoods = FXCollections.observableArrayList();
        goodsTable.setItems(openGoods);
        supplierEmailMap = loadSuppliersData();

        goodsIdColumn.setCellValueFactory(new PropertyValueFactory<>("goodsId"));
        supplierIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supplierNameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        goodsDateColumn.setCellValueFactory(new PropertyValueFactory<>("goodsDate"));
        currentQuantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrentQuantity()));
        demandedQuantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDemandedQuantity()));

        // Add buttons to table columns
        addItemsButtonToTable();
        addUploadButtonToTable();
        addCompleteButtonToTable();

        loadOpenGoods();
        addTableSelectionListener();
    }

    private void loadOpenGoods() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(openGoodsFilePath);
            if (file.exists()) {
                if (file.length() == 0) {
                    System.out.println("Open goods file is empty: " + openGoodsFilePath);
                    openGoods.clear();
                } else {
                    List<Goods> goods = objectMapper.readValue(file, new TypeReference<List<Goods>>() {});
                    openGoods.addAll(goods);
                }
            } else {
                System.out.println("Open goods file does not exist: " + openGoodsFilePath);
                openGoods.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> loadSuppliersData() {
        Map<String, String> supplierEmailMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(suppliersFilePath);
            List<Map<String, String>> suppliers = objectMapper.readValue(file, new TypeReference<List<Map<String, String>>>() {});
            for (Map<String, String> supplier : suppliers) {
                supplierEmailMap.put(supplier.get("ID"), supplier.get("email"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return supplierEmailMap;
    }

    private void addItemsButtonToTable() {
        Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>> cellFactory = new Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>>() {
            @Override
            public TableCell<Goods, Void> call(final TableColumn<Goods, Void> param) {
                final TableCell<Goods, Void> cell = new TableCell<Goods, Void>() {

                    private final Button viewButton = new Button("View Items");

                    {
                        viewButton.setOnAction((ActionEvent event) -> {
                            Goods selectedGoods = getTableView().getItems().get(getIndex());
                            openItemsDialog(selectedGoods);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewButton);
                        }
                    }
                };
                return cell;
            }
        };

        itemsColumn.setCellFactory(cellFactory);
    }

    private void openItemsDialog(Goods selectedGoods) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Items for " + selectedGoods.getGoodsId());
        dialog.setHeaderText("Items for " + selectedGoods.getGoodsId());

        TableView<Item> itemsTable = new TableView<>();
        itemsTable.setPrefWidth(400);
        TableColumn<Item, String> itemNameColumn = new TableColumn<>("Item Name");
        TableColumn<Item, Integer> itemQuantityColumn = new TableColumn<>("Current Quantity");

        itemsTable.getColumns().addAll(itemNameColumn, itemQuantityColumn);

        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        itemQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().currentQuantityProperty().asObject());

        itemsTable.setItems(FXCollections.observableArrayList(selectedGoods.getItems()));

        dialog.getDialogPane().setContent(itemsTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(750);
        itemQuantityColumn.setPrefWidth(250);
        dialog.getDialogPane().getStyleClass().add("custom-dialog-pane");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Design.css").toExternalForm());
        Stage stage = (Stage) goodsTable.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    private void addUploadButtonToTable() {
        Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>> cellFactory = new Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>>() {
            @Override
            public TableCell<Goods, Void> call(final TableColumn<Goods, Void> param) {
                final TableCell<Goods, Void> cell = new TableCell<Goods, Void>() {

                    private final Button uploadButton = new Button("Upload");
                    private final Hyperlink folderLink = new Hyperlink();
                    {
                        uploadButton.setOnAction((ActionEvent event) -> {
                            Goods selectedGoods = getTableView().getItems().get(getIndex());
                            String folderPath = uploadDocuments(selectedGoods);
                            if (!folderPath.isEmpty()) {
                                folderLink.setText(folderPath);
                                updateRow(getIndex());
                            }
                        });
                        folderLink.setOnAction((ActionEvent event) -> {
                            Goods good = getTableView().getItems().get(getIndex());
                            openFolder(good.getDocumentsFolderPath());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Goods good = getTableView().getItems().get(getIndex());
                            String folderPath = good.getDocumentsFolderPath();
                            if (folderPath != null && !folderPath.isEmpty()) {
                                folderLink.setText(folderPath);
                                setGraphic(new VBox(uploadButton, folderLink));
                            } else {
                                setGraphic(uploadButton);
                            }
                        }
                    }
                };
                return cell;
            }
        };

        uploadColumn.setCellFactory(cellFactory);
    }

    private void updateRow(int rowIndex) {
        // Refresh the specific row to reflect changes
        goodsTable.getColumns().get(0).setVisible(false);
        goodsTable.getColumns().get(0).setVisible(true);
    }

    private String uploadDocuments(Goods selectedGoods) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Upload Documents");
        File selectedDirectory = directoryChooser.showDialog(goodsTable.getScene().getWindow());

        if (selectedDirectory != null) {
            String oldFolderPath = selectedGoods.getDocumentsFolderPath();
            String goodsFolderPath = "src/goods/" + selectedGoods.getGoodsId();
            File goodsFolder = new File(goodsFolderPath);
            if (!goodsFolder.exists()) {
                goodsFolder.mkdirs();
            }

            try {
                // Delete old folder
                if (oldFolderPath != null && !oldFolderPath.isEmpty()) {
                    File oldFolder = new File(oldFolderPath);
                    deleteDirectory(oldFolder);
                }

                // Copy new files
                for (File file : selectedDirectory.listFiles()) {
                    String sanitizedFileName = file.getName().replaceAll("[<>:\"/\\|?*]", "");
                    File destinationFile = new File(goodsFolder, sanitizedFileName);
                    destinationFile.getParentFile().mkdirs();
                    Files.copy(file.toPath(), destinationFile.toPath());
                }
                selectedGoods.setDocumentsFolderPath(goodsFolderPath);
                saveOpenGoods();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while uploading documents.");
            }
            return goodsFolderPath;
        } else {
            System.out.println("No Directory Selected");
            return "";
        }
    }
    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    private void openFolder(String folderPath) {
        if (folderPath != null && !folderPath.isEmpty()) {
            try {
                File folder = new File(folderPath);
                if (folder.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(folder);
                } else {
                    showAlert("Folder Not Found", "The specified folder does not exist.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while opening the folder.");
            }
        }
    }

    private void addCompleteButtonToTable() {
        Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>> cellFactory = new Callback<TableColumn<Goods, Void>, TableCell<Goods, Void>>() {
            @Override
            public TableCell<Goods, Void> call(final TableColumn<Goods, Void> param) {
                final TableCell<Goods, Void> cell = new TableCell<Goods, Void>() {

                    private final Button btn = new Button("Complete");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Goods selectedGoods = getTableView().getItems().get(getIndex());
                            completeGoods(selectedGoods);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        completeColumn.setCellFactory(cellFactory);
    }

    private void completeGoods(Goods selectedGoods) {
        if (selectedGoods.getDocumentsFolderPath() == null || selectedGoods.getDocumentsFolderPath().isEmpty()) {
            showAlert("Documents Required", "Please upload documents before completing the goods.");
            return;
        }

        int currentQuantity = Integer.parseInt(selectedGoods.getCurrentQuantity());
        int demandedQuantity = Integer.parseInt(selectedGoods.getDemandedQuantity());

        if (currentQuantity != demandedQuantity) {
            showAlert("Quantity Mismatch", "Current quantity does not match the demanded quantity. Goods cannot be completed.");
            return;
        }

        openGoods.remove(selectedGoods);
        saveOpenGoods();
        saveToHistory(selectedGoods);
    }


    private void saveToHistory(Goods goods) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File(historyGoodsFilePath);
            List<Goods> historyGoods;
            if (file.exists()) {
                if (file.length() == 0) {
                    System.out.println("History goods file is empty: " + historyGoodsFilePath);
                    historyGoods = FXCollections.observableArrayList();
                } else {
                    historyGoods = objectMapper.readValue(file, new TypeReference<List<Goods>>() {});
                }
            } else {
                System.out.println("History goods file does not exist: " + historyGoodsFilePath);
                historyGoods = FXCollections.observableArrayList();
            }
            historyGoods.add(goods);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, historyGoods);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addTableSelectionListener() {
        goodsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                goodsTable.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        handleGoodsSelection(newSelection);
                    }
                });
            }
        });
    }

    private void handleGoodsSelection(Goods selectedGoods) {
            Stage currentStage = (Stage) goodsTable.getScene().getWindow();
        SceneUtils.changeSceneWithGoods(currentStage, "/View/ReceivingGoods.fxml", selectedGoods, true , true);
    }

    @FXML
    private void removeGoodsClicked(ActionEvent event) {
        Goods selectedGoods = goodsTable.getSelectionModel().getSelectedItem();
        if (selectedGoods != null) {
            LocalDate goodsDate = LocalDate.parse(selectedGoods.getGoodsDate());
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(goodsDate, currentDate);

            if (daysBetween > 2) {
                showAlert("Cannot Delete", "You can only delete goods within 2 days of the order date.");
                return;
            }
            if (Integer.parseInt(selectedGoods.currentQuantityProperty().get()) != 0){
                showAlert("Cannot Delete","Cannot delete an order that current quantity of it isn't 0");
                return;
            }

            deleteGoodsDocuments(selectedGoods);
            openGoods.remove(selectedGoods);
            saveOpenGoods();
            sendCancellationEmail(selectedGoods);  // Send email notification
            showAlert("Success", "Goods successfully removed.");
        } else {
            showAlert("No Selection", "Please select a goods entry to remove.");
        }
    }

    private void sendCancellationEmail(Goods selectedGoods) {
       // AFTER — read from environment variables
        final String username = System.getenv("MAILGUN_USERNAME");
        final String password = System.getenv("MAILGUN_API_KEY");
        final String recipientEmail = supplierEmailMap.get(selectedGoods.getSupplierId());  // Lookup supplier email

        if (recipientEmail == null || recipientEmail.isEmpty()) {
            System.err.println("No email address found for supplier ID: " + selectedGoods.getSupplierId());
            showAlert("Error", "No email address found for the supplier. Cancellation email not sent.");
            return;
        }

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
            message.setFrom(new InternetAddress("postmaster@sandboxccb999908b354a93879b5447cdb5522f.mailgun.org"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Order Cancellation Notification");
            message.setText("Dear " + selectedGoods.getSupplierName() + ",\n\nWe regret to inform you that the order with Goods ID: "
                    + selectedGoods.getGoodsId() + " has been canceled.\n\nThank you.");

            Transport.send(message);
            System.out.println("Cancellation email sent successfully.");
            showAlert("Success", "Cancellation email sent successfully to " + recipientEmail);
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


    private void deleteGoodsDocuments(Goods selectedGoods) {
        String folderPath = selectedGoods.getDocumentsFolderPath();
        if (folderPath != null && !folderPath.isEmpty()) {
            File folder = new File(folderPath);
            if (folder.exists()) {
                File[] allContents = folder.listFiles();
                if (allContents != null) {
                    for (File file : allContents) {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
                if (!folder.delete()) {
                    System.err.println("Failed to delete folder: " + folder.getAbsolutePath());
                }
            } else {
                System.out.println("Folder does not exist: " + folderPath);
            }
        } else {
            System.out.println("No folder path provided for goods ID: " + selectedGoods.getGoodsId());
        }
    }

    private void saveOpenGoods() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(openGoodsFilePath).toFile(), openGoods);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            Stage stage = (Stage) goodsTable.getScene().getWindow();
            alert.initOwner(stage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }
}

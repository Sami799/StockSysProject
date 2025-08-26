package Controller;

import Model.Order;
import Model.Item;
import Utils.SceneUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.awt.*;
import java.util.*;
import java.util.Comparator;
import java.util.List;

import static Utils.SceneUtils.showAlert;

public class OpenOrdersController {

    public Button BackBtn;

    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> orderIdColumn;
    @FXML
    private TableColumn<Order, String> CustomerIDColumn;
    @FXML
    private TableColumn<Order, String> customerNameColumn;
    @FXML
    private TableColumn<Order, String> phoneNumberColumn;
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, Void> panelsImagesColumn;
    @FXML
    private TableColumn<Order, Void> completeOrderColumn;
    @FXML
    private CheckBox sortByDateCheckBox;

    @FXML
    private CheckBox sortByPriceCheckBox;
    private ObservableList<Order> openOrders;

    private final String openOrdersFilePath = "src/open_orders.json";
    private final String historyOrdersFilePath = "src/history_orders.json";
    private final String customersFilePath = "src/customers.json";

    @FXML
    public void initialize() {
        openOrders = FXCollections.observableArrayList();
        ordersTable.setItems(openOrders);
        orderIdColumn.setPrefWidth(250);
        CustomerIDColumn.setPrefWidth(200);
        customerNameColumn.setPrefWidth(250);
        phoneNumberColumn.setPrefWidth(200);
        totalPriceColumn.setPrefWidth(200);
        orderDateColumn.setPrefWidth(200);
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        CustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        panelsImagesColumn = new TableColumn<>("Panels Images");
        panelsImagesColumn.setPrefWidth(250);
        addPanelsImagesColumn();
        completeOrderColumn = new TableColumn<>("Complete Order");
        completeOrderColumn.setPrefWidth(250);
        addCompleteOrderButtonToTable();
        loadOpenOrders();
        addTableSelectionListener();
        sortByDateCheckBox.setOnAction(this::handleSortByDateCheckBox);
        sortByPriceCheckBox.setOnAction(this::handleSortByPriceCheckBox);
    }

    private void addPanelsImagesColumn() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<TableColumn<Order, Void>, TableCell<Order, Void>>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<Order, Void>() {
                    private final Button uploadButton = new Button("Upload Folder");

                    private final Hyperlink folderLink = new Hyperlink();

                    {
                        uploadButton.setOnAction((ActionEvent event) -> {
                            Order order = getTableView().getItems().get(getIndex());
                            String folderPath = changePanelsImagesFolder(order);
                            if (!folderPath.isEmpty()) {
                                folderLink.setText(folderPath);
                                updateRow(getIndex());
                            }
                        });

                        folderLink.setOnAction((ActionEvent event) -> {
                            Order order = getTableView().getItems().get(getIndex());
                            openFolder(order.getPanelsImagesFolderPath());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Order order = getTableView().getItems().get(getIndex());
                            String folderPath = order.getPanelsImagesFolderPath();
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

        panelsImagesColumn.setCellFactory(cellFactory);
        ordersTable.getColumns().add(panelsImagesColumn);
    }

    private void updateRow(int rowIndex) {
        // Refresh the specific row to reflect changes
        ordersTable.getColumns().get(0).setVisible(false);
        ordersTable.getColumns().get(0).setVisible(true);
    }

    private String changePanelsImagesFolder(Order order) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Panels Images Folder");
        File selectedDirectory = directoryChooser.showDialog(ordersTable.getScene().getWindow());

        if (selectedDirectory != null) {
            String oldFolderPath = order.getPanelsImagesFolderPath();
            String selectedFolderPath = "src/orders/" + order.getOrderId();
            File ordersFolder = new File(selectedFolderPath);
            if (!ordersFolder.exists()) {
                ordersFolder.mkdirs();
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
                    File destinationFile = new File(ordersFolder, sanitizedFileName);
                    destinationFile.getParentFile().mkdirs();
                    Files.copy(file.toPath(), destinationFile.toPath());
                }
                order.setPanelsImagesFolderPath(selectedFolderPath);
                saveOpenOrders();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while uploading documents.");
            }
            return selectedFolderPath;
        } else {
            System.out.println("No folder selected.");
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

    private void addCompleteOrderButtonToTable() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<TableColumn<Order, Void>, TableCell<Order, Void>>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<Order, Void>() {

                    private final Button btn = new Button("Complete");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Order order = getTableView().getItems().get(getIndex());
                            completeOrder(order);
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

        completeOrderColumn.setCellFactory(cellFactory);
        ordersTable.getColumns().add(completeOrderColumn);
    }

    private void completeOrder(Order order) {
        if (order.getPanelsImagesFolderPath() == null || order.getPanelsImagesFolderPath().isEmpty()) {
            showAlert("Documents Required", "Please upload documents before completing the goods.");
            return;
        }
        sendOrderCompletionEmail(order);
        openOrders.remove(order);
        saveOpenOrders();
        addOrderToHistory(order);
        showAlert("Order Completed", "Order " + order.getOrderId() + " has been completed.");
    }

    private void sendOrderCompletionEmail(Order order) {
        try {
            // Load customers data
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> customers = objectMapper.readValue(Paths.get(customersFilePath).toFile(), new TypeReference<List<Map<String, String>>>() {});

            // Find the customer email
            String customerEmail = null;
            for (Map<String, String> customer : customers) {
                if (customer.get("ID").equals(order.getCustomerID())) {
                    customerEmail = customer.get("email");
                    break;
                }
            }

            if (customerEmail != null) {
                String emailContent = buildEmailContent(order);
                sendEmail(customerEmail, emailContent);
            } else {
                System.out.println("Customer email not found for order: " + order.getOrderId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildEmailContent(Order order) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Thank you for trusting ExtraLine!\n\n")
                .append("We will surely try to give the best of the best.\n\n")
                .append("Order Details:\n")
                .append("Order ID: ").append(order.getOrderId()).append("\n")
                .append("Customer Name: ").append(order.getCustomerName()).append("\n")
                .append("Phone Number: ").append(order.getPhoneNumber()).append("\n")
                .append("Total Price: ").append(order.getTotalPrice()).append("\n")
                .append("Order Date: ").append(order.getOrderDate()).append("\n\n")
                .append("Items:\n");

        for (Item item : order.getItems()) {
            emailContent.append(" - ").append(item.getName())
                    .append(" (Quantity: ").append(item.getQuantity())
                    .append(", Price: ").append(item.getPrice())
                    .append(", Total: ").append(item.getTotal()).append(")\n");
        }

        emailContent.append("\nBest regards,\nExtraLine Team");
        return emailContent.toString();
    }

    private void sendEmail(String recipientEmail, String emailContent) {
        if (!isValidEmail(recipientEmail)) {
            System.err.println("Invalid email address: " + recipientEmail);
            return;
        }

                // Read Mailgun credentials from environment variables instead of hardcoding
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
            message.setFrom(new InternetAddress("postmaster@sandboxccb999908b354a93879b5447cdb5522f.mailgun.org"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Order Completion Notification");
            message.setText(emailContent);

            Transport.send(message);

        }  catch (SendFailedException e) {
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailRegex);
    }

    private void revertItemQuantities(List<Item> items) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = new File("src/items.json");
            Map<String, List<Item>> categories = objectMapper.readValue(file, new TypeReference<Map<String, List<Item>>>() {});
            for (Item orderItem : items) {
                for (List<Item> categoryItems : categories.values()) {
                    for (Item item : categoryItems) {
                        if (item.getID().equals(orderItem.getID())) {
                            item.setQuantity(item.getQuantity() + orderItem.getQuantity());
                            break;
                        }
                    }
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOpenOrders() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = Paths.get(openOrdersFilePath).toFile();
            if (file.exists()) {
                List<Order> orders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
                openOrders.addAll(orders);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addOrderToHistory(Order order) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            File file = Paths.get(historyOrdersFilePath).toFile();
            List<Order> historyOrders;
            if (file.exists()) {
                historyOrders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
            } else {
                historyOrders = FXCollections.observableArrayList();
            }
            historyOrders.add(order);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, historyOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTableSelectionListener() {
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                ordersTable.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        handleOrderSelection(newSelection);
                    }
                });
            }
        });
    }

    private void handleOrderSelection(Order selectedOrder) {
        Stage currentStage = (Stage) ordersTable.getScene().getWindow();
        SceneUtils.changeSceneWithOrder(currentStage, "/View/Cart.fxml", selectedOrder, true, true);
    }

    @FXML
    private void backClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/HomeScreen.fxml", true);
    }

    @FXML
    private void addOrderClicked(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneUtils.changeScene(currentStage, "/View/Cart.fxml", true);
    }

    @FXML
    private void removeOrderClicked(ActionEvent event) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            // Revert item quantities in items.json
            revertItemQuantities(selectedOrder.getItems());

            // Remove the order from open orders
            openOrders.remove(selectedOrder);
            saveOpenOrders();
            showAlert("Order Deleted", "Order " + selectedOrder.getOrderId() + " has been deleted.");
        } else {
            showAlert("No Order Selected", "Please select an order to delete.");
        }
    }

    private void saveOpenOrders() {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(openOrdersFilePath).toFile(), openOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) BackBtn.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    private void handleSortByDateCheckBox(ActionEvent event) {
        if (sortByDateCheckBox.isSelected()) {
            sortByPriceCheckBox.setSelected(false);
        }
        sortOrders();
    }

    private void handleSortByPriceCheckBox(ActionEvent event) {
        if (sortByPriceCheckBox.isSelected()) {
            sortByDateCheckBox.setSelected(false);
        }
        sortOrders();
    }

    @FXML
    private void sortOrders() {
        Comparator<Order> comparator = null;

        if (sortByDateCheckBox.isSelected() && sortByPriceCheckBox.isSelected()) {
            comparator = Comparator.comparing(Order::getOrderDate).thenComparing(Order::getTotalPrice);
        } else if (sortByDateCheckBox.isSelected()) {
            comparator = Comparator.comparing(Order::getOrderDate);
        } else if (sortByPriceCheckBox.isSelected()) {
            comparator = Comparator.comparing(Order::getTotalPrice);
        }

        if (comparator != null) {
            FXCollections.sort(openOrders, comparator);
        }
    }
}

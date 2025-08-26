package Model;

import Model.Item;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ItemListCell extends ListCell<Item> {

    private VBox vbox = new VBox();
    private ImageView imageView = new ImageView();
    private Text idText = new Text();
    private Text nameText = new Text();
    private Text quantityText = new Text();

    public ItemListCell() {
        super();
        vbox.getChildren().addAll(imageView, idText, nameText, quantityText);
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            imageView.setImage(new Image(getClass().getResourceAsStream(item.getImagePath())));
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);

            idText.setText("ID: " + item.getID());
            nameText.setText("Name: " + item.getName());
            quantityText.setText("Quantity: " + item.getQuantity());

            setGraphic(vbox);
        }
    }
}


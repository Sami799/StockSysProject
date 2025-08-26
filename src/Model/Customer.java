package Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.SimpleStringProperty;

public class Customer {
    private final SimpleStringProperty ID;
    private final SimpleStringProperty FullName;
    private final SimpleStringProperty PhoneNumber;
    private final SimpleStringProperty Email;

    @JsonCreator
    public Customer(@JsonProperty("ID") String id,
                @JsonProperty("Name") String name,
                @JsonProperty("Phone number") String phoneNumber,
                @JsonProperty("email") String email) {
        this.ID = new SimpleStringProperty(id);
        this.FullName = new SimpleStringProperty(name);
        this.PhoneNumber = new SimpleStringProperty(phoneNumber);
        this.Email = new SimpleStringProperty(email);
    }


    @JsonProperty("ID")
    public String getID() {
        return ID.get();
    }
    public void setID(String id) {
        this.ID.set(id);
    }
    public SimpleStringProperty IDProperty() {
        return ID;
    }

    @JsonProperty("Name")
    public String getName() {
        return FullName.get();
    }
    public void setName(String name) {
        this.FullName.set(name);
    }
    public SimpleStringProperty nameProperty() {
        return FullName;
    }

    @JsonProperty("Phone number")
    public String getPhoneNumber() {
        return PhoneNumber.get();
    }
    public void setPhoneNumber(String PhoneNumber) {
        this.PhoneNumber.set(PhoneNumber);
    }
    public SimpleStringProperty PhoneNumberProperty() {
        return PhoneNumber;
    }

    @JsonProperty("email")
    public String getEmail() {
        return Email.get();
    }
    public void setEmail(String Email) {
        this.Email.set(Email);
    }
    public SimpleStringProperty EmailProperty() {
        return Email;
    }
}

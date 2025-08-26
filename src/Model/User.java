package Model;

public class User {
    private String FullName;
    private String username;
    private String password;
    private String email;  // New field for the email address

    // Updated constructor to include the email parameter
    public User(String FullName ,String username, String password, String email) {
        this.FullName=FullName;
        this.username = username;
        this.password = password;
        this.email = email;
    }
    public String getFullName() {
        return FullName;
    }

    public void setFullName(String FullName) {
        this.FullName = FullName;
    }
    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }
}

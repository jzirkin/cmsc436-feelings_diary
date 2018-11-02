package cmsc436.feelingsdiary;

class User {
    private String username;
    private String password;
    private String email;
    private String key;

    // Default constructor needed for Firebase
    public User() {}

    public User(String username, String password, String email, String key) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.key = key;
    }

    // Public constructors needed for Firebase
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getKey() {
        return key;
    }
}

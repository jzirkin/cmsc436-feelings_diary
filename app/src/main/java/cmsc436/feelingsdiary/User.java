package cmsc436.feelingsdiary;

class User {
    private String username;
    private String password;
    private String key;

    public User() {}

    public User(String username, String password, String key) {
        this.username = username;
        this.password = password;
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getKey() {
        return key;
    }
}

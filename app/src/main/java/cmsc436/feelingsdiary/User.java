package cmsc436.feelingsdiary;

class User {
    private String username;
    private String password;

    public User() {}

    public User(String user, String pass) {
        username = user;
        password = pass;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

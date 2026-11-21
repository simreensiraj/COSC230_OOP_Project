package model;

// anyone who can log in. Student and Administrator extend this
public abstract class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    // password stays private, other classes can only check a guess
    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    // only subclasses need it (Student saves it to the file)
    protected String getPassword() {
        return password;
    }

    // LoginFrame calls this to open the admin or student window
    public abstract boolean isAdmin();
}

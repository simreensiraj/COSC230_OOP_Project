package logic;

// login failed, the message goes on the login screen
public class LoginException extends Exception {
    public LoginException(String message) {
        super(message);
    }
}

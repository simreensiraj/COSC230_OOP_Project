package logic;

// one exception for problems the user should see: bad form input, failed login, broken data file
// getMessage() goes on the screen. field = which form field is wrong (null if it's not about a field)
public class ValidationException extends Exception {
    private final String field;

    public ValidationException(String message) {
        this(null, message);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

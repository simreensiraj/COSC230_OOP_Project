package logic;

// a form field is wrong. getField() tells the screen which red label to show
public class ValidationException extends Exception {
    private String field;
    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }
    public String getField() {
        return field;
    }
}

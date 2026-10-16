/** Unchecked: programming error or illegal object state (e.g. negative overtime hours). */
public class InvalidEmployeeStateException extends RuntimeException {
    public InvalidEmployeeStateException(String message) {
        super(message);
    }
}

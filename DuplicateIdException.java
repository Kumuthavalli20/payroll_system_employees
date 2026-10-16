/** Checked: thrown when a record with the same key already exists. */
public class DuplicateIdException extends Exception {
    public DuplicateIdException(Object id) {
        super("Duplicate id detected: " + id);
    }
}

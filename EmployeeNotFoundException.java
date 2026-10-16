/** Checked: lookup by id failed. */
public class EmployeeNotFoundException extends Exception {
    public EmployeeNotFoundException(String id) {
        super("No employee found with id " + id);
    }
}

/** Checked: callers must decide what to do with a bad salary (reject, correct, log). */
public class InvalidSalaryException extends Exception {
    private final double salary;

    public InvalidSalaryException(String employeeId, double salary) {
        super("Invalid salary " + salary + " for employee " + employeeId);
        this.salary = salary;
    }

    public double getSalary() {
        return salary;
    }
}

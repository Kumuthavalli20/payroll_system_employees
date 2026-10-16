import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all employee types.
 * Fields are private (encapsulation); baseSalary is protected so subclasses
 * can use it in their salary formulas. Constructors are protected because the
 * class is abstract and only subclasses should chain into them.
 */
public abstract class Employee implements Payable {

    private final String id;
    private String name;
    private String department;
    private final LocalDate joiningDate;
    protected double baseSalary;

    /** Full constructor. */
    protected Employee(String id, String name, String department,
                       LocalDate joiningDate, double baseSalary) throws InvalidSalaryException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Employee id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        if (joiningDate == null) {
            throw new IllegalArgumentException("Joining date is required");
        }
        if (Double.isNaN(baseSalary) || Double.isInfinite(baseSalary) || baseSalary <= 0) {
            throw new InvalidSalaryException(id, baseSalary);
        }
        this.id = id.trim();
        this.name = name.trim();
        this.department = (department == null || department.isBlank()) ? "Unassigned" : department.trim();
        this.joiningDate = joiningDate;
        this.baseSalary = baseSalary;
    }

    /** Convenience constructor: joins today. Chains to the full constructor. */
    protected Employee(String id, String name, String department, double baseSalary)
            throws InvalidSalaryException {
        this(id, name, department, LocalDate.now(), baseSalary);
    }

    // ---------- accessors ----------
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public double getBaseSalary() { return baseSalary; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidEmployeeStateException("Name cannot be blank");
        }
        this.name = name.trim();
    }

    public void setBaseSalary(double baseSalary) throws InvalidSalaryException {
        if (Double.isNaN(baseSalary) || baseSalary <= 0) {
            throw new InvalidSalaryException(id, baseSalary);
        }
        this.baseSalary = baseSalary;
    }

    // ---------- type-specific hooks ----------
    public abstract String getType();

    /** Default bonus percentage for this employee type (overridden per subclass). */
    protected abstract double defaultBonusPercent();

    // ---------- overloaded bonus calculation ----------
    /** Bonus using the type's default percentage. */
    public double calculateBonus() {
        return calculateBonus(defaultBonusPercent());
    }

    /** Bonus using an explicit percentage of base salary. */
    public double calculateBonus(double percent) {
        if (percent < 0) {
            throw new InvalidEmployeeStateException("Bonus percent cannot be negative: " + percent);
        }
        return baseSalary * percent / 100.0;
    }

    /** Bonus scaled by a performance rating (1..5; 3 = neutral). */
    public double calculateBonus(double percent, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5: " + rating);
        }
        return calculateBonus(percent) * (rating / 3.0);
    }

    // ---------- validation ----------
    /** Adds every problem found to the sink. Reusable sink avoids allocating per call. */
    public void validate(List<String> sink) {
        if (joiningDate.isAfter(LocalDate.now())) {
            sink.add("Joining date " + joiningDate + " is in the future");
        }
        addTypeProblems(sink);
    }

    public List<String> validate() {
        List<String> problems = new ArrayList<>();
        validate(problems);
        return problems;
    }

    /** Subclasses add their own rules here. */
    protected void addTypeProblems(List<String> sink) { }

    @Override
    public String toString() {
        return String.format("%-6s %-10s %-18s %-12s joined %s base %,.2f",
                id, getType(), name, department, joiningDate, baseSalary);
    }
}

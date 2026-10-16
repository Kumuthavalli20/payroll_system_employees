import java.time.LocalDate;
import java.util.List;

/** Interns: fixed stipend (base), no bonus, no statutory deductions. Stipend is capped. */
public class Intern extends Employee {

    private static final double MAX_STIPEND = 30_000.0;

    public Intern(String id, String name, String department,
                  LocalDate joiningDate, double stipend) throws InvalidSalaryException {
        super(id, name, department, joiningDate, stipend);
    }

    public Intern(String id, String name, String department, double stipend)
            throws InvalidSalaryException {
        super(id, name, department, stipend);
    }

    @Override public String getType() { return "INTERN"; }

    @Override protected double defaultBonusPercent() { return 0.0; }

    /** Overrides the 2-arg bonus: interns never receive a performance bonus. */
    @Override
    public double calculateBonus(double percent, int rating) {
        return 0.0;
    }

    @Override public double calculateGrossSalary() { return baseSalary; }

    @Override public double calculateDeductions() { return 0.0; }

    @Override
    protected void addTypeProblems(List<String> sink) {
        if (baseSalary > MAX_STIPEND) {
            sink.add("Stipend " + baseSalary + " exceeds intern cap " + MAX_STIPEND);
        }
    }
}

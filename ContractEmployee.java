import java.time.LocalDate;
import java.util.List;

/**
 * Contract staff: monthly retainer (base) + overtime at 1.5x the derived hourly rate.
 * No PF and no bonus; a flat 10% TDS is withheld from gross.
 */
public class ContractEmployee extends Employee {

    private static final double STANDARD_HOURS_PER_MONTH = 160.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;
    private static final double TDS_RATE = 0.10;

    private final LocalDate contractEnd;
    private double overtimeHours;

    public ContractEmployee(String id, String name, String department, LocalDate joiningDate,
                            double baseSalary, LocalDate contractEnd) throws InvalidSalaryException {
        super(id, name, department, joiningDate, baseSalary);
        if (contractEnd == null) {
            throw new IllegalArgumentException("Contract end date is required");
        }
        this.contractEnd = contractEnd;
    }

    /** Convenience: starts today, runs one year. */
    public ContractEmployee(String id, String name, String department, double baseSalary)
            throws InvalidSalaryException {
        this(id, name, department, LocalDate.now(), baseSalary, LocalDate.now().plusYears(1));
    }

    public LocalDate getContractEnd() { return contractEnd; }
    public double getOvertimeHours() { return overtimeHours; }

    public void setOvertimeHours(double hours) {
        if (hours < 0) {
            throw new InvalidEmployeeStateException("Overtime hours cannot be negative: " + hours);
        }
        this.overtimeHours = hours;
    }

    private double hourlyRate() { return baseSalary / STANDARD_HOURS_PER_MONTH; }

    @Override public String getType() { return "CONTRACT"; }

    @Override protected double defaultBonusPercent() { return 0.0; }

    @Override
    public double calculateGrossSalary() {
        return baseSalary + overtimeHours * hourlyRate() * OVERTIME_MULTIPLIER;
    }

    @Override
    public double calculateDeductions() {
        return calculateGrossSalary() * TDS_RATE;
    }

    @Override
    protected void addTypeProblems(List<String> sink) {
        if (contractEnd.isBefore(LocalDate.now())) {
            sink.add("Contract ended on " + contractEnd + " but record is still active");
        }
        if (contractEnd.isBefore(getJoiningDate())) {
            sink.add("Contract end " + contractEnd + " is before joining date");
        }
    }
}

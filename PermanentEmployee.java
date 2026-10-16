import java.time.LocalDate;
import java.util.List;

/**
 * Permanent staff: base + HRA (20%) + special allowance (10%) + rating-scaled bonus.
 * Deductions: provident fund (12% of base), professional tax, progressive income tax.
 */
public class PermanentEmployee extends Employee {

    private static final double HRA_RATE = 0.20;
    private static final double ALLOWANCE_RATE = 0.10;
    private static final double PF_RATE = 0.12;
    private static final double PROFESSIONAL_TAX = 200.0;
    private static final double MIN_PERMANENT_SALARY = 15_000.0;

    private int rating = 3;

    public PermanentEmployee(String id, String name, String department,
                             LocalDate joiningDate, double baseSalary) throws InvalidSalaryException {
        super(id, name, department, joiningDate, baseSalary);
    }

    public PermanentEmployee(String id, String name, String department, double baseSalary)
            throws InvalidSalaryException {
        super(id, name, department, baseSalary);
    }

    public int getRating() { return rating; }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5: " + rating);
        }
        this.rating = rating;
    }

    @Override public String getType() { return "PERMANENT"; }

    @Override protected double defaultBonusPercent() { return 8.0; }

    @Override
    public double calculateGrossSalary() {
        return baseSalary
                + baseSalary * HRA_RATE
                + baseSalary * ALLOWANCE_RATE
                + calculateBonus(defaultBonusPercent(), rating);
    }

    @Override
    public double calculateDeductions() {
        double gross = calculateGrossSalary();
        return baseSalary * PF_RATE + PROFESSIONAL_TAX + incomeTax(gross);
    }

    /** Progressive slabs: 0% to 25k, 5% to 50k, 10% to 100k, 20% above. */
    private static double incomeTax(double gross) {
        double tax = 0;
        double remaining = gross;
        if (remaining > 100_000) { tax += (remaining - 100_000) * 0.20; remaining = 100_000; }
        if (remaining > 50_000)  { tax += (remaining - 50_000) * 0.10;  remaining = 50_000; }
        if (remaining > 25_000)  { tax += (remaining - 25_000) * 0.05; }
        return tax;
    }

    @Override
    protected void addTypeProblems(List<String> sink) {
        if (baseSalary < MIN_PERMANENT_SALARY) {
            sink.add("Base salary " + baseSalary + " is below permanent minimum " + MIN_PERMANENT_SALARY);
        }
    }
}

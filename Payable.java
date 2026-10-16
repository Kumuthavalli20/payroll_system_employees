/** Contract for anything that can be paid. */
public interface Payable {

    double calculateGrossSalary();

    double calculateDeductions();

    /** Default: gross minus deductions. Subclasses may override for special rules. */
    default double calculateNetSalary() {
        return calculateGrossSalary() - calculateDeductions();
    }
}

import java.util.ArrayList;
import java.util.List;

/**
 * Processes payroll in fixed-size batches.
 *
 * Allocation discipline (see README for the memory discussion):
 *  - subList() gives a view of each batch, not a copy
 *  - totals live in primitive locals (stack), not in per-employee result objects
 *  - one reusable problems list and one reusable StringBuilder for the whole run
 *  - gross and deductions are computed once per employee; net = gross - deductions
 */
public class PayrollProcessor {

    /** Single small object returned at the end of the run. */
    public static final class Summary {
        public int batches;
        public int processed;
        public int skipped;
        public double totalGross;
        public double totalDeductions;
        public double totalNet;
    }

    public Summary process(List<Employee> employees, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive: " + batchSize);
        }
        Summary summary = new Summary();
        List<String> problems = new ArrayList<>(4);
        StringBuilder line = new StringBuilder(96);
        int total = employees.size();

        for (int start = 0; start < total; start += batchSize) {
            int end = Math.min(start + batchSize, total);
            List<Employee> batch = employees.subList(start, end);   // view, no copy

            double batchGross = 0;
            double batchDeductions = 0;
            int batchOk = 0;
            int batchSkipped = 0;

            for (int i = 0; i < batch.size(); i++) {                // indexed loop: no Iterator object
                Employee e = batch.get(i);
                problems.clear();
                e.validate(problems);
                if (!problems.isEmpty()) {
                    batchSkipped++;
                    continue;                                       // invalid records are not paid
                }
                double gross = e.calculateGrossSalary();            // runtime polymorphism
                double deductions = e.calculateDeductions();
                batchGross += gross;
                batchDeductions += deductions;
                batchOk++;
            }

            summary.batches++;
            summary.processed += batchOk;
            summary.skipped += batchSkipped;
            summary.totalGross += batchGross;
            summary.totalDeductions += batchDeductions;
            summary.totalNet += batchGross - batchDeductions;

            line.setLength(0);
            line.append("Batch ").append(summary.batches)
                .append(" [").append(start + 1).append('-').append(end).append("] paid=")
                .append(batchOk).append(" skipped=").append(batchSkipped)
                .append(String.format(" net=%,.2f", batchGross - batchDeductions));
            System.out.println(line);
        }
        return summary;
    }
}

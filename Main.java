import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Menu-driven console front end. */
public class Main {

    private static final java.util.Scanner in = new java.util.Scanner(System.in);
    private static final EmployeeService service = new EmployeeService();
    private static final PayrollProcessor processor = new PayrollProcessor();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1: addEmployee(); break;
                case 2: searchById(); break;
                case 3: searchByName(); break;
                case 4: filterByDepartment(); break;
                case 5: sortBySalary(); break;
                case 6: departmentReport(); break;
                case 7: ranking(); break;
                case 8: runPayroll(); break;
                case 9: showInvalidRecords(); break;
                case 10: loadSampleData(); break;
                case 11: showAll(); break;
                case 12: showBonusDemo(); break;
                case 0:
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Unknown option. Try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Payroll Management =====");
        System.out.println(" 1. Add employee");
        System.out.println(" 2. Find employee by id");
        System.out.println(" 3. Search by name");
        System.out.println(" 4. Filter by department");
        System.out.println(" 5. Sort by net salary");
        System.out.println(" 6. Department-wise report");
        System.out.println(" 7. Rank employees by net salary");
        System.out.println(" 8. Process payroll (batches)");
        System.out.println(" 9. Show invalid records");
        System.out.println("10. Load sample data");
        System.out.println("11. List all employees");
        System.out.println("12. Bonus overload demo");
        System.out.println(" 0. Exit");
    }

    // ---------- actions ----------
    private static void addEmployee() {
        System.out.println("Type: 1) Permanent  2) Contract  3) Intern");
        int type = readInt("Type: ");
        if (type < 1 || type > 3) {
            System.out.println("Invalid type.");
            return;
        }
        String id = readLine("Employee id: ");
        String name = readLine("Name: ");
        String dept = readLine("Department: ");
        LocalDate joining = readDate("Joining date (yyyy-MM-dd, blank = today): ", LocalDate.now());
        double base = readDouble(type == 3 ? "Monthly stipend: " : "Monthly base salary: ");

        try {
            Employee e;
            switch (type) {
                case 1: {
                    PermanentEmployee p = new PermanentEmployee(id, name, dept, joining, base);
                    p.setRating(readInt("Performance rating (1-5): "));
                    e = p;
                    break;
                }
                case 2: {
                    LocalDate end = readDate("Contract end (yyyy-MM-dd, blank = +1 year): ",
                            LocalDate.now().plusYears(1));
                    ContractEmployee c = new ContractEmployee(id, name, dept, joining, base, end);
                    c.setOvertimeHours(readDouble("Overtime hours this month: "));
                    e = c;
                    break;
                }
                default:
                    e = new Intern(id, name, dept, joining, base);
                    break;
            }
            service.add(e);
            System.out.println("Added: " + e);
        } catch (InvalidSalaryException | DuplicateIdException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        } catch (IllegalArgumentException | InvalidEmployeeStateException ex) {
            System.out.println("Rejected (bad input): " + ex.getMessage());
        }
    }

    private static void searchById() {
        try {
            Employee e = service.findById(readLine("Employee id: ").trim());
            printWithPay(e);
        } catch (EmployeeNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void searchByName() {
        printList(service.searchByName(readLine("Name contains: ")));
    }

    private static void filterByDepartment() {
        printList(service.byDepartment(readLine("Department: ")));
    }

    private static void sortBySalary() {
        int dir = readInt("1) Ascending  2) Descending: ");
        List<Employee> sorted = service.sortedByNetSalary(dir == 1);
        for (Employee e : sorted) {
            System.out.printf("%-6s %-18s %-12s net %,12.2f%n",
                    e.getId(), e.getName(), e.getDepartment(), e.calculateNetSalary());
        }
        if (sorted.isEmpty()) {
            System.out.println("No employees.");
        }
    }

    private static void departmentReport() {
        TreeMap<String, List<Employee>> report = service.departmentReport();
        if (report.isEmpty()) {
            System.out.println("No employees.");
            return;
        }
        for (Map.Entry<String, List<Employee>> entry : report.entrySet()) {
            double gross = 0, net = 0;
            System.out.println("--- " + entry.getKey() + " (" + entry.getValue().size() + ") ---");
            for (Employee e : entry.getValue()) {
                gross += e.calculateGrossSalary();
                net += e.calculateNetSalary();
                System.out.printf("  %-6s %-10s %-18s net %,12.2f%n",
                        e.getId(), e.getType(), e.getName(), e.calculateNetSalary());
            }
            System.out.printf("  Totals: gross %,.2f | net %,.2f%n", gross, net);
        }
    }

    private static void ranking() {
        int limit = readInt("Show top N (0 = all): ");
        TreeSet<Employee> ranked = service.rankByNetSalary();
        int rank = 0;
        for (Employee e : ranked) {
            if (limit > 0 && rank >= limit) {
                break;
            }
            rank++;
            System.out.printf("#%-3d %-6s %-18s %-10s net %,12.2f%n",
                    rank, e.getId(), e.getName(), e.getType(), e.calculateNetSalary());
        }
        if (rank == 0) {
            System.out.println("No employees.");
        }
    }

    private static void runPayroll() {
        if (service.count() == 0) {
            System.out.println("No employees to process.");
            return;
        }
        int batchSize = readInt("Batch size: ");
        try {
            PayrollProcessor.Summary s = processor.process(service.all(), batchSize);
            System.out.printf("Done: %d batches, %d paid, %d skipped (invalid)%n",
                    s.batches, s.processed, s.skipped);
            System.out.printf("Gross %,.2f | Deductions %,.2f | Net %,.2f%n",
                    s.totalGross, s.totalDeductions, s.totalNet);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void showInvalidRecords() {
        Map<String, List<String>> invalid = service.findInvalidRecords();
        if (invalid.isEmpty()) {
            System.out.println("All records are valid.");
            return;
        }
        for (Map.Entry<String, List<String>> entry : invalid.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (String problem : entry.getValue()) {
                System.out.println("   - " + problem);
            }
        }
    }

    private static void showAll() {
        printList(service.all());
        System.out.println("Counts by type: " + service.countByType());
    }

    private static void showBonusDemo() {
        List<Employee> list = service.all();
        if (list.isEmpty()) {
            System.out.println("Add or load employees first.");
            return;
        }
        Employee e = list.get(0);
        System.out.println("Employee: " + e.getId() + " " + e.getName() + " (" + e.getType() + ")");
        System.out.printf("calculateBonus()            = %,.2f%n", e.calculateBonus());
        System.out.printf("calculateBonus(10)          = %,.2f%n", e.calculateBonus(10));
        System.out.printf("calculateBonus(10, rating 5)= %,.2f%n", e.calculateBonus(10, 5));
    }

    private static void loadSampleData() {
        List<Employee> raw = new ArrayList<>();
        int rejected = 0;
        try {
            PermanentEmployee a = new PermanentEmployee("E001", "Asha Rao", "Engineering",
                    LocalDate.of(2019, 4, 1), 120_000);
            a.setRating(5);
            PermanentEmployee b = new PermanentEmployee("E002", "Vikram Singh", "sales",
                    LocalDate.of(2021, 8, 15), 60_000);
            b.setRating(3);
            PermanentEmployee c = new PermanentEmployee("E003", "Meera Nair", "Finance",
                    LocalDate.of(2020, 1, 10), 85_000);
            c.setRating(4);
            ContractEmployee d = new ContractEmployee("C101", "Rahul Verma", "Engineering",
                    LocalDate.of(2026, 1, 1), 70_000, LocalDate.of(2027, 1, 1));
            d.setOvertimeHours(12);
            Intern f = new Intern("I201", "Divya Iyer", "Operations", LocalDate.of(2026, 6, 1), 20_000);
            Intern g = new Intern("I202", "Karan Shah", "Sales", LocalDate.of(2026, 7, 1), 18_000);

            // deliberately bad records
            PermanentEmployee lowPay = new PermanentEmployee("E900", "Low Pay", "Operations",
                    LocalDate.of(2022, 2, 2), 9_000);
            PermanentEmployee future = new PermanentEmployee("E901", "Future Joiner", "Finance",
                    LocalDate.of(2027, 3, 1), 50_000);
            ContractEmployee expired = new ContractEmployee("C900", "Expired Contract", "Operations",
                    LocalDate.of(2024, 1, 1), 40_000, LocalDate.of(2025, 12, 31));
            Intern rich = new Intern("I900", "Overpaid Intern", "Engineering",
                    LocalDate.of(2026, 5, 1), 45_000);
            PermanentEmployee dup = new PermanentEmployee("E002", "Duplicate Id", "Sales", 55_000);

            raw.add(a); raw.add(b); raw.add(c); raw.add(d); raw.add(f); raw.add(g);
            raw.add(lowPay); raw.add(future); raw.add(expired); raw.add(rich); raw.add(dup);
        } catch (InvalidSalaryException ex) {
            System.out.println("Sample construction failed: " + ex.getMessage());
            return;
        }

        try {
            new PermanentEmployee("E999", "Negative Pay", "Finance", -500);
        } catch (InvalidSalaryException ex) {
            rejected++;
            System.out.println("Rejected at construction: " + ex.getMessage());
        }

        Set<String> duplicates = service.findDuplicateIds(raw);
        if (!duplicates.isEmpty()) {
            System.out.println("Duplicate ids in incoming batch: " + duplicates);
        }

        int added = 0;
        for (Employee e : raw) {
            try {
                service.add(e);
                added++;
            } catch (DuplicateIdException ex) {
                rejected++;
                System.out.println("Skipped: " + ex.getMessage());
                continue;
            }
        }
        System.out.println("Loaded " + added + " employees, " + rejected + " rejected.");
    }

    // ---------- helpers ----------
    private static void printList(List<Employee> list) {
        if (list.isEmpty()) {
            System.out.println("No matching employees.");
            return;
        }
        for (Employee e : list) {
            System.out.println(e);
        }
    }

    private static void printWithPay(Employee e) {
        System.out.println(e);
        System.out.printf("   gross %,.2f | deductions %,.2f | net %,.2f%n",
                e.calculateGrossSalary(), e.calculateDeductions(), e.calculateNetSalary());
        List<String> problems = e.validate();
        for (String p : problems) {
            System.out.println("   ! " + p);
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return in.nextLine();
    }

    private static int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
                continue;
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a number.");
                continue;
            }
        }
    }

    private static LocalDate readDate(String prompt, LocalDate defaultValue) {
        while (true) {
            String s = readLine(prompt).trim();
            if (s.isEmpty()) {
                return defaultValue;
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException ex) {
                System.out.println("Use the format yyyy-MM-dd.");
                continue;
            }
        }
    }
}

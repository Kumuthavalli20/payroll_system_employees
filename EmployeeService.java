import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Business operations over the employee repository. */
public class EmployeeService {

    private final Repository<String, Employee> repository = new Repository<>(Employee::getId);

    public void add(Employee employee) throws DuplicateIdException {
        repository.save(employee);
    }

    public Employee findById(String id) throws EmployeeNotFoundException {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public List<Employee> all() {
        return repository.findAll();
    }

    public int count() {
        return repository.size();
    }

    // ---------- search & filter ----------
    public List<Employee> searchByName(String fragment) {
        String needle = fragment.toLowerCase();
        return repository.findWhere(e -> e.getName().toLowerCase().contains(needle));
    }

    public List<Employee> byDepartment(String department) {
        return repository.findWhere(e -> e.getDepartment().equalsIgnoreCase(department.trim()));
    }

    // ---------- sorting ----------
    public List<Employee> sortedByNetSalary(boolean ascending) {
        List<Employee> copy = new ArrayList<>(repository.findAll());
        Comparator<Employee> cmp = Comparator.comparingDouble(Employee::calculateNetSalary);
        copy.sort(ascending ? cmp : cmp.reversed());
        return copy;
    }

    // ---------- reports ----------
    /** Department name -> employees, departments sorted alphabetically (case-insensitive). */
    public TreeMap<String, List<Employee>> departmentReport() {
        TreeMap<String, List<Employee>> report = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        List<Employee> list = repository.findAll();
        for (int i = 0; i < list.size(); i++) {
            Employee e = list.get(i);
            report.computeIfAbsent(e.getDepartment(), d -> new ArrayList<>()).add(e);
        }
        return report;
    }

    /** Highest net salary first; ties broken by id so no employee is dropped by the set. */
    public TreeSet<Employee> rankByNetSalary() {
        TreeSet<Employee> ranked = new TreeSet<>(
                Comparator.comparingDouble(Employee::calculateNetSalary).reversed()
                        .thenComparing(Employee::getId));
        ranked.addAll(repository.findAll());
        return ranked;
    }

    // ---------- data quality ----------
    /** Ids that appear more than once in an incoming batch (before it hits the repository). */
    public Set<String> findDuplicateIds(Collection<Employee> incoming) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (Employee e : incoming) {
            if (!seen.add(e.getId())) {
                duplicates.add(e.getId());
            }
        }
        return duplicates;
    }

    /** Employee id -> list of problems, for every stored record that fails validation. */
    public Map<String, List<String>> findInvalidRecords() {
        Map<String, List<String>> invalid = new LinkedHashMap<>();
        for (Employee e : repository.findAll()) {
            List<String> problems = e.validate();
            if (!problems.isEmpty()) {
                invalid.put(e.getId(), problems);
            }
        }
        return invalid;
    }

    public Map<String, Integer> countByType() {
        Map<String, Integer> counts = new HashMap<>();
        for (Employee e : repository.findAll()) {
            counts.merge(e.getType(), 1, Integer::sum);
        }
        return counts;
    }
}

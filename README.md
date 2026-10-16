# Enterprise Employee & Payroll Management (Core Java)

## Build and run

    javac -d out *.java
    java -cp out Main

Start with menu option 10 (load sample data), then try 9 (invalid records), 6 (department report), 7 (ranking) and 8 (batch payroll).

## Requirement map

| # | Requirement | Where |
|---|-------------|-------|
| 1 | Abstract `Employee`, private fields, protected `baseSalary`, constructor chaining | `Employee` (4-arg constructor calls the 5-arg one via `this(...)`; subclasses call `super(...)`) |
| 2 | Permanent / Contract / Intern rules | `PermanentEmployee`, `ContractEmployee`, `Intern` |
| 3 | `Payable` interface | `Payable` (gross, deductions, default net) |
| 4 | Overriding, runtime polymorphism, overloaded bonus | `calculateGrossSalary/Deductions` overridden per type; `Employee.calculateBonus()`, `(double)`, `(double,int)`; `Intern` overrides the 2-arg version |
| 5 | HashMap, ArrayList, TreeMap, TreeSet | `Repository` (HashMap + ArrayList), `EmployeeService.departmentReport()` (TreeMap), `rankByNetSalary()` (TreeSet + comparator) |
| 6 | Search, filter, sort, duplicates, exceptions | `EmployeeService`; checked: `InvalidSalaryException`, `DuplicateIdException`, `EmployeeNotFoundException`; unchecked: `InvalidEmployeeStateException`, `IllegalArgumentException` |
| 7 | Generic repository | `Repository<K, V>` |
| 8 | Batch payroll | `PayrollProcessor` |
| 9 | Menu UI | `Main` (Scanner, while loop, switch/break, continue) |

## Salary rules

- **Permanent**: gross = base + 20% HRA + 10% allowance + bonus (8% of base, scaled by rating/3). Deductions = 12% PF + 200 professional tax + progressive tax (0% to 25k, 5% to 50k, 10% to 100k, 20% above).
- **Contract**: gross = base + overtime hours x (base/160) x 1.5. Deductions = flat 10% TDS. No bonus, no PF.
- **Intern**: gross = stipend. No deductions, no bonus.

## Checked vs unchecked exceptions

- Checked (caller must handle a business outcome): bad salary, duplicate id, employee not found.
- Unchecked (bug or illegal state): negative overtime, blank name, rating outside 1..5, negative bonus percent, non-positive batch size.

## Invalid records

Some records are legal to construct but unfit to pay. `validate()` flags: joining date in the future, permanent salary below 15,000, expired contract still on the roll, intern stipend above 30,000. Payroll skips these and reports the count.

## Stack, heap and garbage collection

**Stack.** Every method call gets a frame holding its local primitives and object references. In `PayrollProcessor.process`, `batchGross`, `batchDeductions`, `i`, `start` and `end` are primitives living in the frame, so accumulating totals allocates nothing. Frames vanish when the method returns, so there is nothing to clean up. Deep recursion would overflow the stack; this app uses loops.

**Heap.** All objects live here: every `Employee`, the `HashMap`, the `ArrayList`, `TreeMap` nodes and `String`s. Stack variables only hold references into the heap. The repository holds one reference per employee in both the HashMap and the ArrayList, so employees are not duplicated, only referenced twice.

**Garbage collection.** The GC reclaims heap objects that are no longer reachable from any stack frame, static field or other live object. Short-lived garbage (a report list, a formatted string) dies young and is collected cheaply in a minor collection. Long-lived data (the repository) gets promoted to the old generation. Heavy per-employee allocation across thousands of records raises GC frequency and can cause pauses, so the payroll loop is written to produce as little garbage as possible.

**How the batch processor limits garbage:**

- `subList()` returns a view of the original list, not a copy of each batch.
- Totals are primitive locals; there is no per-employee result object. Only one `Summary` is created per run.
- One `ArrayList<String>` for validation problems and one `StringBuilder` for log lines are created once and reused (`clear()` / `setLength(0)`).
- `Repository.findAll()` returns an unmodifiable view, not a copy.
- Gross and deductions are computed once per employee and net is derived, rather than recomputing through `calculateNetSalary()`.
- Indexed `for` loops avoid creating an `Iterator` per batch.

**Known trade-offs.** Sorting and ranking by net salary call `calculateNetSalary()` inside the comparator, which recomputes on each comparison. That is fine for thousands of records; for very large sets, precompute net once into a parallel `double[]` or a cached field. `Repository.remove` is O(n) because of the `ArrayList`; use a `LinkedHashMap` if removals are frequent.

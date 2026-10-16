package ap.projects.finalproject;

import java.util.ArrayList;
import java.util.List;

public class EmployeeManager {
    private List<Employee> employees;
    private EmployeeFileManager fileManager;

    public EmployeeManager() {
        this.fileManager = new EmployeeFileManager();
        this.employees = fileManager.loadEmployees();
        if (this.employees == null || this.employees.isEmpty()) {
            this.employees = new ArrayList<>();
            initializeDefaultEmployees();
        }
    }

    private void initializeDefaultEmployees() {
        employees.add(new Employee("mr.shahi", "1111", "admin", "1234", "manager"));
        employees.add(new Employee("mrs.tiem", "2222", "staff", "1234", "staff"));
        saveEmployees();
    }

    public void registerEmployee(String name, String employeeId, String username, String password, String role) {
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        if (isUsernameTaken(username)) {
            System.out.println("This username already exists. Please choose a different username.");
            return;
        }

        Employee newEmployee = new Employee(name, employeeId, username, password, role);
        employees.add(newEmployee);
        saveEmployees();
        System.out.println("Employee registration completed successfully.");
    }

    public Employee authenticateEmployee(String username, String password) {
        return employees.stream()
                .filter(e -> e.getUsername().equals(username) && e.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Employee employee = findEmployeeByUsername(username);
        if (employee != null && employee.getPassword().equals(currentPassword)) {
            employee.setPassword(newPassword);
            saveEmployees();
            return true;
        }
        return false;
    }

    private Employee findEmployeeByUsername(String username) {
        return employees.stream()
                .filter(e -> e.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private boolean isUsernameTaken(String username) {
        return employees.stream().anyMatch(e -> e.getUsername().equals(username));
    }

    public void displayAllEmployees() {
        System.out.println("\n--- List of Employees ---");
        if (employees.isEmpty()) {
            System.out.println("No employees registered yet.");
            return;
        }

        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }

    private void saveEmployees() {
        fileManager.saveEmployees(employees);
    }
}

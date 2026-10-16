package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePerformanceManager {
    private Map<String, EmployeePerformance> performanceRecords;
    private EmployeePerformanceFileManager fileManager;

    public EmployeePerformanceManager() {
        this.fileManager = new EmployeePerformanceFileManager();
        this.performanceRecords = fileManager.loadPerformanceRecords();
        if (this.performanceRecords == null) {
            this.performanceRecords = new HashMap<>();
        }
    }

    public EmployeePerformance getEmployeePerformance(String employeeUsername) {
        return performanceRecords.computeIfAbsent(employeeUsername, EmployeePerformance::new);
    }

    public void recordBookAdded(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBooksAdded();
        saveRecords();
    }

    public void recordBookEdited(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBooksEdited();
        saveRecords();
    }

    public void recordBookBorrowed(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBooksBorrowed();
        saveRecords();
    }

    public void recordBookReturned(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBooksReturned();
        saveRecords();
    }

    public void recordBorrowRequestApproved(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBorrowRequestsApproved();
        saveRecords();
    }

    public void recordBorrowRequestRejected(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.incrementBorrowRequestsRejected();
        saveRecords();
    }

    public void displayEmployeePerformance(String employeeUsername) {
        EmployeePerformance performance = getEmployeePerformance(employeeUsername);
        performance.displayPerformance();
    }

    public void displayAllEmployeesPerformance() {
        System.out.println("\n=== All Employees Performance Report ===");
        if (performanceRecords.isEmpty()) {
            System.out.println("No performance records found.");
            return;
        }

        performanceRecords.values().forEach(performance -> {
            System.out.println("\n" + performance);
        });
    }

    public List<EmployeePerformance> getTopPerformers(int limit) {
        return performanceRecords.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getTotalActivities(), p1.getTotalActivities()))
                .limit(limit)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void displayTopPerformers(int limit) {
        List<EmployeePerformance> topPerformers = getTopPerformers(limit);

        System.out.println("\n=== Top " + limit + " Performers ===");
        if (topPerformers.isEmpty()) {
            System.out.println("No performance records found.");
            return;
        }

        for (int i = 0; i < topPerformers.size(); i++) {
            System.out.println((i + 1) + ". " + topPerformers.get(i));
        }
    }

    private void saveRecords() {
        fileManager.savePerformanceRecords(performanceRecords);
    }
}
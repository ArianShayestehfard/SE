package ap.projects.finalproject;

import java.util.ArrayList;
import java.util.List;

public class MenuHandler {
    private final LibrarySystem librarySystem;
    private Student currentUser;
    private Employee currentEmployee;

    public MenuHandler(LibrarySystem librarySystem) {
        this.librarySystem = librarySystem;
        this.currentUser = null;
        this.currentEmployee = null;
    }

    private static class MenuOption {
        private final String label;
        private final Runnable action;

        private MenuOption(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    public void displayMainMenu() {
        List<MenuOption> options = new ArrayList<>();
        options.add(new MenuOption("Student Login", this::handleStudentLogin));
        options.add(new MenuOption("Employee Login", this::handleEmployeeLogin));
        options.add(new MenuOption("View Library Statistics", librarySystem::displayStatistics));
        options.add(new MenuOption("Search Books by Title", librarySystem::searchBooksByTitle));
        options.add(new MenuOption("Student Registration", this::handleStudentRegistration));
        options.add(new MenuOption("Exit", () -> System.out.println("Exiting system. Goodbye!")));

        while (true) {
            System.out.println("\n=== University Library Management System ===");
            printOptions(options);
            int choice = getIntInput(1, options.size());
            options.get(choice - 1).action.run();
            if (choice == options.size()) {
                return;
            }
            System.out.println("___________________________");
        }
    }

    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        String username = readLine("Username: ");
        String password = readLine("Password: ");

        currentUser = librarySystem.authenticateStudent(username, password);
        if (currentUser != null) {
            System.out.println("Login successful! Welcome, " + currentUser.getName());
            displayLoggedInStudentMenu();
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    private void handleStudentRegistration() {
        System.out.println("\n--- New Student Registration ---");
        String name = readLine("Student name: ");
        String studentId = readLine("Student ID: ");
        String username = readLine("Username: ");
        String password = readLine("Password: ");

        librarySystem.registerStudent(name, studentId, username, password);
    }

    private void handleEmployeeLogin() {
        System.out.println("\n--- Employee Login ---");
        String username = readLine("Username: ");
        String password = readLine("Password: ");

        currentEmployee = librarySystem.authenticateEmployee(username, password);
        if (currentEmployee != null) {
            System.out.println("Login successful! Welcome, " + currentEmployee.getName());
            librarySystem.setCurrentEmployee(currentEmployee);
            displayLoggedInEmployeeMenu();
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    private void displayLoggedInStudentMenu() {
        List<MenuOption> options = new ArrayList<>();
        options.add(new MenuOption("View My Information", () -> {
            System.out.println("\n--- My Information ---");
            System.out.println(currentUser);
        }));
        options.add(new MenuOption("Edit My Information", () -> librarySystem.editStudentInformation(currentUser)));
        options.add(new MenuOption("Search Books", librarySystem::searchBooksForStudent));
        options.add(new MenuOption("View Available Books", librarySystem::displayAvailableBooks));
        options.add(new MenuOption("Request to Borrow a Book", () -> librarySystem.requestBorrowBook(currentUser)));
        options.add(new MenuOption("View My Borrow Requests", () -> librarySystem.viewMyBorrowRequests(currentUser)));
        options.add(new MenuOption("View My Borrow History", () -> librarySystem.viewMyBorrows(currentUser)));
        options.add(new MenuOption("Logout", () -> currentUser = null));

        while (currentUser != null) {
            System.out.println("\n=== Student Dashboard ===");
            printOptions(options);
            int choice = getIntInput(1, options.size());
            options.get(choice - 1).action.run();
            System.out.println("___________________________");
        }
        System.out.println("Logged out successfully.");
    }

    private void displayLoggedInEmployeeMenu() {
        List<MenuOption> options = buildEmployeeOptions();
        while (currentEmployee != null) {
            System.out.println("\n=== Employee Dashboard" +
                    (currentEmployee.isManager() ? " (Manager)" : "") + " ===");
            printOptions(options);
            int choice = getIntInput(1, options.size());
            options.get(choice - 1).action.run();
            System.out.println("___________________________");
        }
        System.out.println("Logged out successfully.");
    }

    private List<MenuOption> buildEmployeeOptions() {
        List<MenuOption> options = new ArrayList<>();
        options.add(new MenuOption("Add New Book", librarySystem::addNewBook));
        options.add(new MenuOption("Search Books", librarySystem::searchBooksFromInput));
        options.add(new MenuOption("View Available Books", librarySystem::displayAvailableBooks));
        options.add(new MenuOption("View All Books", librarySystem::displayAllBooks));
        options.add(new MenuOption("Edit Book Information", librarySystem::editBook));
        options.add(new MenuOption("View Pending Borrow Requests (Start Date: Today/Yesterday)",
                librarySystem::displayPendingApprovableRequests));
        options.add(new MenuOption("Approve Borrow Request", librarySystem::approveBorrowRequest));
        options.add(new MenuOption("Reject Borrow Request", librarySystem::rejectBorrowRequest));
        options.add(new MenuOption("Lend Book to Student (Record Pickup)", librarySystem::lendBookToStudent));
        options.add(new MenuOption("Receive Returned Book (Record Return Time)", librarySystem::receiveReturnedBook));
        options.add(new MenuOption("Student Borrow Report", librarySystem::viewStudentBorrowReport));
        options.add(new MenuOption("Activate/Deactivate Student", librarySystem::toggleStudentActivation));
        options.add(new MenuOption("View My Performance", librarySystem::displayMyPerformance));
        options.add(new MenuOption("Change Password", this::handleChangePassword));

        if (currentEmployee.isManager()) {
            options.add(new MenuOption("Register New Employee", this::handleEmployeeRegistration));
            options.add(new MenuOption("View All Employees", librarySystem::displayAllEmployees));
            options.add(new MenuOption("View All Employees Performance", librarySystem::displayAllEmployeesPerformance));
            options.add(new MenuOption("Borrow Statistics", librarySystem::viewBorrowStatistics));
            options.add(new MenuOption("Student Statistics and Top 10 Late Students",
                    librarySystem::displayStudentStatisticsAndTopLate));
        }

        options.add(new MenuOption("Logout", this::logoutEmployee));
        return options;
    }

    private void logoutEmployee() {
        currentEmployee = null;
        librarySystem.setCurrentEmployee(null);
    }

    private void handleChangePassword() {
        System.out.println("\n--- Change Password ---");
        String currentPassword = readLine("Current password: ");
        String newPassword = readLine("New password: ");
        String confirmPassword = readLine("Confirm new password: ");

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        if (librarySystem.changeEmployeePassword(currentEmployee.getUsername(), currentPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Please check your current password.");
        }
    }

    private void handleEmployeeRegistration() {
        System.out.println("\n--- New Employee Registration ---");
        String name = readLine("Employee name: ");
        String employeeId = readLine("Employee ID: ");
        String username = readLine("Username: ");
        String password = readLine("Password: ");
        String role = readLine("Role (manager/staff): ");

        if (!role.equalsIgnoreCase("manager") && !role.equalsIgnoreCase("staff")) {
            System.out.println("Invalid role. Please enter 'manager' or 'staff'.");
            return;
        }

        librarySystem.registerEmployee(name, employeeId, username, password, role.toLowerCase());
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return librarySystem.getScanner().nextLine();
    }

    private void printOptions(List<MenuOption> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i).label);
        }
        System.out.print("Please enter your choice: ");
    }

    private int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(librarySystem.getScanner().nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}

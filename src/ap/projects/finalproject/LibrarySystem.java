package ap.projects.finalproject;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class LibrarySystem {
    private final Scanner scanner;
    private final StudentManager studentManager;
    private final BookManager bookManager;
    private final BorrowManager borrowManager;
    private final BorrowRequestManager borrowRequestManager;
    private final EmployeeManager employeeManager;
    private final StatisticsManager statisticsManager;
    private final EmployeePerformanceManager performanceManager;
    private final MenuHandler menuHandler;
    private Employee currentEmployee;

    public LibrarySystem() {
        this.scanner = new Scanner(System.in);
        this.studentManager = new StudentManager();
        this.bookManager = new BookManager();
        this.borrowManager = new BorrowManager();
        this.borrowRequestManager = new BorrowRequestManager();
        this.employeeManager = new EmployeeManager();
        this.statisticsManager = new StatisticsManager(studentManager, bookManager, borrowManager);
        this.performanceManager = new EmployeePerformanceManager();
        this.menuHandler = new MenuHandler(this);
        this.currentEmployee = null;
    }

    public Scanner getScanner() {
        return scanner;
    }

    // ---------- Guest ----------

    public void displayStatistics() {
        statisticsManager.displayStatistics();
    }

    public void searchBooksByTitle() {
        System.out.println("\n--- Search Books by Title ---");
        System.out.print("Enter book title to search: ");
        String title = scanner.nextLine();

        if (title.isEmpty()) {
            System.out.println("Please enter a title to search.");
            return;
        }

        bookManager.displaySearchResults(bookManager.searchBooksByTitle(title), false);
    }

    // ---------- Student ----------

    public void registerStudent(String name, String studentId, String username, String password) {
        studentManager.registerStudent(name, studentId, username, password);
    }

    public Student authenticateStudent(String username, String password) {
        return studentManager.authenticateStudent(username, password);
    }

    public void searchBooksForStudent() {
        bookManager.searchBooksForStudent(scanner);
    }

    public void displayAvailableBooks() {
        bookManager.displayAvailableBooks();
    }

    public void requestBorrowBook(Student student) {
        System.out.println("\n--- Request to Borrow a Book ---");
        bookManager.displayAvailableBooks();

        System.out.print("Enter ISBN of the book you want to borrow: ");
        String isbn = scanner.nextLine();
        Book book = bookManager.findBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Invalid ISBN.");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is not available.");
            return;
        }

        System.out.print("Enter desired start date (YYYY-MM-DD) or press Enter for today: ");
        String startInput = scanner.nextLine();
        LocalDate startDate = LocalDate.now();
        if (!startInput.isEmpty()) {
            try {
                startDate = LocalDate.parse(startInput);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                return;
            }
        }
        if (startDate.isBefore(LocalDate.now())) {
            System.out.println("Start date cannot be in the past.");
            return;
        }

        System.out.print("Enter desired end date (YYYY-MM-DD): ");
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }
        if (!endDate.isAfter(startDate)) {
            System.out.println("End date must be after the start date.");
            return;
        }

        if (!student.isActive()) {
            System.out.println("Your account is inactive. You cannot borrow books.");
            return;
        }

        borrowRequestManager.createBorrowRequest(student.getUsername(), isbn, startDate, endDate);
    }

    public void viewMyBorrowRequests(Student student) {
        List<BorrowRequest> requests = borrowRequestManager.getStudentRequests(student.getUsername());

        System.out.println("\n--- My Borrow Requests ---");
        if (requests.isEmpty()) {
            System.out.println("You have no borrow requests.");
            return;
        }

        requests.forEach(System.out::println);
    }

    public void viewMyBorrows(Student student) {
        System.out.println("\n--- My Borrow History ---");
        List<BorrowRecord> borrows = borrowManager.getStudentBorrows(student.getUsername());

        if (borrows.isEmpty()) {
            System.out.println("You have no borrow history.");
            return;
        }

        borrows.forEach(System.out::println);
    }

    public void editStudentInformation(Student student) {
        System.out.println("\n--- Edit My Information ---");
        System.out.print("New name (current: " + student.getName() + "): ");
        String newName = scanner.nextLine();
        System.out.print("New password (press Enter to keep the current one): ");
        String newPassword = scanner.nextLine();

        if (studentManager.updateStudentInfo(student.getUsername(), newName, newPassword)) {
            System.out.println("Information updated successfully.");
        } else {
            System.out.println("Nothing to update.");
        }
    }

    // ---------- Employee ----------

    public void addNewBook() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        Book newBook = bookManager.createBookFromInput(scanner);
        if (newBook != null) {
            bookManager.addNewBook(newBook);
            recordBookAdded();
        }
    }

    public void editBook() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Edit Book Information ---");
        System.out.print("Enter ISBN of the book to edit: ");
        String isbn = scanner.nextLine();

        if (bookManager.editBookFromInput(scanner, isbn)) {
            recordBookEdited();
        }
    }

    public void searchBooksFromInput() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        bookManager.searchBooksFromInput(scanner);
    }

    public void displayAllBooks() {
        bookManager.displayAllBooks();
    }

    public void displayPendingApprovableRequests() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        borrowRequestManager.displayPendingApprovableRequests();
    }

    public void approveBorrowRequest() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Approve Borrow Request ---");
        borrowRequestManager.displayPendingApprovableRequests();

        System.out.print("\nEnter student username to approve: ");
        String username = scanner.nextLine();
        System.out.print("Enter book ISBN to approve: ");
        String isbn = scanner.nextLine();

        Student student = studentManager.findStudentByUsername(username);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }
        if (!student.isActive()) {
            System.out.println("Student is inactive and cannot borrow books.");
            return;
        }

        BorrowRequest request = borrowRequestManager.findPendingRequest(username, isbn);
        if (request == null) {
            System.out.println("No pending borrow request found for this student and book.");
            return;
        }
        if (!request.isApprovableDate()) {
            System.out.println("Only requests whose start date is today or yesterday can be approved.");
            return;
        }

        Book book = bookManager.findBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Invalid ISBN.");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is not available.");
            return;
        }

        borrowRequestManager.approveRequest(request);
        bookManager.borrowBook(isbn);
        borrowManager.createBorrow(username, isbn, request.getDesiredBorrowDate(), request.getDesiredEndDate());
        recordBorrowRequestApproved();
        System.out.println("Borrow request approved. The book can now be lent to the student.");
    }

    public void rejectBorrowRequest() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Reject Borrow Request ---");
        borrowRequestManager.displayPendingRequests();

        System.out.print("\nEnter student username to reject: ");
        String username = scanner.nextLine();
        System.out.print("Enter book ISBN to reject: ");
        String isbn = scanner.nextLine();
        System.out.print("Enter rejection reason: ");
        String reason = scanner.nextLine();

        BorrowRequest request = borrowRequestManager.findPendingRequest(username, isbn);
        if (request == null) {
            System.out.println("No pending borrow request found for this student and book.");
            return;
        }

        borrowRequestManager.rejectRequest(request, reason);
        recordBorrowRequestRejected();
        System.out.println("Borrow request rejected successfully!");
    }

    public void lendBookToStudent() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Lend Book to Student ---");
        borrowManager.displayReadyForPickupBooks();

        System.out.print("Enter student username: ");
        String username = scanner.nextLine();
        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        if (borrowManager.markBookAsPickedUp(username, isbn)) {
            recordBookBorrowed();
            System.out.println("Book handed to the student. Pickup time recorded.");
            BorrowRecord record = borrowManager.findActiveBorrow(username, isbn);
            if (record != null) {
                System.out.println("Due date: " + record.getDueDate());
            }
        } else {
            System.out.println("No approved book ready for pickup found for this student and ISBN.");
        }
    }

    public void receiveReturnedBook() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Receive Returned Book ---");
        List<BorrowRecord> borrowedBooks = borrowManager.getActiveBorrows();
        if (borrowedBooks.isEmpty()) {
            System.out.println("No books are currently borrowed.");
            return;
        }

        System.out.println("\nCurrently borrowed books:");
        borrowedBooks.forEach(System.out::println);

        System.out.print("Enter student username: ");
        String username = scanner.nextLine();
        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine();

        if (borrowManager.returnBook(username, isbn)) {
            bookManager.returnBook(isbn);
            recordBookReturned();
            System.out.println("Book received back. Return time recorded.");
        } else {
            System.out.println("No borrowed book found for this student and ISBN (or it was never picked up).");
        }
    }

    public void viewStudentBorrowReport() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n=== Student Borrow Report ===");
        System.out.print("Enter student username: ");
        String username = scanner.nextLine();

        Student student = studentManager.findStudentByUsername(username);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        borrowManager.generateStudentReport(username).displayReport();
    }

    public void toggleStudentActivation() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        System.out.println("\n--- Activate/Deactivate Student ---");
        System.out.print("Enter student username: ");
        String username = scanner.nextLine();

        Student student = studentManager.findStudentByUsername(username);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        if (studentManager.toggleStudentStatus(username)) {
            System.out.println("Student status updated successfully. Student is now " +
                    (student.isActive() ? "activated" : "deactivated") + ".");
        } else {
            System.out.println("Failed to update student status.");
        }
    }

    // ---------- Manager ----------

    public void registerEmployee(String name, String employeeId, String username, String password, String role) {
        if (!isManagerLoggedIn()) {
            return;
        }

        employeeManager.registerEmployee(name, employeeId, username, password, role);
    }

    public void displayAllEmployees() {
        if (!isManagerLoggedIn()) {
            return;
        }

        employeeManager.displayAllEmployees();
    }

    public void displayAllEmployeesPerformance() {
        if (!isManagerLoggedIn()) {
            return;
        }

        performanceManager.displayAllEmployeesPerformance();
    }

    public void viewBorrowStatistics() {
        if (!isManagerLoggedIn()) {
            return;
        }

        BorrowStatistics statistics = new BorrowStatistics(
                borrowManager.getAllBorrows(), borrowRequestManager.getAllRequests());
        statistics.displayStatistics();
    }

    public void displayStudentStatisticsAndTopLate() {
        if (!isManagerLoggedIn()) {
            return;
        }

        List<BorrowRecord> borrows = borrowManager.getAllBorrows();
        studentManager.generateStudentStatistics(borrows).displayStatistics();
        studentManager.displayStudentsWithMostLate(10, borrows);
    }

    public void displayMyPerformance() {
        if (!isEmployeeLoggedIn()) {
            return;
        }

        performanceManager.displayEmployeePerformance(currentEmployee.getUsername());
    }

    // ---------- Authentication ----------

    public Employee authenticateEmployee(String username, String password) {
        return employeeManager.authenticateEmployee(username, password);
    }

    public boolean changeEmployeePassword(String username, String currentPassword, String newPassword) {
        return employeeManager.changePassword(username, currentPassword, newPassword);
    }

    public void setCurrentEmployee(Employee employee) {
        this.currentEmployee = employee;
    }

    // ---------- Performance recording ----------

    private void recordBookAdded() {
        if (currentEmployee != null) {
            performanceManager.recordBookAdded(currentEmployee.getUsername());
        }
    }

    private void recordBookEdited() {
        if (currentEmployee != null) {
            performanceManager.recordBookEdited(currentEmployee.getUsername());
        }
    }

    private void recordBookBorrowed() {
        if (currentEmployee != null) {
            performanceManager.recordBookBorrowed(currentEmployee.getUsername());
        }
    }

    private void recordBookReturned() {
        if (currentEmployee != null) {
            performanceManager.recordBookReturned(currentEmployee.getUsername());
        }
    }

    private void recordBorrowRequestApproved() {
        if (currentEmployee != null) {
            performanceManager.recordBorrowRequestApproved(currentEmployee.getUsername());
        }
    }

    private void recordBorrowRequestRejected() {
        if (currentEmployee != null) {
            performanceManager.recordBorrowRequestRejected(currentEmployee.getUsername());
        }
    }

    // ---------- Guards ----------

    private boolean isEmployeeLoggedIn() {
        if (currentEmployee == null) {
            System.out.println("Only employees can perform this action.");
            return false;
        }
        return true;
    }

    private boolean isManagerLoggedIn() {
        if (currentEmployee == null || !currentEmployee.isManager()) {
            System.out.println("Only managers can perform this action.");
            return false;
        }
        return true;
    }

    // ---------- Entry point ----------

    public void start() {
        menuHandler.displayMainMenu();
    }

    public static void main(String[] args) {
        new LibrarySystem().start();
    }
}

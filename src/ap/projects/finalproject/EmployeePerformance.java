package ap.projects.finalproject;

import java.io.Serializable;
import java.time.LocalDate;

public class EmployeePerformance implements Serializable {
    private static final long serialVersionUID = 5L;
    private String employeeUsername;
    private int booksAdded;
    private int booksEdited;
    private int booksBorrowed;
    private int booksReturned;
    private int borrowRequestsApproved;
    private int borrowRequestsRejected;
    private LocalDate lastActivityDate;

    public EmployeePerformance(String employeeUsername) {
        this.employeeUsername = employeeUsername;
        this.booksAdded = 0;
        this.booksEdited = 0;
        this.booksBorrowed = 0;
        this.booksReturned = 0;
        this.borrowRequestsApproved = 0;
        this.borrowRequestsRejected = 0;
        this.lastActivityDate = LocalDate.now();
    }

    public String getEmployeeUsername() { return employeeUsername; }
    public int getBooksAdded() { return booksAdded; }
    public int getBooksEdited() { return booksEdited; }
    public int getBooksBorrowed() { return booksBorrowed; }
    public int getBooksReturned() { return booksReturned; }
    public int getBorrowRequestsApproved() { return borrowRequestsApproved; }
    public int getBorrowRequestsRejected() { return borrowRequestsRejected; }
    public LocalDate getLastActivityDate() { return lastActivityDate; }

    public void incrementBooksAdded() {
        booksAdded++;
        lastActivityDate = LocalDate.now();
    }

    public void incrementBooksEdited() {
        booksEdited++;
        lastActivityDate = LocalDate.now();
    }

    public void incrementBooksBorrowed() {
        booksBorrowed++;
        lastActivityDate = LocalDate.now();
    }

    public void incrementBooksReturned() {
        booksReturned++;
        lastActivityDate = LocalDate.now();
    }

    public void incrementBorrowRequestsApproved() {
        borrowRequestsApproved++;
        lastActivityDate = LocalDate.now();
    }

    public void incrementBorrowRequestsRejected() {
        borrowRequestsRejected++;
        lastActivityDate = LocalDate.now();
    }

    public int getTotalActivities() {
        return booksAdded + booksEdited + booksBorrowed + booksReturned +
                borrowRequestsApproved + borrowRequestsRejected;
    }

    @Override
    public String toString() {
        return "Employee: " + employeeUsername +
                " | Books Added: " + booksAdded +
                " | Books Edited: " + booksEdited +
                " | Books Lent: " + booksBorrowed +
                " | Books Received Back: " + booksReturned +
                " | Requests Approved: " + borrowRequestsApproved +
                " | Requests Rejected: " + borrowRequestsRejected +
                " | Last Activity: " + lastActivityDate +
                " | Total Activities: " + getTotalActivities();
    }

    public void displayPerformance() {
        System.out.println("\n=== Employee Performance Report ===");
        System.out.println("Employee: " + employeeUsername);
        System.out.println("Books Added: " + booksAdded);
        System.out.println("Books Edited: " + booksEdited);
        System.out.println("Books Lent: " + booksBorrowed);
        System.out.println("Books Received Back: " + booksReturned);
        System.out.println("Borrow Requests Approved: " + borrowRequestsApproved);
        System.out.println("Borrow Requests Rejected: " + borrowRequestsRejected);
        System.out.println("Total Activities: " + getTotalActivities());
        System.out.println("Last Activity Date: " + lastActivityDate);
    }
}

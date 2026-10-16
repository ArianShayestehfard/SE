package ap.projects.finalproject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowRecord implements Serializable {
    private static final long serialVersionUID = 3L;
    private String studentUsername;
    private String bookIsbn;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LocalDate actualPickupDate;
    private boolean isReturned;
    private boolean isPickedUp;

    public BorrowRecord(String studentUsername, String bookIsbn, LocalDate borrowDate, LocalDate dueDate) {
        this.studentUsername = studentUsername;
        this.bookIsbn = bookIsbn;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.isReturned = false;
        this.isPickedUp = false;
        this.returnDate = null;
        this.actualPickupDate = null;
    }

    public String getStudentUsername() { return studentUsername; }
    public String getBookIsbn() { return bookIsbn; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LocalDate getActualPickupDate() { return actualPickupDate; }
    public boolean isReturned() { return isReturned; }
    public boolean isPickedUp() { return isPickedUp; }

    public void markAsPickedUp() {
        this.isPickedUp = true;
        this.actualPickupDate = LocalDate.now();
    }

    public void markAsReturned() {
        this.isReturned = true;
        this.returnDate = LocalDate.now();
    }

    public boolean isOverdue() {
        return isPickedUp && !isReturned && LocalDate.now().isAfter(dueDate);
    }

    public boolean isReturnedLate() {
        return isReturned && returnDate != null && dueDate != null && returnDate.isAfter(dueDate);
    }

    public boolean isLate() {
        return isReturnedLate() || isOverdue();
    }

    public boolean isReadyForPickup() {
        return !isPickedUp && !isReturned;
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String status;

        if (isReturned) {
            status = isReturnedLate()
                    ? "Returned late on " + returnDate.format(formatter)
                    : "Returned on " + returnDate.format(formatter);
        } else if (isPickedUp) {
            status = isOverdue() ? "OVERDUE (" + getDaysOverdue() + " days)" : "Due on " + dueDate.format(formatter);
        } else {
            status = "Ready for pickup";
        }

        return "Book ISBN: " + bookIsbn +
                " | Borrowed: " + borrowDate.format(formatter) +
                " | Due: " + dueDate.format(formatter) +
                (actualPickupDate != null ? " | Picked up: " + actualPickupDate.format(formatter) : "") +
                " | Status: " + status;
    }
}

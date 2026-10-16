package ap.projects.finalproject;

import java.io.Serializable;
import java.time.LocalDate;

public class BorrowRequest implements Serializable {
    private static final long serialVersionUID = 5L;
    private String studentUsername;
    private String bookIsbn;
    private LocalDate requestDate;
    private LocalDate desiredBorrowDate;
    private LocalDate desiredEndDate;
    private String status; // "pending", "approved", "rejected"
    private String rejectionReason;

    public BorrowRequest(String studentUsername, String bookIsbn,
                         LocalDate desiredBorrowDate, LocalDate desiredEndDate) {
        this.studentUsername = studentUsername;
        this.bookIsbn = bookIsbn;
        this.requestDate = LocalDate.now();
        this.desiredBorrowDate = desiredBorrowDate;
        this.desiredEndDate = desiredEndDate;
        this.status = "pending";
        this.rejectionReason = "";
    }

    public String getStudentUsername() { return studentUsername; }
    public String getBookIsbn() { return bookIsbn; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getDesiredBorrowDate() { return desiredBorrowDate; }
    public LocalDate getDesiredEndDate() { return desiredEndDate; }
    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }

    public void approve() {
        this.status = "approved";
        this.rejectionReason = "";
    }

    public void reject(String reason) {
        this.status = "rejected";
        this.rejectionReason = reason;
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public boolean isApprovableDate() {
        LocalDate today = LocalDate.now();
        return desiredBorrowDate.equals(today) || desiredBorrowDate.equals(today.minusDays(1));
    }

    @Override
    public String toString() {
        String desired = desiredEndDate != null
                ? desiredBorrowDate + " to " + desiredEndDate
                : desiredBorrowDate.toString();
        return "Student: " + studentUsername +
                " | Book ISBN: " + bookIsbn +
                " | Request Date: " + requestDate +
                " | Desired Period: " + desired +
                " | Status: " + status +
                (isRejected() ? " | Reason: " + rejectionReason : "");
    }
}

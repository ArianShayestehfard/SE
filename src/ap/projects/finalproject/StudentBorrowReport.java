package ap.projects.finalproject;

import java.util.List;

public class StudentBorrowReport {
    private final String studentUsername;
    private final List<BorrowRecord> borrowHistory;
    private final int totalBorrows;
    private final int notDeliveredCount;
    private final int activeBorrows;
    private final int currentlyOverdueCount;
    private final int lateReturnCount;
    private final int returnedBorrows;

    public StudentBorrowReport(String studentUsername, List<BorrowRecord> borrowHistory) {
        this.studentUsername = studentUsername;
        this.borrowHistory = borrowHistory;
        this.totalBorrows = borrowHistory.size();
        this.notDeliveredCount = (int) borrowHistory.stream()
                .filter(record -> !record.isPickedUp() && !record.isReturned())
                .count();
        this.activeBorrows = (int) borrowHistory.stream()
                .filter(record -> record.isPickedUp() && !record.isReturned())
                .count();
        this.currentlyOverdueCount = (int) borrowHistory.stream()
                .filter(BorrowRecord::isOverdue)
                .count();
        this.lateReturnCount = (int) borrowHistory.stream()
                .filter(BorrowRecord::isReturnedLate)
                .count();
        this.returnedBorrows = (int) borrowHistory.stream()
                .filter(BorrowRecord::isReturned)
                .count();
    }

    public void displayReport() {
        System.out.println("\n=== Student Borrow Report ===");
        System.out.println("Student Username: " + studentUsername);
        System.out.println("Total Borrows: " + totalBorrows);
        System.out.println("Not Delivered Yet (Ready for Pickup): " + notDeliveredCount);
        System.out.println("Active Borrows: " + activeBorrows);
        System.out.println("Currently Overdue: " + currentlyOverdueCount);
        System.out.println("Returned Late: " + lateReturnCount);
        System.out.println("Returned: " + returnedBorrows);

        System.out.println("\n--- Borrow History ---");
        if (borrowHistory.isEmpty()) {
            System.out.println("No borrow history found.");
        } else {
            borrowHistory.forEach(System.out::println);
        }

        double returnRate = totalBorrows > 0 ? (returnedBorrows * 100.0 / totalBorrows) : 0;
        System.out.printf("\nReturn Rate: %.1f%%\n", returnRate);

        if (currentlyOverdueCount > 0) {
            System.out.println("This student has " + currentlyOverdueCount + " currently overdue book(s)!");
        }
        if (lateReturnCount > 0) {
            System.out.println("This student has returned " + lateReturnCount + " book(s) late!");
        }
    }
}

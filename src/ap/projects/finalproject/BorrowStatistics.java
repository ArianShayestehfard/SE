package ap.projects.finalproject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BorrowStatistics implements Serializable {
    private static final long serialVersionUID = 6L;
    private int totalBorrowRequests;
    private int approvedBorrowRequests;
    private int rejectedBorrowRequests;
    private int pendingBorrowRequests;
    private int totalBorrows;
    private int activeBorrows;
    private int completedBorrows;
    private int overdueBorrows;
    private double averageBorrowDays;

    public BorrowStatistics(List<BorrowRecord> borrowRecords, List<BorrowRequest> borrowRequests) {
        calculateStatistics(borrowRecords, borrowRequests);
    }

    private void calculateStatistics(List<BorrowRecord> borrowRecords, List<BorrowRequest> borrowRequests) {
        this.totalBorrowRequests = borrowRequests.size();
        this.approvedBorrowRequests = (int) borrowRequests.stream()
                .filter(BorrowRequest::isApproved)
                .count();
        this.rejectedBorrowRequests = (int) borrowRequests.stream()
                .filter(BorrowRequest::isRejected)
                .count();
        this.pendingBorrowRequests = (int) borrowRequests.stream()
                .filter(BorrowRequest::isPending)
                .count();

        this.totalBorrows = borrowRecords.size();
        this.activeBorrows = (int) borrowRecords.stream()
                .filter(record -> !record.isReturned() && record.isPickedUp())
                .count();
        this.completedBorrows = (int) borrowRecords.stream()
                .filter(BorrowRecord::isReturned)
                .count();
        this.overdueBorrows = (int) borrowRecords.stream()
                .filter(BorrowRecord::isOverdue)
                .count();

        calculateAverageBorrowDays(borrowRecords);
    }

    private void calculateAverageBorrowDays(List<BorrowRecord> borrowRecords) {
        long totalDays = 0;
        int count = 0;

        for (BorrowRecord record : borrowRecords) {
            if (record.isReturned() && record.getActualPickupDate() != null && record.getReturnDate() != null) {
                long days = ChronoUnit.DAYS.between(record.getActualPickupDate(), record.getReturnDate());
                totalDays += days;
                count++;
            }
        }

        this.averageBorrowDays = count > 0 ? (double) totalDays / count : 0.0;
    }

    public int getTotalBorrowRequests() { return totalBorrowRequests; }
    public int getApprovedBorrowRequests() { return approvedBorrowRequests; }
    public int getRejectedBorrowRequests() { return rejectedBorrowRequests; }
    public int getPendingBorrowRequests() { return pendingBorrowRequests; }
    public int getTotalBorrows() { return totalBorrows; }
    public int getActiveBorrows() { return activeBorrows; }
    public int getCompletedBorrows() { return completedBorrows; }
    public int getOverdueBorrows() { return overdueBorrows; }
    public double getAverageBorrowDays() { return averageBorrowDays; }

    public void displayStatistics() {
        System.out.println("\n=== Borrow Statistics ===");
        System.out.println("=== Borrow Requests ===");
        System.out.println("Total Borrow Requests: " + totalBorrowRequests);
        System.out.println("Approved Requests: " + approvedBorrowRequests);
        System.out.println("Rejected Requests: " + rejectedBorrowRequests);
        System.out.println("Pending Requests: " + pendingBorrowRequests);

        System.out.println("\n=== Borrow Operations ===");
        System.out.println("Total Borrows: " + totalBorrows);
        System.out.println("Active Borrows: " + activeBorrows);
        System.out.println("Completed Borrows: " + completedBorrows);
        System.out.println("Overdue Borrows: " + overdueBorrows);
        System.out.printf("Average Borrow Duration: %.2f days\n", averageBorrowDays);

        if (totalBorrowRequests > 0) {
            double approvalRate = (approvedBorrowRequests * 100.0) / totalBorrowRequests;
            double rejectionRate = (rejectedBorrowRequests * 100.0) / totalBorrowRequests;
            System.out.printf("\nApproval Rate: %.1f%%\n", approvalRate);
            System.out.printf("Rejection Rate: %.1f%%\n", rejectionRate);
        }

        if (totalBorrows > 0) {
            double completionRate = (completedBorrows * 100.0) / totalBorrows;
            double overdueRate = (overdueBorrows * 100.0) / totalBorrows;
            System.out.printf("Completion Rate: %.1f%%\n", completionRate);
            System.out.printf("Overdue Rate: %.1f%%\n", overdueRate);
        }
    }
}
package ap.projects.finalproject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowRequestManager {
    private List<BorrowRequest> borrowRequests;
    private BorrowRequestFileManager fileManager;

    public BorrowRequestManager() {
        this.fileManager = new BorrowRequestFileManager();
        this.borrowRequests = fileManager.loadBorrowRequests();
        if (this.borrowRequests == null) {
            this.borrowRequests = new ArrayList<>();
        }
    }

    public void createBorrowRequest(String studentUsername, String bookIsbn,
                                    LocalDate desiredBorrowDate, LocalDate desiredEndDate) {
        if (findPendingRequest(studentUsername, bookIsbn) != null) {
            System.out.println("You already have a pending request for this book.");
            return;
        }

        BorrowRequest request = new BorrowRequest(studentUsername, bookIsbn, desiredBorrowDate, desiredEndDate);
        borrowRequests.add(request);
        saveRequests();
        System.out.println("Borrow request submitted successfully. Waiting for approval.");
    }

    public List<BorrowRequest> getAllRequests() {
        return new ArrayList<>(borrowRequests);
    }

    public List<BorrowRequest> getPendingRequests() {
        return borrowRequests.stream()
                .filter(BorrowRequest::isPending)
                .collect(Collectors.toList());
    }

    public List<BorrowRequest> getPendingApprovableRequests() {
        return borrowRequests.stream()
                .filter(request -> request.isPending() && request.isApprovableDate())
                .collect(Collectors.toList());
    }

    public List<BorrowRequest> getStudentRequests(String studentUsername) {
        return borrowRequests.stream()
                .filter(request -> request.getStudentUsername().equals(studentUsername))
                .collect(Collectors.toList());
    }

    public BorrowRequest findPendingRequest(String studentUsername, String bookIsbn) {
        return borrowRequests.stream()
                .filter(request -> request.getStudentUsername().equals(studentUsername) &&
                        request.getBookIsbn().equals(bookIsbn) &&
                        request.isPending())
                .findFirst()
                .orElse(null);
    }

    public void approveRequest(BorrowRequest request) {
        request.approve();
        saveRequests();
    }

    public void rejectRequest(BorrowRequest request, String reason) {
        request.reject(reason);
        saveRequests();
    }

    public void displayPendingRequests() {
        List<BorrowRequest> pendingRequests = getPendingRequests();

        System.out.println("\n--- Pending Borrow Requests ---");
        if (pendingRequests.isEmpty()) {
            System.out.println("No pending borrow requests.");
            return;
        }

        pendingRequests.forEach(System.out::println);
    }

    public void displayPendingApprovableRequests() {
        List<BorrowRequest> approvableRequests = getPendingApprovableRequests();

        System.out.println("\n--- Pending Borrow Requests (Start Date: Today or Yesterday) ---");
        if (approvableRequests.isEmpty()) {
            System.out.println("No pending borrow requests to approve today.");
            return;
        }

        approvableRequests.forEach(System.out::println);
    }

    private void saveRequests() {
        fileManager.saveBorrowRequests(borrowRequests);
    }
}

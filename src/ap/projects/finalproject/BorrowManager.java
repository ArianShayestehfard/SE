package ap.projects.finalproject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowManager {
    private List<BorrowRecord> borrowRecords;
    private BorrowFileManager fileManager;

    public BorrowManager() {
        this.fileManager = new BorrowFileManager();
        this.borrowRecords = fileManager.loadBorrowRecords();
        if (this.borrowRecords == null) {
            this.borrowRecords = new ArrayList<>();
        }
    }

    public boolean markBookAsPickedUp(String studentUsername, String bookIsbn) {
        BorrowRecord record = findReadyForPickupRecord(studentUsername, bookIsbn);
        if (record != null) {
            record.markAsPickedUp();
            saveRecords();
            return true;
        }
        return false;
    }

    public BorrowRecord findReadyForPickupRecord(String studentUsername, String bookIsbn) {
        return borrowRecords.stream()
                .filter(record -> record.getStudentUsername().equals(studentUsername) &&
                        record.getBookIsbn().equals(bookIsbn) &&
                        record.isReadyForPickup())
                .findFirst()
                .orElse(null);
    }

    public List<BorrowRecord> getAllReadyForPickupRecords() {
        return borrowRecords.stream()
                .filter(BorrowRecord::isReadyForPickup)
                .collect(Collectors.toList());
    }

    public void displayReadyForPickupBooks() {
        List<BorrowRecord> readyRecords = getAllReadyForPickupRecords();

        System.out.println("\n--- Books Ready for Pickup ---");
        if (readyRecords.isEmpty()) {
            System.out.println("No books ready for pickup.");
            return;
        }

        readyRecords.forEach(System.out::println);
    }

    public StudentBorrowReport generateStudentReport(String studentUsername) {
        return new StudentBorrowReport(studentUsername, getStudentBorrows(studentUsername));
    }

    public List<BorrowRecord> getAllBorrows() {
        return new ArrayList<>(borrowRecords);
    }

    public int getTotalBorrowsCount() {
        return borrowRecords.size();
    }

    public int getBorrowedOutCount() {
        return (int) borrowRecords.stream()
                .filter(record -> record.isPickedUp() && !record.isReturned())
                .count();
    }

    public void createBorrow(String studentUsername, String bookIsbn, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        // Records saved before the end date was stored fall back to a two-week loan.
        LocalDate end = endDate != null ? endDate : start.plusDays(14);
        borrowRecords.add(new BorrowRecord(studentUsername, bookIsbn, start, end));
        saveRecords();
    }

    public boolean returnBook(String studentUsername, String bookIsbn) {
        BorrowRecord record = findActiveBorrow(studentUsername, bookIsbn);
        if (record != null && record.isPickedUp()) {
            record.markAsReturned();
            saveRecords();
            return true;
        }
        return false;
    }

    public BorrowRecord findActiveBorrow(String studentUsername, String bookIsbn) {
        return borrowRecords.stream()
                .filter(record -> record.getStudentUsername().equals(studentUsername) &&
                        record.getBookIsbn().equals(bookIsbn) &&
                        !record.isReturned())
                .findFirst()
                .orElse(null);
    }

    public List<BorrowRecord> getStudentBorrows(String studentUsername) {
        return borrowRecords.stream()
                .filter(record -> record.getStudentUsername().equals(studentUsername))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getActiveBorrows() {
        return borrowRecords.stream()
                .filter(record -> !record.isReturned())
                .collect(Collectors.toList());
    }

    private void saveRecords() {
        fileManager.saveBorrowRecords(borrowRecords);
    }
}

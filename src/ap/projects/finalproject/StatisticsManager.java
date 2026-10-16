package ap.projects.finalproject;

public class StatisticsManager {
    private final StudentManager studentManager;
    private final BookManager bookManager;
    private final BorrowManager borrowManager;

    public StatisticsManager(StudentManager studentManager, BookManager bookManager, BorrowManager borrowManager) {
        this.studentManager = studentManager;
        this.bookManager = bookManager;
        this.borrowManager = borrowManager;
    }

    public void displayStatistics() {
        System.out.println("\n=== Library Statistics ===");
        System.out.println("Registered Students: " + studentManager.getStudentCount());
        System.out.println("Total Books: " + bookManager.getTotalBooksCount());
        System.out.println("Total Borrows: " + borrowManager.getTotalBorrowsCount());
        System.out.println("Books Currently Borrowed: " + borrowManager.getBorrowedOutCount());
    }
}

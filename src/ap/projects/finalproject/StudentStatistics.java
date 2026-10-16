package ap.projects.finalproject;

import java.io.Serializable;
import java.util.List;

public class StudentStatistics implements Serializable {
    private static final long serialVersionUID = 6L;
    private int totalStudents;
    private int activeStudents;
    private int inactiveStudents;
    private int totalBorrows;
    private int undeliveredBorrows;
    private int lateBorrows;
    private int studentsWithBorrows;
    private int studentsWithLateBooks;
    private double averageBorrowsPerStudent;

    public StudentStatistics(List<Student> students, List<BorrowRecord> borrowRecords) {
        calculateStatistics(students, borrowRecords);
    }

    private void calculateStatistics(List<Student> students, List<BorrowRecord> borrowRecords) {
        this.totalStudents = students.size();
        this.activeStudents = (int) students.stream().filter(Student::isActive).count();
        this.inactiveStudents = totalStudents - activeStudents;

        this.totalBorrows = borrowRecords.size();
        this.undeliveredBorrows = (int) borrowRecords.stream()
                .filter(record -> !record.isPickedUp() && !record.isReturned())
                .count();
        this.lateBorrows = (int) borrowRecords.stream()
                .filter(BorrowRecord::isLate)
                .count();

        this.studentsWithBorrows = (int) students.stream()
                .filter(student -> hasBorrows(student.getUsername(), borrowRecords))
                .count();
        this.studentsWithLateBooks = (int) students.stream()
                .filter(student -> hasLateBorrows(student.getUsername(), borrowRecords))
                .count();

        this.averageBorrowsPerStudent = studentsWithBorrows > 0 ?
                (double) totalBorrows / studentsWithBorrows : 0;
    }

    private boolean hasBorrows(String username, List<BorrowRecord> borrowRecords) {
        return borrowRecords.stream()
                .anyMatch(record -> record.getStudentUsername().equals(username));
    }

    private boolean hasLateBorrows(String username, List<BorrowRecord> borrowRecords) {
        return borrowRecords.stream()
                .anyMatch(record -> record.getStudentUsername().equals(username) && record.isLate());
    }

    public void displayStatistics() {
        System.out.println("\n=== Student Statistics ===");
        System.out.println("Total Students: " + totalStudents);
        System.out.println("Active Students: " + activeStudents);
        System.out.println("Inactive Students: " + inactiveStudents);

        System.out.println("\n--- Borrow Statistics (All Students) ---");
        System.out.println("Total Borrows: " + totalBorrows);
        System.out.println("Undelivered Books (Ready for Pickup): " + undeliveredBorrows);
        System.out.println("Late Borrows: " + lateBorrows);
        System.out.println("Students with Borrows: " + studentsWithBorrows);
        System.out.println("Students with Late Books: " + studentsWithLateBooks);
        System.out.printf("Average Borrows per Student: %.2f\n", averageBorrowsPerStudent);
    }
}

package ap.projects.finalproject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentManager {
    private List<Student> students;
    private StudentFileManager fileManager;

    public StudentManager() {
        this.fileManager = new StudentFileManager();
        this.students = fileManager.loadStudents();

        if (this.students == null) {
            this.students = new ArrayList<>();
        }
    }

    public StudentStatistics generateStudentStatistics(List<BorrowRecord> borrowRecords) {
        return new StudentStatistics(students, borrowRecords);
    }

    public void displayStudentsWithMostLate(int limit, List<BorrowRecord> borrowRecords) {
        List<String> topLateStudents = students.stream()
                .map(student -> {
                    long lateCount = borrowRecords.stream()
                            .filter(record -> record.getStudentUsername().equals(student.getUsername())
                                    && record.isLate())
                            .count();
                    return new StudentLateInfo(student, lateCount);
                })
                .filter(info -> info.getLateCount() > 0)
                .sorted((i1, i2) -> Long.compare(i2.getLateCount(), i1.getLateCount()))
                .limit(limit)
                .map(StudentLateInfo::toString)
                .collect(Collectors.toList());

        System.out.println("\n=== Top " + limit + " Students with Most Late Books ===");
        if (topLateStudents.isEmpty()) {
            System.out.println("No students with late books.");
            return;
        }

        for (int i = 0; i < topLateStudents.size(); i++) {
            System.out.println((i + 1) + ". " + topLateStudents.get(i));
        }
    }

    private static class StudentLateInfo {
        private final Student student;
        private final long lateCount;

        public StudentLateInfo(Student student, long lateCount) {
            this.student = student;
            this.lateCount = lateCount;
        }

        public long getLateCount() {
            return lateCount;
        }

        @Override
        public String toString() {
            return student.getName() + " (" + student.getUsername() + ") - " +
                    lateCount + " late book(s)";
        }
    }

    public void registerStudent(String name, String studentId, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        if (isUsernameTaken(username)) {
            System.out.println("This username already exists. Please choose a different username.");
            return;
        }

        Student newStudent = new Student(name, studentId, username, password);
        students.add(newStudent);
        fileManager.saveStudents(students);
        System.out.println("Student registration completed successfully.");
    }

    public Student authenticateStudent(String username, String password) {
        Student student = students.stream()
                .filter(s -> s.getUsername().equals(username) && s.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (student != null && !student.isActive()) {
            System.out.println("Your account is inactive. Please contact the library administrator.");
            return null;
        }

        return student;
    }

    public boolean updateStudentInfo(String username, String newName, String newPassword) {
        Student student = findStudentByUsername(username);
        if (student == null) {
            return false;
        }

        boolean updated = false;
        if (newName != null && !newName.isEmpty()) {
            student.setName(newName);
            updated = true;
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            student.setPassword(newPassword);
            updated = true;
        }
        if (updated) {
            fileManager.saveStudents(students);
        }
        return updated;
    }

    public boolean toggleStudentStatus(String username) {
        Student student = findStudentByUsername(username);
        if (student != null) {
            student.setActive(!student.isActive());
            fileManager.saveStudents(students);
            return true;
        }
        return false;
    }

    public Student findStudentByUsername(String username) {
        return students.stream()
                .filter(s -> s.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private boolean isUsernameTaken(String username) {
        return students.stream().anyMatch(s -> s.getUsername().equals(username));
    }

    public int getStudentCount() {
        return students.size();
    }
}

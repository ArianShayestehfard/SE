package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudentFileManager {
    private static final String FILE_NAME = "students.dat";

    public void saveStudents(List<Student> students) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.out.println("Error saving students data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Student> loadStudents() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof List) {
                return (List<Student>) loadedObject;
            } else {
                System.out.println("Invalid data format in file. Starting with empty list.");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading students data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
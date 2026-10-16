package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeFileManager {
    private static final String FILE_NAME = "employees.dat";

    public void saveEmployees(List<Employee> employees) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(employees);
        } catch (IOException e) {
            System.out.println("Error saving employees data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Employee> loadEmployees() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof List) {
                return (List<Employee>) loadedObject;
            } else {
                System.out.println("Invalid data  format in employees file.");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading employees data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
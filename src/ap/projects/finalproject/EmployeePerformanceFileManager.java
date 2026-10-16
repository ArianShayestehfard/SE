package ap.projects.finalproject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EmployeePerformanceFileManager {
    private static final String FILE_NAME = "employee_performance.dat";

    public void savePerformanceRecords(Map<String, EmployeePerformance> performanceRecords) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(performanceRecords);
        } catch (IOException e) {
            System.out.println("Error saving employee performance data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, EmployeePerformance> loadPerformanceRecords() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof Map) {
                return (Map<String, EmployeePerformance>) loadedObject;
            } else {
                System.out.println("Invalid data format in employee performance file.");
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading employee performance data: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
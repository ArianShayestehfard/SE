package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowFileManager {
    private static final String FILE_NAME = "borrow_records.dat";

    public void saveBorrowRecords(List<BorrowRecord> records) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.out.println("Error saving borrow records: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<BorrowRecord> loadBorrowRecords() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof List) {
                return (List<BorrowRecord>) loadedObject;
            } else {
                System.out.println("Invalid data format in borrow records file.");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading borrow records: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
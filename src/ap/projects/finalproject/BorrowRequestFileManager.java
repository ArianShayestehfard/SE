package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRequestFileManager {
    private static final String FILE_NAME = "borrow_requests.dat";

    public void saveBorrowRequests(List<BorrowRequest> requests) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(requests);
        } catch (IOException e) {
            System.out.println("Error saving borrow requests: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<BorrowRequest> loadBorrowRequests() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof List) {
                return (List<BorrowRequest>) loadedObject;
            } else {
                System.out.println("Invalid data format in borrow requests file.");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading borrow requests: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
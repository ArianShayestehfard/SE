package ap.projects.finalproject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BookFileManager {
    private static final String FILE_NAME = "books.dat";

    public void saveBooks(List<Book> books) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(books);
        } catch (IOException e) {
            System.out.println("Error saving books data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Book> loadBooks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object loadedObject = ois.readObject();
            if (loadedObject instanceof List) {
                return (List<Book>) loadedObject;
            } else {
                System.out.println("Invalid data format in file.");
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading books data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}

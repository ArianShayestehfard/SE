package ap.projects.finalproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BookManager {
    private List<Book> books;
    private BookFileManager fileManager;

    public BookManager() {
        this.fileManager = new BookFileManager();
        this.books = fileManager.loadBooks();
        if (this.books == null) {
            this.books = new ArrayList<>();
        }
    }

    public List<Book> searchBooks(String title, String author, Integer year, String isbn) {
        return books.stream()
                .filter(book ->
                        (title == null || title.isEmpty() || book.matchesTitleSearch(title)) &&
                                (author == null || author.isEmpty() || book.matchesAuthorSearch(author)) &&
                                (year == null || book.matchesPublicationYear(year)) &&
                                (isbn == null || isbn.isEmpty() || book.matchesIsbnSearch(isbn)))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return new ArrayList<>();
        }
        return searchBooks(title, null, null, null);
    }

    public boolean editBook(String isbn, String newTitle, String newAuthor, Integer newYear) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            return false;
        }
        if (newTitle != null && !newTitle.isEmpty()) {
            book.setTitle(newTitle);
        }
        if (newAuthor != null && !newAuthor.isEmpty()) {
            book.setAuthor(newAuthor);
        }
        if (newYear != null && newYear > 0) {
            book.setPublicationYear(newYear);
        }
        saveBooks();
        return true;
    }

    public boolean editBookFromInput(Scanner scanner, String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Book with ISBN " + isbn + " not found.");
            return false;
        }

        System.out.println("\nCurrent book information:");
        System.out.println(book.getFullBookInfo());
        System.out.println("\nEnter new information (press Enter to keep current value):");

        System.out.print("New title (current: " + book.getTitle() + "): ");
        String newTitle = scanner.nextLine();

        System.out.print("New author (current: " + book.getAuthor() + "): ");
        String newAuthor = scanner.nextLine();

        System.out.print("New publication year (current: " + book.getPublicationYear() + "): ");
        String yearInput = scanner.nextLine();
        Integer newYear = null;

        if (!yearInput.isEmpty()) {
            try {
                newYear = Integer.parseInt(yearInput);
                if (newYear < 1000 || newYear > 2100) {
                    System.out.println("Invalid year. Must be between 1000 and 2100.");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year format.");
                return false;
            }
        }

        if (editBook(isbn, newTitle, newAuthor, newYear)) {
            System.out.println("Book information updated successfully!");
            System.out.println(findBookByIsbn(isbn).getFullBookInfo());
            return true;
        }
        System.out.println("Failed to update book information.");
        return false;
    }

    public void searchBooksFromInput(Scanner scanner) {
        System.out.println("\n--- Advanced Book Search ---");
        String title = readOptionalLine(scanner, "Title (press Enter to skip): ");
        String author = readOptionalLine(scanner, "Author (press Enter to skip): ");
        Integer year = readOptionalYear(scanner);
        String isbn = readOptionalLine(scanner, "ISBN (press Enter to skip): ");

        displaySearchResults(searchBooks(title, author, year, isbn), true);
    }

    public void searchBooksForStudent(Scanner scanner) {
        System.out.println("\n--- Book Search ---");
        String title = readOptionalLine(scanner, "Title (press Enter to skip): ");
        String author = readOptionalLine(scanner, "Author (press Enter to skip): ");
        Integer year = readOptionalYear(scanner);

        displaySearchResults(searchBooks(title, author, year, null), true);
    }

    public void addNewBook(Book book) {
        if (isIsbnTaken(book.getIsbn())) {
            System.out.println("A book with this ISBN already exists.");
            return;
        }

        books.add(book);
        saveBooks();
        System.out.println("Book added successfully!");
    }

    private boolean isIsbnTaken(String isbn) {
        return books.stream().anyMatch(book -> book.getIsbn().equals(isbn));
    }

    public Book createBookFromInput(Scanner scanner) {
        System.out.println("\n--- Add New Book ---");

        System.out.print("Book title: ");
        String title = scanner.nextLine();

        System.out.print("Author: ");
        String author = scanner.nextLine();

        System.out.print("Publication year: ");
        int year = getIntInput(scanner, 1000, 2100);

        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();

        if (isbn.isEmpty() || isbn.length() < 10) {
            System.out.println("Invalid ISBN. Please enter a valid ISBN (minimum 10 characters).");
            return null;
        }

        return new Book(title, author, year, isbn);
    }

    private String readOptionalLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private Integer readOptionalYear(Scanner scanner) {
        System.out.print("Publication year (press Enter to skip): ");
        String input = scanner.nextLine();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year format, ignoring the year filter.");
            return null;
        }
    }

    private int getIntInput(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    public int getTotalBooksCount() {
        return books.size();
    }

    public Book findBookByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }

    public void displayAllBooks() {
        System.out.println("\n--- All Books in Library ---");
        if (books.isEmpty()) {
            System.out.println("No books available in the library.");
            return;
        }

        books.forEach(book -> System.out.println(book.getFullBookInfo()));
    }

    public void displayAvailableBooks() {
        System.out.println("\n--- Available Books ---");
        List<Book> availableBooks = books.stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());

        if (availableBooks.isEmpty()) {
            System.out.println("No books available at the moment.");
            return;
        }

        availableBooks.forEach(book -> System.out.println(book.getBookInfo()));
    }

    public void displaySearchResults(List<Book> results, boolean includeStatus) {
        if (results.isEmpty()) {
            System.out.println("No books found matching your criteria.");
            return;
        }

        System.out.println("\n--- Search Results ---");
        results.forEach(book ->
                System.out.println(includeStatus ? book.getFullBookInfo() : book.getBookInfo()));
        System.out.println("Found " + results.size() + " book(s)");
    }

    public boolean borrowBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book != null && book.isAvailable()) {
            book.setAvailable(false);
            saveBooks();
            return true;
        }
        return false;
    }

    public boolean returnBook(String isbn) {
        Book book = findBookByIsbn(isbn);
        if (book != null && !book.isAvailable()) {
            book.setAvailable(true);
            saveBooks();
            return true;
        }
        return false;
    }

    private void saveBooks() {
        fileManager.saveBooks(books);
    }
}

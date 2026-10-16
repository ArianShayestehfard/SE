package ap.projects.finalproject;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 2L;
    private String title;
    private String author;
    private int publicationYear;
    private String isbn;
    private boolean isAvailable;

    public Book(String title, String author, int publicationYear, String isbn) {
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.isAvailable = true;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPublicationYear() { return publicationYear; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getBookInfo() {
        return "Title: " + title +
                " | Author: " + author +
                " | Year: " + publicationYear +
                " | ISBN: " + isbn;
    }

    public String getFullBookInfo() {
        String status = isAvailable ? "Available" : "Borrowed";
        return "Title: " + title +
                " | Author: " + author +
                " | Year: " + publicationYear +
                " | ISBN: " + isbn +
                " | Status: " + status;
    }

    @Override
    public String toString() {
        return getFullBookInfo();
    }

    public boolean matchesTitleSearch(String searchTitle) {
        if (searchTitle == null || searchTitle.isEmpty()) {
            return false;
        }
        return this.title.toLowerCase().contains(searchTitle.toLowerCase());
    }

    public boolean matchesIsbnSearch(String searchIsbn) {
        if (searchIsbn == null || searchIsbn.isEmpty()) {
            return false;
        }
        return this.isbn.toLowerCase().contains(searchIsbn.toLowerCase());
    }

    public boolean matchesAuthorSearch(String searchAuthor) {
        if (searchAuthor == null || searchAuthor.isEmpty()) {
            return false;
        }
        return this.author.toLowerCase().contains(searchAuthor.toLowerCase());
    }

    public boolean matchesPublicationYear(int year) {
        return this.publicationYear == year;
    }
}

import java.sql.*;
import java.util.Scanner;

public class Library {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            while (true) {
                System.out.println("\nLibrary Management System:");
                System.out.println("1. Add Book");
                System.out.println("2. Add Member");
                System.out.println("3. Issue Book");
                System.out.println("4. Return Book");
                System.out.println("5. View Books");
                System.out.println("6. View Members");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume the newline character

                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        addMember();
                        break;
                    case 3:
                        issueBook();
                        break;
                    case 4:
                        returnBook();
                        break;
                    case 5:
                        viewBooks();
                        break;
                    case 6:
                        viewMembers();
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void addBook() {
        System.out.println("\nEnter Book Details:");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO books (title, author, isbn, quantity) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, isbn);
                stmt.setInt(4, quantity);
                stmt.executeUpdate();
                System.out.println("Book added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to add a member
    public static void addMember() {
        System.out.println("\nEnter Member Details:");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Membership ID: ");
        String membershipId = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO members (name, email, membership_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, membershipId);
                stmt.executeUpdate();
                System.out.println("Member added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to issue a book
    public static void issueBook() {
        System.out.println("\nEnter Book ISBN: ");
        String isbn = scanner.nextLine();
        System.out.println("Enter Member ID: ");
        String membershipId = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            
            String bookQuery = "SELECT * FROM books WHERE isbn = ? AND quantity > 0";
            try (PreparedStatement bookStmt = conn.prepareStatement(bookQuery)) {
                bookStmt.setString(1, isbn);
                ResultSet bookResult = bookStmt.executeQuery();
                if (!bookResult.next()) {
                    System.out.println("Book not available or invalid ISBN.");
                    return;
                }

                int bookId = bookResult.getInt("book_id");

                // Check if member exists
                String memberQuery = "SELECT * FROM members WHERE membership_id = ?";
                try (PreparedStatement memberStmt = conn.prepareStatement(memberQuery)) {
                    memberStmt.setString(1, membershipId);
                    ResultSet memberResult = memberStmt.executeQuery();
                    if (!memberResult.next()) {
                        System.out.println("Invalid member ID.");
                        return;
                    }

                    int memberId = memberResult.getInt("member_id");

                    // Issue the book
                    String issueQuery = "INSERT INTO issued_books (book_id, member_id, issue_date, return_date) VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY))";
                    try (PreparedStatement issueStmt = conn.prepareStatement(issueQuery)) {
                        issueStmt.setInt(1, bookId);
                        issueStmt.setInt(2, memberId);
                        issueStmt.executeUpdate();
                    }

                    // Decrease book quantity
                    String updateBookQuery = "UPDATE books SET quantity = quantity - 1 WHERE book_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery)) {
                        updateStmt.setInt(1, bookId);
                        updateStmt.executeUpdate();
                    }

                    System.out.println("Book issued successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to return a book
    public static void returnBook() {
        System.out.println("\nEnter Book ISBN: ");
        String isbn = scanner.nextLine();
        System.out.println("Enter Member ID: ");
        String membershipId = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            // Get issued book details
            String query = "SELECT issued_books.issue_id, issued_books.book_id, books.title " +
                           "FROM issued_books " +
                           "JOIN books ON issued_books.book_id = books.book_id " +
                           "JOIN members ON issued_books.member_id = members.member_id " +
                           "WHERE books.isbn = ? AND members.membership_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, isbn);
                stmt.setString(2, membershipId);
                ResultSet result = stmt.executeQuery();
                if (!result.next()) {
                    System.out.println("No issued book found for the given ISBN and Member ID.");
                    return;
                }

                int issueId = result.getInt("issue_id");
                int bookId = result.getInt("book_id");

                // Delete the issued book record
                String deleteIssueQuery = "DELETE FROM issued_books WHERE issue_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteIssueQuery)) {
                    deleteStmt.setInt(1, issueId);
                    deleteStmt.executeUpdate();
                }

                // Increase book quantity
                String updateBookQuery = "UPDATE books SET quantity = quantity + 1 WHERE book_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery)) {
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();
                }

                System.out.println("Book returned successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to view books
    public static void viewBooks() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM books";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                System.out.println("\nAvailable Books:");
                while (rs.next()) {
                    System.out.printf("ID: %d, Title: %s, Author: %s, ISBN: %s, Quantity: %d\n",
                            rs.getInt("book_id"), rs.getString("title"), rs.getString("author"), rs.getString("isbn"), rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to view members
    public static void viewMembers() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM members";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                System.out.println("\nRegistered Members:");
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %s, Email: %s, Membership ID: %s\n",
                            rs.getInt("member_id"), rs.getString("name"), rs.getString("email"), rs.getString("membership_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


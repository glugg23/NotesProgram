import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static Connection connect() {
        String url = "jdbc:sqlite:db/notes.db";
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }

    private static void initialSetup() {
        try(Connection connection = connect()) {
            /*
            Database gets created if it does not already exist.
            So this query just creates the tables we need if the database is being made from scratch.
             */
            String query = "CREATE TABLE IF NOT EXISTS notes (\n"
                    + " id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT,\n"
                    + " title TEXT NOT NULL,\n"
                    + " content BLOB\n"
                    + ");";

            connection.createStatement().execute(query);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void enterNote() {
        Scanner in = new Scanner(System.in);

        System.out.print("Enter note title: ");
        String title = in.nextLine();
        Note note = new Note(title);

        System.out.print("Enter note content: ");
        String content = in.nextLine();
        note.setNote(content);

        note.upload();
    }

    private static void showAllNotes() {
        Connection connection = connect();

        String query = "SELECT * FROM notes";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while(rs.next()) {
                System.out.println("title: " + rs.getString("title") + "\n" +
                        "content:\n" + rs.getString("content"));
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        initialSetup();

        Scanner in = new Scanner(System.in);
        int result;

        do {
            System.out.println("Notes Program V0.1\n" +
                    "Menu\n" +
                    "\t1 - Enter note\n" +
                    "\t2 - Show all notes\n" +
                    "\t0 - Exit\n");

            System.out.print("-> ");

            try {
                result = in.nextInt();

            } catch(InputMismatchException e) {
                System.out.println("Invalid option");
                result = -1;
            }

            switch(result) {
                case 1:
                    enterNote();
                    break;
                case 2:
                    showAllNotes();
                    break;
                case 0:
                    System.out.println("Goodbye");
                    break;
                default:
                    in.nextLine();
                    break;
            }

        } while(result != 0);

        in.close();
    }
}
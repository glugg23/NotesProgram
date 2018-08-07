import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static Connection connect() {
        String url = "jdbc:sqlite:db/notes.db";
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);

            /*
            Database gets created if it does not already exist.
            So this query just creates the tables we need if the database is being made from scratch.
             */
            String query = "CREATE TABLE IF NOT EXISTS notes (\n"
                    + " id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT,\n"
                    + " title TEXT NOT NULL,\n"
                    + " content BLOB\n"
                    + ");";

            Statement statement = connection.createStatement();
            statement.execute(query);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) {
        Connection connection = connect();
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
                    System.out.println(1);
                    break;
                case 2:
                    System.out.println(2);
                    break;
                case 0:
                    System.out.println("Goodbye");
                    break;
                default:
                    in.nextLine();
                    break;
            }

        } while(result != 0);
    }
}
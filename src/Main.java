import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

    }
}
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

public class SQL {
    /**
     * Creates a connect to the database
     * @return A valid connect to the database
     */
    public static Connection connect() {
        String url = "jdbc:sqlite:db/notes.db";
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return connection;
    }

    /**
     * Sets up the tables needed if they do not exist
     */
    public static void initialSetup() {
        try(Connection connection = connect()) {
            /*
            Database gets created if it does not already exist.
            So this query just creates the tables we need if the database is being made from scratch.
             */
            String createNotesTable = "CREATE TABLE IF NOT EXISTS notes (\n"
                    + " id INTEGER PRIMARY KEY,\n"
                    + " title TEXT NOT NULL,\n"
                    + " content BLOB\n"
                    + ");";

            String createKeysTable = "CREATE TABLE IF NOT EXISTS keys(\n"
                    + " id INTEGER PRIMARY KEY,\n"
                    + " key BLOB NOT NULL,\n"
                    + " size INTEGER DEFAULT 128,\n"
                    + " creation_date DATETIME NOT NULL,\n"
                    + " expiry_date TIMESTAMP DEFAULT null\n"
                    + ");";

            String createNotesIndex = "CREATE INDEX IF NOT EXISTS notes_title_index ON notes(title);";
            String createKeysIndex = "CREATE UNIQUE INDEX IF NOT EXISTS keys_id_uindex ON keys(id);";

            Statement statement = connection.createStatement();
            statement.addBatch(createNotesTable);
            statement.addBatch(createKeysTable);
            statement.addBatch(createNotesIndex);
            statement.addBatch(createKeysIndex);
            statement.executeBatch();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Prints out all saved notes, shortens any long notes to just show the first 40 characters
     */
    public static void showAllNotes() {
        Connection connection = connect();

        String notesQuery = "SELECT * FROM notes";
        String countQuery = "SELECT COUNT(*) FROM notes";

        try {
            Statement statementNotes = connection.createStatement();
            Statement statementCount = connection.createStatement();
            ResultSet notes = statementNotes.executeQuery(notesQuery);
            ResultSet countRS = statementCount.executeQuery(countQuery);

            countRS.next();
            int count = countRS.getInt(1);

            System.out.println("Number of notes: " + count);

            while(notes.next()) {
                String content = notes.getString("content");
                if(content.length() > 41) {
                    content = content.substring(0, 37) + "...\n";
                }

                System.out.println("title: " + notes.getString("title") + "\n" +
                        "content:" + content);
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());

        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }

            } catch(SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Saves note to a file
     * TODO Make this handle notes with the same title
     */
    public static void saveNote() {
        Scanner in = new Scanner(System.in);

        System.out.print("Enter note title: ");
        String title = in.nextLine();
        Note note = new Note(title);

        String query = "SELECT * FROM notes WHERE title=?";
        Connection connection = connect();

        try {
            PreparedStatement pstmnt = connection.prepareStatement(query);
            pstmnt.setString(1, note.getTitle());
            ResultSet rs = pstmnt.executeQuery();

            while(rs.next()) {
                note.setNote(rs.getString("content"));
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        String fileUrl = String.format("./db/%s", note.getTitle());

        Path path = Paths.get(fileUrl);

        try {
            Files.createFile(path);
            Files.write(path, note.getNote().getBytes());

        } catch(IOException e) {
            System.out.println("ERROR: File already exists");
        }
    }

    /**
     * Reads in a file and saves it in the database
     */
    public static void uploadFile() {
        Scanner in = new Scanner(System.in);
        String path;
        System.out.print("Enter file name: ");
        path = in.nextLine();

        String content;

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            content = new String(bytes);

            Note note = new Note(path, content);
            note.upload();

        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
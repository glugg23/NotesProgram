import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
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
                    + " id INTEGER PRIMARY KEY,\n"
                    + " title TEXT NOT NULL,\n"
                    + " content BLOB\n"
                    + ");\n"
                    + "CREATE INDEX notes_title_index ON notes(title);";

            connection.createStatement().execute(query);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
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

    private static void saveNote() {
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

    private static void uploadFile() {
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

    public static void main(String[] args) {
        initialSetup();

        Scanner in = new Scanner(System.in);
        int mainMenuChoice;

        do {
            System.out.println("Notes Program V0.1\n" +
                    "Menu\n" +
                    "\t1 - Enter note\n" +
                    "\t2 - Show all notes\n" +
                    "\t3 - Save note to file\n" +
                    "\t4 - Upload file\n" +
                    "\t5 - Encryption menu\n" +
                    "\t0 - Exit\n");

            System.out.print("-> ");

            try {
                mainMenuChoice = in.nextInt();

            } catch(InputMismatchException e) {
                System.out.println("Invalid option");
                mainMenuChoice = -1;
            }

            switch(mainMenuChoice) {
                case 1:
                    TextEditor ed = new TextEditor();
                    ed.use();
                    break;
                case 2:
                    showAllNotes();
                    break;
                case 3:
                    saveNote();
                    break;
                case 4:
                    uploadFile();
                    break;
                case 5:
                    int encryptionMenuChoice;
                    do {
                        System.out.println("Encryption Menu\n" +
                                "\t1 - Generate key\n" +
                                "\t0 - Exit\n");

                        System.out.print("-> ");

                        try {
                            encryptionMenuChoice = in.nextInt();

                        } catch(InputMismatchException e) {
                            System.out.println("Invalid option");
                            encryptionMenuChoice = -1;
                        }

                        switch(encryptionMenuChoice) {
                            case 1:
                                try {
                                    byte[] key = Encryption.generateKey();

                                    String query = "INSERT INTO keys(key, creation_date) VALUES(?, ?)";
                                    Connection connection = connect();
                                    PreparedStatement pstmnt = connection.prepareStatement(query);
                                    pstmnt.setString(1, new String(key));
                                    pstmnt.setTimestamp(2, Timestamp.from(Instant.now()));
                                    pstmnt.execute();

                                } catch(Exception e) {
                                    System.out.println(e.getMessage());
                                }
                                break;
                            case 0:
                                break;
                            default:
                                in.nextLine();
                                break;
                        }

                    } while(encryptionMenuChoice != 0);

                    break;
                case 0:
                    System.out.println("Goodbye");
                    break;
                default:
                    in.nextLine();
                    break;
            }

        } while(mainMenuChoice != 0);

        in.close();
    }
}
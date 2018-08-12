import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Note {
    private String title;
    //Might make this an ArrayList of Strings
    private String note = "";

    /**
     * Allows creation of note with only a title
     *
     * @param title The title of this note
     */
    public Note(String title) {
        this.title = title;
    }

    public Note(String title, String note) {
        this.title = title;
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Allows more text to be added to a note
     *
     * @param note Message to be added to note
     */
    public void appendNote(String note) {
        this.note += note;
    }

    /**
     *
     * @return connect A valid connect to the database
     */
    private Connection connect() {
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
     * Uploads Note object to database
     */
    public void upload() {
        String query = "INSERT INTO notes(title, content) VALUES (?, ?)";

        try {
            Connection connection = this.connect();
            PreparedStatement pstmnt = connection.prepareStatement(query);

            pstmnt.setString(1, this.title);
            pstmnt.setString(2, this.note);

            pstmnt.execute();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks equality through matching titles
     *
     * @param o Any object
     * @return boolean Stating equality
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        return title.equals(note.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
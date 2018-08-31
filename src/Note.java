import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Note {
    private String title;
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
     * Uploads Note object to database
     */
    public void upload() {
        String query = "INSERT INTO notes(title, content) VALUES (?, ?)";

        try {
            Connection connection = SQL.connect();
            PreparedStatement pstmnt = connection.prepareStatement(query);

            pstmnt.setString(1, this.title);
            pstmnt.setString(2, this.note);

            pstmnt.execute();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a note that exists in the database with this note
     */
    public void update() {
        String query = "UPDATE notes SET content=? WHERE title=?";

        try {
            Connection connection = SQL.connect();
            PreparedStatement pstmnt = connection.prepareStatement(query);
            pstmnt.setString(1, this.note);
            pstmnt.setString(2, this.title);

            pstmnt.execute();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Note note1 = (Note) o;

        if(!title.equals(note1.title)) return false;
        return note.equals(note1.note);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + note.hashCode();
        return result;
    }
}
/*
    NotesProgram - A simple notes management system
    Copyright (C) 2018 Max Leonhardt

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Note {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
            LOGGER.log(Level.WARNING, e.toString(), e);
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
            LOGGER.log(Level.WARNING, e.toString(), e);
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
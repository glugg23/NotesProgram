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

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Base64;
import java.util.Optional;

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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();

        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }

            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves note to a file, if there are multiple notes with the same title it saves them all
     */
    public static void saveNote(String title) {
        String notesQuery = "SELECT * FROM notes WHERE title=?";
        String countQuery = "SELECT COUNT(*) FROM notes WHERE title=?";
        Connection connection = connect();

        try {
            PreparedStatement notesStatement = connection.prepareStatement(notesQuery);
            notesStatement.setString(1, title);

            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            countStatement.setString(1, title);

            ResultSet notes = notesStatement.executeQuery();
            ResultSet countRS = countStatement.executeQuery();

            countRS.next();
            int count = countRS.getInt(1);

            if(count == 1) {
                notes.next();
                String url = String.format("./db/%s", title);
                Path path = Paths.get(url);

                try {
                    Files.createFile(path);
                    Files.write(path, notes.getString("content").getBytes());

                } catch(IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("There were " + count + " number of notes with this name");

                //For loop going through all of notes and incrementing i each time
                for(int i = 1; notes.next(); ++i) {
                    String url = String.format("./db/%s (%d)", title, i);
                    Path path = Paths.get(url);

                    try {
                        Files.createFile(path);
                        Files.write(path, notes.getString("content").getBytes());

                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch(SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }

            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads in a file and saves it in the database
     */
    public static void uploadFile(String filename) {
        String content;

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            content = new String(bytes);

            Note note = new Note(filename, content);
            note.upload();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Pair<byte[], Note>> getNoteAndKeyPair(String title) {
        String noteQuery = "SELECT * FROM notes WHERE title=?";
        String keyQuery = "SELECT * FROM keys";
        Connection connection = SQL.connect();
        Note note;
        byte[] key;

        try {
            PreparedStatement notesStatement = connection.prepareStatement(noteQuery);
            notesStatement.setString(1, title);
            ResultSet notes = notesStatement.executeQuery();

            Statement keyStatement = connection.createStatement();
            ResultSet keys = keyStatement.executeQuery(keyQuery);

            //Get first note
            notes.next();
            note = new Note(notes.getString("title"), notes.getString("content"));

            //Get first key
            keys.next();
            String base64 = keys.getString("key");
            key = Base64.getDecoder().decode(base64.getBytes());

            Pair<byte[], Note> pair = new Pair<>(key, note);
            return Optional.of(pair);

        } catch(SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }

            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
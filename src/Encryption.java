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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encryption {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Generates a new key for encryption
     * @return A Base64 encoded 128bit AES key, or Nothing if something went wrong
     */
    //TODO Add way to choose key size and expiry date
    public static Optional<byte[]> generateKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            byte[] key = keygen.generateKey().getEncoded();
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            byte[] encodedBytes = Base64.getEncoder().encode(secretKey.getEncoded());

            LOGGER.log(Level.FINEST, "New key generated");

            return Optional.of(encodedBytes);

        } catch(NoSuchAlgorithmException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Inserts a key into the database
     * @param key The key that needs to be inserted
     */
    public static void insertKey(byte[] key) {
        String query = "INSERT INTO keys(key, creation_date) VALUES(?, ?)";
        Connection connection = SQL.connect();
        try {
            PreparedStatement pstmnt = connection.prepareStatement(query);
            pstmnt.setString(1, new String(key));
            pstmnt.setTimestamp(2, Timestamp.from(Instant.now()));
            pstmnt.execute();

            LOGGER.log(Level.FINEST, "New key added to database");

        } catch(SQLException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
    }

    public static void encryptFile(String filename) {
        //Binary content of file
        byte[] fileText;

        try {
            fileText = Files.readAllBytes(Paths.get(filename));

        } catch(IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
            return;
        }

        byte[] key;
        String query = "SELECT * FROM keys";
        Connection connection = SQL.connect();

        try {
            Statement statement = connection.createStatement();
            //TODO Change this later to allow the user to pick a key they want to use
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            key = Base64.getDecoder().decode(rs.getString("key").getBytes());

            rs.close();
            connection.close();

        } catch(SQLException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
            return;
        }

        SecretKey secretKey = new SecretKeySpec(key, "AES");

        //Generating random initialization vector
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);
        GCMParameterSpec gcmPS = new GCMParameterSpec(128, iv);

        try {
            //Encrypts the message
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, secretKey, gcmPS, new SecureRandom());
            byte[] encryptedText = cipher.doFinal(fileText);

            //Saves the iv length, the iv itself and the encrypted message in a binary form
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedText);

            //Finally it is encoded in base 64 to make storing easier
            byte[] finalMessage = Base64.getEncoder().encode(byteBuffer.array());

            Note note = new Note(filename, new String(finalMessage));
            note.upload();

        } catch(Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
    }

    public static void decryptAndSaveNote(String title) {
        Pair<byte[], Note> keyNotePair;

        Optional<Pair<byte[], Note>> optionalPair = SQL.getNoteAndKeyPair(title);

        if(optionalPair.isPresent()) {
            keyNotePair = new Pair<>(optionalPair.get().getKey(), optionalPair.get().getValue());

        } else {
            //TODO Handle failure to find key
            LOGGER.log(Level.SEVERE, "Failure to find encryption key");
            return;
        }

        byte[] decodedMessage = Base64.getDecoder().decode(keyNotePair.getValue().getNote().getBytes());

        //Gets message and checks if it is valid
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedMessage);
        int ivLength = byteBuffer.getInt();
        if(ivLength < 12 || ivLength >= 16) {
            throw new IllegalArgumentException("Invalid iv length");
        }
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] encryptedMessage = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedMessage);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            cipher.init(cipher.DECRYPT_MODE, new SecretKeySpec(keyNotePair.getKey(), "AES"), new GCMParameterSpec(128, iv));
            byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
            keyNotePair.getValue().setNote(new String(decryptedMessage));

        } catch(Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }

        try {
            Files.createFile(Paths.get(title));
            Files.write(Paths.get(title), keyNotePair.getValue().getNote().getBytes());

        } catch(IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
    }

    public static void encryptNote(String title) {
        Pair<byte[], Note> keyNotePair;

        Optional<Pair<byte[], Note>> optionalPair = SQL.getNoteAndKeyPair(title);

        if(optionalPair.isPresent()) {
            keyNotePair = new Pair<>(optionalPair.get().getKey(), optionalPair.get().getValue());

        } else {
            //TODO Handle failure to find key
            LOGGER.log(Level.SEVERE, "Failure to find encryption key");
            return;
        }

        //Generating random initialization vector
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, new SecretKeySpec(keyNotePair.getKey(), "AES"), new GCMParameterSpec(128, iv));
            byte[] encryptedText = cipher.doFinal(keyNotePair.getValue().getNote().getBytes());

            //Saves the iv length, the iv itself and the encrypted message in a binary form
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + encryptedText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedText);
            byte[] finalMessage = Base64.getEncoder().encode(byteBuffer.array());

            keyNotePair.getValue().setNote(new String(finalMessage));
            keyNotePair.getValue().update();

        } catch(Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
    }

    public static void decryptNote(String title) {
        Pair<byte[], Note> keyNotePair;

        Optional<Pair<byte[], Note>> optionalPair = SQL.getNoteAndKeyPair(title);

        if(optionalPair.isPresent()) {
            keyNotePair = new Pair<>(optionalPair.get().getKey(), optionalPair.get().getValue());

        } else {
            //TODO Handle failure to find key
            LOGGER.log(Level.SEVERE, "Failure to find encryption key");
            return;
        }

        byte[] decodedMessage = Base64.getDecoder().decode(keyNotePair.getValue().getNote().getBytes());

        //Gets message and checks if it is valid
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedMessage);
        int ivLength = byteBuffer.getInt();
        if(ivLength < 12 || ivLength >= 16) {
            throw new IllegalArgumentException("Invalid iv length");
        }
        byte[] iv = new byte[ivLength];
        byteBuffer.get(iv);
        byte[] encryptedMessage = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedMessage);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            cipher.init(cipher.DECRYPT_MODE, new SecretKeySpec(keyNotePair.getKey(), "AES"), new GCMParameterSpec(128, iv));
            byte[] decryptedMessage = cipher.doFinal(encryptedMessage);

            keyNotePair.getValue().setNote(new String(decryptedMessage));
            keyNotePair.getValue().update();

        } catch(Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            e.printStackTrace();
        }
    }
}
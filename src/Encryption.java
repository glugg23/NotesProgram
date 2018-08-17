import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;

//import java.nio.file.Path;

public class Encryption {
    /**
     * Generates a new key for encryption
     * @return A Base64 encoded 128bit AES key
     * @throws Exception NoSuchAlgorithmException: Which should never throw
     */
    //TODO Add way to choose key size and expiry date
    public static byte[] generateKey() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        byte[] encodedBytes = Base64.getEncoder().encode(secretKey.getEncoded());
        //System.out.println(new String(encodedBytes));
        return encodedBytes;
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

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void encryptFile(String filename) {
        //Binary content of file
        byte[] fileText;

        try {
            fileText = Files.readAllBytes(Paths.get(filename));

        } catch(IOException e) {
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
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

            /*try {
                String newFilename = String.format("%s.enc", filename);
                Path path = Paths.get(newFilename);
                Files.createFile(path);
                Files.write(path, finalMessage);

            } catch(IOException e) {
                System.out.println(e.getMessage());
            }*/

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
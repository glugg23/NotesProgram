import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

public class Encryption {
    /**
     * Generates a new key for encryption
     * @return A Base64 encoded 128bit AES key
     * @throws Exception NoSuchAlgorithmException: Which should never throw
     */
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
}
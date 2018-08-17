import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Encryption {
    public static byte[] generateKey() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        byte[] encodedBytes = Base64.getEncoder().encode(secretKey.getEncoded());
        //System.out.println(new String(encodedBytes));
        return encodedBytes;
    }
}
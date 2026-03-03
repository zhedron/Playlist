package zhedron.playlist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class AESEncryptionService {
    @Value("${secret_key.aes}")
    private String KEY;
    private final String IV = "1234567890";

    public String encrypt(String phone) {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, IV.getBytes()));

            byte[] encoder = cipher.doFinal(phone.getBytes());

            return Base64.getEncoder().encodeToString(encoder);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException("Error encrypt AES: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error encrypt GCM: " + e);
        }
    }

    public String decrypt(String phone) {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");

        byte[] decodedBytes = Base64.getDecoder().decode(phone);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, IV.getBytes()));

            byte[] decoder = cipher.doFinal(decodedBytes);

            return new String(decoder);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Error decrypt AES: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error decrypt GCM generateIv: " + e);
        }
    }
}

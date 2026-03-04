package zhedron.playlist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AESEncryptionService {
    @Value("${secret_key.aes}")
    private String KEY;

    private byte[] generateIV() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public String encrypt(String phone) {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

        try {
            byte[] iv = generateIV();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            byte[] encoder = cipher.doFinal(phone.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encoder.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encoder, 0, combined, iv.length, encoder.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException("Error encrypt AES: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error encrypt GCM: " + e);
        }
    }

    public String decrypt(String phone) {
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

        byte[] decodedBytes = Base64.getDecoder().decode(phone);

        byte[] iv = new byte[12];
        byte[] encryptedBytes = new byte[decodedBytes.length - 12];

        System.arraycopy(decodedBytes, 0, iv, 0, iv.length);
        System.arraycopy(decodedBytes, 12, encryptedBytes, 0, encryptedBytes.length);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            byte[] decoder = cipher.doFinal(encryptedBytes);

            return new String(decoder, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException("Error decrypt AES: " + e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error decrypt GCM generateIv: " + e);
        }
    }
}

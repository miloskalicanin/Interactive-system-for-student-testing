/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encryptor {

    static Logger logger = LoggerFactory.getLogger(Encryptor.class);

    private static final String KEYSTORE_PATH = "encription_store";
    private static final String KEYSTORE_PASSWORD = "HS7et>'vD!!uyuta";
    private static final String STORAGE_KEY_ALIAS = "encription-key";
    private static final String ALGORITHM = "AES";
    private static final String STORAGE_CIPHER = "AES/CBC/PKCS5PADDING";

    private static final String CREDENTIALS_FILE = "saved_data";

    private static KeyStore encryptionStore = null;

    private static SecretKey createKeyEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256, new SecureRandom());
        return keyGenerator.generateKey();
    }

    private static Cipher retrieveKeyEncryptionCipher() throws Exception {
        KeyStore keyStore = loadKeyStore();

        SecretKey secretKey = (SecretKey) keyStore.getKey(STORAGE_KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());

        if (secretKey == null) {
            // The key was not found
            // Create a new key
            secretKey = createKeyEncryptionKey();

            //keyStore.setKeyEntry(STORAGE_KEY_ALIAS, secretKey, KEYSTORE_PASSWORD.toCharArray(), null);
            KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(STORAGE_KEY_ALIAS, skEntry, new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()));

            storeKeyStore(keyStore);
        }

        try {
            Cipher cipher = Cipher.getInstance(STORAGE_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        } catch (Exception e) {
            // This happens if the lock screen has been disabled or reset after the key was
            // generated.
            // Create a new key
            secretKey = createKeyEncryptionKey();
            KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(STORAGE_KEY_ALIAS, skEntry, new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()));

            storeKeyStore(keyStore);
            Cipher cipher = Cipher.getInstance(STORAGE_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        }
    }

    private static Cipher retrieveKeyDecryptionCipher(IvParameterSpec ivParams) throws Exception {
        KeyStore keyStore = loadKeyStore();
        SecretKey secretKey = (SecretKey) keyStore.getKey(STORAGE_KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());

        if (secretKey == null) {
            // The key was not found
            return null;
        }
        Cipher cipher = Cipher.getInstance(STORAGE_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        return cipher;
    }

    private static KeyStore loadKeyStore() throws Exception {
        if (encryptionStore != null) {
            return encryptionStore;
        }

        KeyStore keyStoreTmp = KeyStore.getInstance("JCEKS");

        File f = new File(KEYSTORE_PATH);
        try (FileInputStream fis = new FileInputStream(f)) {
            keyStoreTmp.load(fis, KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            try {
                keyStoreTmp.load(null, KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e1) {
                return null;
            }
        }

        encryptionStore = keyStoreTmp;
        return encryptionStore;
    }

    private static void storeKeyStore(KeyStore keyStore) {
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
            keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
        }
    }

    private static byte[] read(File file) throws Exception {
        byte[] encrypted;
        byte[] iv;

        try (FileInputStream fis = new FileInputStream(file)) {
            iv = new byte[16];
            fis.read(iv);
            encrypted = copyStreamToByteArray(fis);
        }

        Cipher cipher = retrieveKeyDecryptionCipher(new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return decrypted;
    }

    private static void write(File file, byte[] data) throws Exception {
        Cipher cipher = retrieveKeyEncryptionCipher();
        IvParameterSpec ivParams = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
        byte[] iv = ivParams.getIV();

        byte[] encrypted = cipher.doFinal(data);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(iv);
            fos.write(encrypted);
        }
    }

    public static byte[] copyStreamToByteArray(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }

        return out.toByteArray();
    }

    public static void saveLoginData(LoginData loginData) throws Exception {
        if (loginData == null || loginData.getEmail() == null || loginData.getPassword() == null) {
            throw new Exception("Invalid login data");
        }

        logger.info("Saving user data");

        File file = new File(CREDENTIALS_FILE);
        byte[] data = new ObjectMapper().writeValueAsBytes(loginData);

        write(file, data);

        logger.info("User data saved");
    }

    public static LoginData loadLoginData() throws Exception {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return null;
        } else {
            logger.info("Loading user data");
            byte[] data = read(file);
            LoginData loginData = new ObjectMapper().readValue(data, LoginData.class);
            logger.info("User data loaded");
            return loginData;
        }
    }

    public static void deleteLoginData() throws Exception {
        logger.info("Deleting user data");
        File file = new File(CREDENTIALS_FILE);
        if (file.exists()) {
            file.delete();
        }
        logger.info("User data deleted");
    }
    /*
    public static byte[] encrypt(byte[] plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(plainText);
    }
    public static byte[] decrypt(byte[] cipherText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(cipherText);
    }
     */
}

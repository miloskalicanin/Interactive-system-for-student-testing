package rs.etf.teststudent.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.util.Collections;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import retrofit2.Response;
import rs.etf.teststudent.utils.Configuration;

public class NetworkUtils {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private static KeyStore keyStore = null;

    public static KeyStore loadKeyStore() {
        if (keyStore != null) {
            return keyStore;
        }
        try {
            keyStore = KeyStore.getInstance("PKCS12", "BC");
        } catch (Exception e) {
            return null;
        }

        File f = new File(Configuration.KEYSTORE_PATH);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                keyStore.load(fis, Configuration.KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                try {
                    keyStore.load(null, Configuration.KEYSTORE_PASSWORD.toCharArray());
                } catch (Exception e1) {
                    return null;
                }
            }
        } else {
            try {
                keyStore.load(null, Configuration.KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                return null;
            }
        }
        return keyStore;
    }

    public static void storeKeyStore() {
        try (FileOutputStream fos = new FileOutputStream(Configuration.KEYSTORE_PATH)) {
            keyStore.store(fos, Configuration.KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
        }
    }

    public static OkHttpClient create(KeyStore keyStore, boolean enforceTLS) throws
            NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException,
            KeyManagementException {

        KeyManager[] keyManagers = null;
        if (keyStore != null) {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                    .getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "keystore_pass".toCharArray());
            keyManagers = keyManagerFactory.getKeyManagers();
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (enforceTLS) {
            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                    .build();

            builder.connectionSpecs(Collections.singletonList(cs));
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, null, new SecureRandom());

        return builder.connectionPool(new ConnectionPool())
                .certificatePinner(CertificatePinner.DEFAULT)
                .sslSocketFactory(sslContext.getSocketFactory()).build();
    }

    public static void handleError(Response<?> response) {
        if (!response.isSuccessful()) {
            String string = null;
            try {
                string = response.errorBody().string();
            } catch (Exception e) {

            }
            throw new RetrofitException("Request failed with status:" + response.code()
                    + ", " + response.message() + "\n" + string, response.code());

        }
    }
}

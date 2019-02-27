package rs.etf.teststudent.network;

import android.content.Context;

import org.spongycastle.jce.provider.BouncyCastleProvider;

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
import rs.etf.teststudent.Configuration;

public class NetworkUtils {

    static {
        // register SC provider, needed for ECC crypto
        Security.addProvider(new BouncyCastleProvider());
    }

    private static KeyStore keyStore = null;

    public static KeyStore loadKeyStore(Context context) {
        if (keyStore != null) {
            return keyStore;
        }

        try {
            keyStore = KeyStore.getInstance("PKCS12", "SC");
        } catch (Exception e) {
            return null;
        }

        try (FileInputStream stream = context.openFileInput(Configuration.KEYSTORE_NAME)) {
            keyStore = KeyStore.getInstance("PKCS12", "SC");
            keyStore.load(stream, Configuration.KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            try {
                keyStore.load(null, Configuration.KEYSTORE_PASSWORD.toCharArray());
            } catch (Exception e1) {
                return null;
            }
        }

        return keyStore;
    }

    public static void storeKeyStore(Context context) {
        try (FileOutputStream fileOutputStream = context.openFileOutput(
                Configuration.KEYSTORE_NAME, Context.MODE_PRIVATE)) {
            keyStore.store(fileOutputStream, Configuration.KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {

        }
    }

    public static void deleteKeystore(Context context) {
        context.deleteFile(Configuration.KEYSTORE_NAME);
        keyStore = null;
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

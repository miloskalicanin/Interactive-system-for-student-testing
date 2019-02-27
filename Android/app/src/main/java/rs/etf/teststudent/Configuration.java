package rs.etf.teststudent;

public class Configuration {
    public static final String SERVER_URL = "http://192.168.0.15:8000/";
    public static final String MQTT_BROKER_URL = "ssl://192.168.0.15:8883";
    public static final boolean ENFORCE_TLS = false;

    public static final String KEYSTORE_NAME = "deviceKeyStore";
    public static final String KEYSTORE_PASSWORD = "Xge6pZP5:6<%jsq7";
    public static final String KEYSTORE_CERT_ENTRY = "student_cert";
    public static final String KEY_PASSWORD = "";
    public static final String KEY_PAIR_EC_ALGORITHM = "secp521r1";
}

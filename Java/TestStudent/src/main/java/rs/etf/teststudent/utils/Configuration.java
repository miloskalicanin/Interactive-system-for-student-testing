package rs.etf.teststudent.utils;

public class Configuration {

    public static final String APP_NAME = "Test Student";
    public static final String LOGIN_SCREEN_SUFFIX = " - Login";
    public static final String MAIN_SCREEN_SUFFIX = " - Dobro dosli";
    public static final String CREATE_TEST_SCREEN_SUFFIX = " - Napravi test";
    public static final String EDIT_TEST_SCREEN_SUFFIX = " - Izmeni test";
    public static final String TESTS_SCREEN_SUFFIX = " - Testovi";
    public static final String TESTINFO_SCREEN_SUFFIX = " - Informacije o testu";
    public static final String EDIT_QUESTION_SCREEN_SUFFIX = " - Izmena pitanja";
    public static final String CREATE_QUESTION_SCREEN_SUFFIX = " - Kreiranje pitanja";
    public static final String QUESTION_DETAILS_SCREEN_SUFFIX = " - Detalji pitanja";
    public static final String MANAGE_COURSES_SCREEN_SUFFIX = " - Upravljanje kursevima";
    public static final String MANAGE_STUDENTS_AND_COURSES_SCREEN_SUFFIX = " - Upravljanje pracenjem kursa";

    public static final String SERVER_URL = "http://localhost:8000/";
    public static final String MQTT_BROKER_URL = "ssl://localhost:8883";
    public static final boolean ENFORCE_TLS = false;

    public static final String KEYSTORE_PATH = "keystore.p12";
    public static final String KEYSTORE_PASSWORD = "b@q}XR[7%Ja:uYG2";
    public static final String KEYSTORE_CERT_ENTRY = "client_certificate";
    public static final String KEY_PASSWORD = "";
    public static final String KEY_PAIR_EC_ALGORITHM = "secp521r1";

    public static final int QR_CODE_SIZE = 360;
}

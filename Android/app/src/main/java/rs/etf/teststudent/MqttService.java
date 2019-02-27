package rs.etf.teststudent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMParser;

import java.io.StringReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import rs.etf.teststudent.network.NetworkUtils;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MqttService extends Service {
    private static final String TAG = MqttService.class.getSimpleName();

    public static final String MQTT_SUBSCRIBER_FILTER = "mqtt.client.subscribe";
    public static final String MQTT_UNSUBSCRIBE_FILTER = "mqtt.client.unsubscribe";
    public static final String MQTT_PUBLISH_FILTER = "mqtt.client.publish";
    public static final String MQTT_MESSAGES_CALLBACK_FILTER = "mqtt.client.messages";

    public static final String ACTION_START = "MyMqttClient.START";
    public static final String ACTION_RESTART = "MyMqttClient.RESTART";
    public static final String ACTION_RECONNECT = "MyMqttClient.RECONNECT";
    public static final String ACTION_KEEP_ALIVE = "MyMqttClient.KEEP_ALIVE";
    public static final String ACTION_KEEP_ALIVE_IDLE = "MyMqttClient.KEEP_ALIVE_IDLE";
    public static final String ACTION_SUBSCRIBE = "MyMqttClient.SUBSCRIBE";
    public static final String ACTION_UNSUBSCRIBE = "MyMqttClient.UNSUBSCRIBE";
    public static final String ACTION_PUBLISH = "MyMqttClient.PUBLISH";
    public static final String ACTION_STOPPING = "MyMqttClient.STOPPING";


    private static final long INITIAL_RETRY_INTERVAL = 500L;
    private static final long MAXIMUM_RETRY_INTERVAL = 60000L;
    private static final short MQTT_CONNECT_TIMEOUT = 15;
    private static final short MQTT_ACK_TIMEOUT = 15000;
    private static final boolean MQTT_RETAINED_PUBLISH = false;
    private static final short MQTT_DEFAULT_KEEP_ALIVE = 300;
    private static final short MQTT_ALARM_KEEP_ALIVE = 240;
    private static final int MQTT_QUALITY_OF_SERVICE = 2;

    private static String username = null;

    static {
        // Add BouncyCastle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private WifiManager mWifiManager;
    private AlarmManager mAlarmManager;
    private ConnectivityManager mConnectivityManager;
    private WifiManager.WifiLock mWifiWakeLock;
    private boolean mIsDestroyed;
    private MqttConnection mqttConnection;
    private long mRetryInterval = 500L;


    private BroadcastReceiver mConnectivityListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent != null ? intent.getAction() : null;
            Log.d(TAG, "BroadcastReceiver - received action: " + action);

            if (isNetworkAvailable()) {
                mRetryInterval = 500L;
                if (!mqttConnection.isConnected()) {
                    Log.d(TAG, "Client is not connected => reconnectAsync");
                    reconnectAsync();
                } else if (getConnectedNetwork() == 1 && mqttConnection.getNetwork() == 0) {
                    Log.d(TAG, "State changed from Wi-Fi to mobile network");
                    reconnectAsync();
                }
            } else {
                Log.d(TAG, "Internet disconnected");
                cancelReconnect();
            }
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating service");

        mWifiManager = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        mAlarmManager = ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE));
        mConnectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));

        if (mWifiManager == null || mAlarmManager == null || mConnectivityManager == null) {
            Log.d(TAG, "Can't initialize mqtt service");
            stopSelf();
            return;
        }

        mqttConnection = new MqttConnection();

        handleCrashedService();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityListener, filter);

        mIsDestroyed = false;

        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.d(TAG, "Service started with action: NULL");
            start();
            return START_STICKY;
        }

        String action = intent.getAction();

        Log.d(TAG, "Service started with action: " + action);

        switch (action) {
            case ACTION_START:
                username = intent.getStringExtra("USERNAME");
                start();
                break;
            case ACTION_RESTART:
                start();
                //  onCreate();
                break;
            case ACTION_KEEP_ALIVE:
                sendKeepAlive();
                break;
            case ACTION_KEEP_ALIVE_IDLE:
                // every 15 min (this is for doze mode)
                sendKeepAlive();
                tickKeepAliveTimer();
                break;
            case ACTION_RECONNECT:
                reconnectAsync();
                break;
            case ACTION_SUBSCRIBE: {
                String topic = intent.getStringExtra("TOPIC");
                subscribe(topic);
                break;
            }
            case ACTION_UNSUBSCRIBE: {
                String topic = intent.getStringExtra("TOPIC");
                unsubscribe(topic);
                break;
            }
            case ACTION_PUBLISH: {
                String topic = intent.getStringExtra("TOPIC");
                String message = intent.getStringExtra("MESSAGE");

                if (topic != null && message != null && !topic.isEmpty() && !message.isEmpty()) {
                    publishMessage(topic, message);
                } else {
                    sendMqttActionBroadcast(MQTT_PUBLISH_FILTER, topic, false);
                }
                break;
            }
            case ACTION_STOPPING:
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Task removed, attempting restart in 5 seconds");

        Intent restartService = new Intent(getApplicationContext(), MqttService.class);
        restartService.setPackage(getPackageName());
        restartService.setAction(ACTION_RESTART);

        PendingIntent restartServiceIntent = PendingIntent.getService(getApplicationContext(), 0, restartService, FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, System.currentTimeMillis() + 5000L, restartServiceIntent);
        } else {
            mAlarmManager.set(RTC_WAKEUP, System.currentTimeMillis() + 5000L, restartServiceIntent);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");

        if (!mIsDestroyed) {
            mIsDestroyed = true;
            stop();
        }

        super.onDestroy();
    }

    private void start() {
        Log.d(TAG, "Method start");

        if (username == null || username.isEmpty()) {
            cancelReconnect();
            return;
        }
        stopStoppingg();

        if (!isNetworkAvailable()) {
            Log.d(TAG, "No network available");
            return;
        }
        if (mqttConnection.isConnecting()) {
            Log.d(TAG, "Currently connecting, schedule reconnect in case connect fails");
            scheduleReconnect();
            return;
        } else if (mqttConnection.isConnected()) {
            Log.d(TAG, "MQTT client is connected");
            return;
        }

        new ConnectAsync().execute();
    }

    private void stop() {
        Log.d(TAG, "Stop service (cancel reconnect, stop KeepAliveTimer and WifiLock, unregister receiver and disconnect client)");

        cancelReconnect();
        stopKeepAliveTimerAndWifiLock();
        unregisterReceiver(mConnectivityListener);

        mqttConnection.disconnectExistingClient();
    }

    private void handleCrashedService() {
        stopKeepAliveTimerAndWifiLock();
        cancelReconnect();
    }

    private void reconnectAsync() {
        Log.d(TAG, "reconnectAsync");

        if (mIsDestroyed) {
            Log.d(TAG, "Service is destroyed");
            return;
        }
        if (username == null || username.isEmpty()) {
            cancelReconnect();
            return;
        }

        stopStoppingg();
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No network available");
            return;
        }
        if (mqttConnection.isConnecting()) {
            Log.d(TAG, "Currently connecting, schedule reconnect in case connect fails");
            scheduleReconnect();
            return;
        }
        if (this.mqttConnection.isConnected()) {
            Log.d(TAG, "Already connected");
            return;
        }
        handleCrashedService();

        new ConnectAsync().execute();
    }

    public void scheduleReconnect() {
        if (mRetryInterval >= 60000L) {
            mRetryInterval = 60000L;
        }
        mRetryInterval = Math.min(mRetryInterval * 2L, 120000L);

        Log.d(TAG, "scheduleReconnect - reconnecting in " + mRetryInterval / 1000 + " seconds.");

        PendingIntent keepAliveIntent = getAlarmPendingIntent(ACTION_RECONNECT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, System.currentTimeMillis() + mRetryInterval, keepAliveIntent);
        } else {
            mAlarmManager.set(RTC_WAKEUP, System.currentTimeMillis() + mRetryInterval, keepAliveIntent);
        }
    }

    private void sendKeepAlive() {
        Log.d(TAG, "sendKeepAlive");
        if (mqttConnection.isConnected()) {
            new SendKeepAliveAsync().execute();
        } else {
            reconnectAsync();
        }
    }

    PendingIntent getAlarmPendingIntent(String action) {
        Intent intent = new Intent(this, MqttService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private void startKeepAliveTimerAndWifiLock() {
        long interval = MQTT_ALARM_KEEP_ALIVE * 1000;

        PendingIntent pendingIntent = getAlarmPendingIntent(ACTION_KEEP_ALIVE);

        mAlarmManager.setRepeating(RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);

        acquireWifiLock();
        tickKeepAliveTimer();
    }

    private void tickKeepAliveTimer() {
        long interval = 15 * 60 * 1000; //every 15 minuters

        PendingIntent pendingIntent = getAlarmPendingIntent(ACTION_KEEP_ALIVE_IDLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
        } else {
            mAlarmManager.set(RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent);
        }
    }

    private void stopKeepAliveTimerAndWifiLock() {
        PendingIntent keepAliveIntent = getAlarmPendingIntent(ACTION_KEEP_ALIVE);
        mAlarmManager.cancel(keepAliveIntent);

        releaseWifiLock();
        stopTickKeepAliveTimer();
    }

    private void stopTickKeepAliveTimer() {
        PendingIntent keepAliveIntent = getAlarmPendingIntent(ACTION_KEEP_ALIVE_IDLE);
        mAlarmManager.cancel(keepAliveIntent);
    }

    private void startStopping() {
        // stops in 10 minutes if not connect
        Log.d(TAG, "startStopping - Stops in 10 minutes if not connect");

        long millis = System.currentTimeMillis() + 10L * 60 * 1000;

        PendingIntent stoppingIntent = getAlarmPendingIntent(ACTION_STOPPING);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, millis, stoppingIntent);
        } else {
            mAlarmManager.set(RTC_WAKEUP, millis, stoppingIntent);
        }
    }

    private void stopStoppingg() {
        PendingIntent stoppingIntent = getAlarmPendingIntent(ACTION_STOPPING);
        mAlarmManager.cancel(stoppingIntent);
    }

    public void cancelReconnect() {
        Log.d(TAG, "cancelReconnect");

        PendingIntent reconnectIntent = getAlarmPendingIntent(ACTION_RECONNECT);
        mAlarmManager.cancel(reconnectIntent);

        /*
        if (username == null || username.isEmpty()) {
            startStopping(); // service is started after user is logged in, no need for stopping
        }
        */
    }

    private void acquireWifiLock() {
        if (mWifiWakeLock == null) {
            mWifiWakeLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG);
        }

        if (mWifiWakeLock.isHeld()) {
            return;
        }

        mWifiWakeLock.setReferenceCounted(false);
        mWifiWakeLock.acquire();
    }

    private void releaseWifiLock() {
        if (mWifiWakeLock == null || !mWifiWakeLock.isHeld()) {
            return;
        }

        try {
            mWifiWakeLock.release();
        } catch (Exception exc) {

        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private int getConnectedNetwork() {
        NetworkInfo activeNetwork = this.mConnectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return 1;
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 0;
            }
        }

        return -1; // not connected
    }

    private void publishMessage(String topic, String message) {
        Log.d(TAG, "Publishing message on topic: " + topic);
        if (mqttConnection.isConnected()) {
            new PublishAsync(topic, message).execute();
        } else {
            reconnectAsync();
            sendMqttActionBroadcast(MQTT_PUBLISH_FILTER, topic, false);
        }
    }

    private void subscribe(String topic) {
        Log.d(TAG, "Subscribing to topic: " + topic);
        if (mqttConnection.isConnected()) {
            new SubscribeAsync(topic).execute();
        } else {
            reconnectAsync();
            sendMqttActionBroadcast(MQTT_SUBSCRIBER_FILTER, topic, false);
        }
    }

    private void unsubscribe(String topic) {
        Log.d(TAG, "Unsubscribing from topic: " + topic);
        if (mqttConnection.isConnected()) {
            new UnsubscribeAsync(topic).execute();
        } else {
            sendMqttActionBroadcast(MQTT_UNSUBSCRIBE_FILTER, topic, false);
            reconnectAsync();
        }
    }

    public class ConnectAsync extends AsyncTask<Void, Void, Void> {
        ConnectAsync() {
        }

        protected Void doInBackground(Void... parameter) {
            try {
                mqttConnection.connect();
            } catch (Exception e) {
                Log.e(TAG, "Connect exception", e);

                mqttConnection.setConnecting(false);

                if (e instanceof MqttSecurityException) {
                    if (((MqttSecurityException) e).getReasonCode() == 5) {
                        Log.d(TAG, "MQTT connect returned error code 5, invalid credentials (username/password/certificate)");
                        MqttService.this.stopSelf();
                        return null;
                    }
                }

                if (isNetworkAvailable()) {
                    scheduleReconnect();
                }
            }

            return null;
        }
    }


    public class PublishAsync extends AsyncTask<Void, Void, Void> {
        private String topic, message;

        PublishAsync(String t, String m) {
            topic = t;
            message = m;
        }

        protected Void doInBackground(Void... parameter) {
            mqttConnection.publish(topic, message);
            return null;
        }
    }


    public class SendKeepAliveAsync extends AsyncTask<Void, Void, Void> {
        SendKeepAliveAsync() {
        }

        protected Void doInBackground(Void... parameter) {
            mqttConnection.sendKeepAlive();
            return null;
        }
    }

    public class SubscribeAsync extends AsyncTask<Void, Void, Void> {
        private String topic;

        SubscribeAsync(String t) {
            topic = t;
        }

        protected Void doInBackground(Void... parameter) {
            mqttConnection.subscribeToTopic(topic);
            return null;
        }
    }

    public class UnsubscribeAsync extends AsyncTask<Void, Void, Void> {
        private String topic;

        public UnsubscribeAsync(String t) {
            topic = t;
        }

        protected Void doInBackground(Void... parameter) {
            mqttConnection.unsubscribeFromTopic(topic);
            return null;
        }
    }

    private void sendMqttActionBroadcast(String fiter, String topic, boolean status) {
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(fiter);
        broadCastIntent.putExtra("MQTT_TOPIC", topic);
        broadCastIntent.putExtra("STATUS", status);
        sendBroadcast(broadCastIntent);
    }

    private class MqttConnection {
        private final String TAG = MqttConnection.class.getSimpleName();

        private static final String CAString = "-----BEGIN CERTIFICATE-----\n" +
                "MIIB9jCCAZygAwIBAgIJAKuq3ojdQdvmMAoGCCqGSM49BAMEMFYxCzAJBgNVBAYT\n" +
                "AlJTMQ8wDQYDVQQIDAZTZXJiaWExETAPBgNVBAcMCEJlbGdyYWRlMQwwCgYDVQQK\n" +
                "DANFVEYxFTATBgNVBAMMDHRlc3Qtc3R1ZGVudDAeFw0xODA3MTQxMTAwMzFaFw0y\n" +
                "MTA1MDMxMTAwMzFaMFYxCzAJBgNVBAYTAlJTMQ8wDQYDVQQIDAZTZXJiaWExETAP\n" +
                "BgNVBAcMCEJlbGdyYWRlMQwwCgYDVQQKDANFVEYxFTATBgNVBAMMDHRlc3Qtc3R1\n" +
                "ZGVudDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBNn3OSCasribhZMVl0HpjnS\n" +
                "FmHbHXhaPZIjJb1R8Q7UHfb0YkQuK//PwctbQp15pNXIwgKqQzBiXwdZTMK3nj2j\n" +
                "UzBRMB0GA1UdDgQWBBRKeDDBVBt/BLaqkqMWd//J1exQSTAfBgNVHSMEGDAWgBRK\n" +
                "eDDBVBt/BLaqkqMWd//J1exQSTAPBgNVHRMBAf8EBTADAQH/MAoGCCqGSM49BAME\n" +
                "A0gAMEUCIHKO1Pe9dUOqDQxh9edmUpSkKeTxfZk/ZFESkg+Gd8LcAiEA7lOolzG0\n" +
                "U3gUdSwA3Po4kDVACqeajp3bm0f+Bwkv7CY=\n" +
                "-----END CERTIFICATE-----";

        private int mNetwork;
        private int mIsConnecting = 0;
        private MqttAndroidClient mClient;

        MqttConnection() {
            Log.d(TAG, "MqttConnection created");
        }


        SSLSocketFactory getSocketFactory(String password) {

            try {
                JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider());

                // Load Certificate Authority (CA) certificate
                PEMParser reader = new PEMParser(new StringReader(CAString));
                X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
                reader.close();

                X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

                // Load client certificate
                KeyStore ks = NetworkUtils.loadKeyStore(getApplicationContext());

                final Key privateKey = ks.getKey(Configuration.KEYSTORE_CERT_ENTRY, Configuration.KEY_PASSWORD.toCharArray());
                final Certificate cert = ks.getCertificate(Configuration.KEYSTORE_CERT_ENTRY);
                final PublicKey publicKey = cert.getPublicKey();

                KeyPair key = new KeyPair(publicKey, (PrivateKey) privateKey);

                // CA certificate is used to authenticate server
                KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                caKeyStore.load(null, null);
                caKeyStore.setCertificateEntry("ca-certificate", caCert);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(caKeyStore);

                // Client key and certificates are sent to server so it can authenticate the client
                KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                clientKeyStore.load(null, null);
                clientKeyStore.setCertificateEntry("certificate", cert);
                clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                        new Certificate[]{cert});

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(clientKeyStore, password.toCharArray());

                // Create SSL socket factory
                SSLContext context = SSLContext.getInstance("TLSv1.1");
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                // Return the newly created socket factory object
                return context.getSocketFactory();

            } catch (Exception e) {
                Log.e(TAG, "Failed to get socket factory", e);

            }
            return null;
        }

        synchronized void connect() throws Exception {
            Log.d(TAG, "Connecting mqtt client...");

            if (isConnecting() || isConnected()) {
                Log.d(TAG, "Already connecting");
                scheduleReconnect();
                return;
            }

            setConnecting(true);

            disconnectExistingClient();

            mClient = new MqttAndroidClient(MqttService.this.getApplicationContext(), Configuration.MQTT_BROKER_URL, username);

            mClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection lost", cause);
                    //disconnectExistingClient();
                    scheduleReconnect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Received push for topic: " + topic);
                    Log.d(TAG, "Received message: " + new String(message.getPayload()));

                    Intent intent = new Intent();
                    intent.setAction(MQTT_MESSAGES_CALLBACK_FILTER);
                    intent.putExtra("MQTT_TOPIC", topic);
                    intent.putExtra("MQTT_MESSAGE_PAYLOAD", message.getPayload());
                    sendBroadcast(intent);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            //mClient.setTimeToWait(MqttService.MQTT_ACK_TIMEOUT);

            mNetwork = getConnectedNetwork();

            MqttConnectOptions connectOptions = new MqttConnectOptions();

            //connectOptions.setUserName(username);
            //connectOptions.setPassword(password.toCharArray());
            connectOptions.setSocketFactory(getSocketFactory("password"));

            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(false);
            //connectOptions.setConnectionTimeout(MqttService.MQTT_CONNECT_TIMEOUT);
            connectOptions.setKeepAliveInterval(MQTT_DEFAULT_KEEP_ALIVE);

            IMqttToken token = mClient.connect(connectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "MQTT client connected");
                    mqttConnection.setConnecting(false);
                    mRetryInterval = 500L;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT client connection failed", exception);
                    mqttConnection.setConnecting(false);
                    disconnectExistingClient();
                    scheduleReconnect();
                }
            });

            startKeepAliveTimerAndWifiLock();

            Log.d(TAG, "Connecting finished, sending keep alive every " + MQTT_ALARM_KEEP_ALIVE + " seconds");
            if (mIsDestroyed) {
                disconnectExistingClient();
                Log.d(TAG, "Service destroyed, aborting connection");
            }
        }

        void subscribeToTopic(String topic) {
            Log.d(TAG, "Subscribe to topic " + topic);
            try {
                mClient.subscribe(topic, MqttService.MQTT_QUALITY_OF_SERVICE);
                sendMqttActionBroadcast(MQTT_SUBSCRIBER_FILTER, topic, true);
            } catch (Exception e) {
                Log.d(TAG, "Subscribe failed", e);
                sendMqttActionBroadcast(MQTT_SUBSCRIBER_FILTER, topic, false);
                reconnectAsync();
            }
        }

        void unsubscribeFromTopic(String topic) {
            Log.d(TAG, "Unsubscribe from topic " + topic);
            try {
                mClient.unsubscribe(topic);
                sendMqttActionBroadcast(MQTT_UNSUBSCRIBE_FILTER, topic, true);
            } catch (Exception e) {
                Log.d(TAG, "Unsubscribe failed", e);
                sendMqttActionBroadcast(MQTT_UNSUBSCRIBE_FILTER, topic, false);
                reconnectAsync();
            }
        }

        void publish(String topic, String payload) {
            Log.d(TAG, "Publishing message: " + payload + "\non topic: " + topic);
            try {
                mClient.publish(topic, payload.getBytes(), MqttService.MQTT_QUALITY_OF_SERVICE, MqttService.MQTT_RETAINED_PUBLISH);
                if (!"keepalive".equals(topic)) {
                    sendMqttActionBroadcast(MQTT_PUBLISH_FILTER, topic, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Publishing message failed", e);
                sendMqttActionBroadcast(MQTT_PUBLISH_FILTER, topic, false);
                reconnectAsync();
            }
        }

        void sendKeepAlive() {
            publish("keepalive", "");
        }

        synchronized void disconnectExistingClient() {
            /*
            if ((this.mClient == null) || (!this.mClient.isConnected())) {
                return;
            }
            */
            try {
                //  this.mClient.disconnectForcibly(2000L, 2000L);
                mClient.disconnect();
                mClient.close();
                mClient = null;
            } catch (Exception e) {
            }
        }

        boolean isConnected() {
            try {
                return mClient != null && mClient.isConnected();
            } catch (Exception e) {
                return false;
            }
        }

        int getNetwork() {
            return mNetwork;
        }

        synchronized boolean isConnecting() {
            return mIsConnecting > 0;
        }

        synchronized void setConnecting(boolean value) {
            mIsConnecting = value ? mIsConnecting + 1 : mIsConnecting - 1;
            if (mIsConnecting < 0) {
                mIsConnecting = 0;
            }
        }
    }

    /*
    private synchronized void pushNotification(String topic, String text) {
        Log.d(TAG, "pushNotification - topic: " + topic + " text: " + text);
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(MqttService.this, notificationID, intent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(MqttService.this);

        Notification.BigTextStyle inboxStyle = new Notification.BigTextStyle();
        inboxStyle.setBuilder(notificationBuilder);
        inboxStyle.setBigContentTitle(topic);
        inboxStyle.bigText(text);
        inboxStyle.setSummaryText("");

        notificationBuilder
                .setStyle(inboxStyle)
                .setContentTitle(topic)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationID++, notification);
    }
    */
}


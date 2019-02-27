package rs.etf.teststudent;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import rs.etf.teststudent.dto.MqttQuestionDTO;
import rs.etf.teststudent.fragments.LoginFragment;
import rs.etf.teststudent.fragments.MainFragment;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();


    public static MqttQuestionDTO questionSentAnswer = null;

    private BroadcastReceiver mqttPublishListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                String topic = intent.getStringExtra("MQTT_TOPIC");
                boolean status = intent.getBooleanExtra("STATUS", false);
                if (questionSentAnswer != null && questionSentAnswer.getMqttThemeA().equals(topic)) {
                    if (status) {
                        MySharedPreferences.getInstance(getApplication()).removeQuestion(questionSentAnswer);
                    }
                    questionSentAnswer = null;
                }
            } catch (Exception e) {

            }
        }
    };

    private BroadcastReceiver mqttQuestionsListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttQuestionsListener received question");

                byte[] payload = intent.getByteArrayExtra("MQTT_MESSAGE_PAYLOAD");
                String topic = intent.getStringExtra("MQTT_TOPIC");

                MqttQuestionDTO question = new ObjectMapper().readValue(new String(payload), MqttQuestionDTO.class);

                MySharedPreferences.getInstance(getApplication()).addQuestion(question);

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse question", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request BATTERY_OPTIMIZATIONS permissions
        /*
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
        */

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_MESSAGES_CALLBACK_FILTER);
        registerReceiver(mqttQuestionsListener, filter);

        filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_PUBLISH_FILTER);
        registerReceiver(mqttPublishListener, filter);

        openLoginFragment();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mqttQuestionsListener);
        unregisterReceiver(mqttPublishListener);

        Intent intent = new Intent(getApplicationContext(), MqttService.class);
        stopService(intent);

        super.onDestroy();
    }

    private void replaceFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getFragmentManager();

            if (fragment instanceof LoginFragment || fragment instanceof MainFragment) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_activity_fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            Log.d(TAG, "Opened fragment " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.d(TAG, "Failed to replace fragment " + fragment.getClass().getSimpleName());
        }
    }

    public void openLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        replaceFragment(fragment);
    }

    public void openMainFragment(String email, int position) {
        Fragment fragment = MainFragment.newInstance(email, position);
        replaceFragment(fragment);
    }

    // TODO: promeniti mqtt tagove za prosledjivanje podataka sa statickim poljima

}

package rs.etf.teststudent.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import rs.etf.teststudent.MainActivity;
import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.UserInfoDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.network.NetworkUtils;

public class UserInfoFragment extends Fragment {
    private static final String TAG = UserInfoFragment.class.getSimpleName();

    public static UserInfoDTO currentUserInfo = null;

    public static UserInfoFragment newInstance(String email) {
        UserInfoFragment fragment = new UserInfoFragment();

        Bundle args = new Bundle();
        args.putString("emailText", email);
        fragment.setArguments(args);

        return fragment;
    }

    private String emailText;
    private LinearLayout userInfoHolder;
    private TextView name;
    private TextView lastName;
    private TextView type;
    private TextView email;
    private Button logout;

    private boolean logoutRequested = false;
    private ProgressDialog progress;

    private BroadcastReceiver mqttUnsubscriberListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttUnsubscriberListener received message");

                String topic = intent.getStringExtra("MQTT_TOPIC");
                boolean status = intent.getBooleanExtra("STATUS", false);
                if ("etf/#".equals(topic) && logoutRequested) {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                    if (status) {
                        Log.d(TAG, "Logging out..");
                        NetworkUtils.deleteKeystore(getActivity().getApplicationContext());

                        MySharedPreferences.getInstance(getActivity().getApplication()).deleteData();
                        currentUserInfo = null;

                        MainActivity activity = (MainActivity) getActivity();
                        activity.openLoginFragment();
                        //((ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                    } else {
                        Toast.makeText(getActivity(), R.string.logout_failed_message, Toast.LENGTH_SHORT).show();
                    }

                    logoutRequested = false;
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.logout_failed_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            emailText = bundle.getString("emailText");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_UNSUBSCRIBE_FILTER);
        getActivity().registerReceiver(mqttUnsubscriberListener, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        userInfoHolder = view.findViewById(R.id.user_info_holder);
        name = view.findViewById(R.id.name);
        lastName = view.findViewById(R.id.last_name);
        type = view.findViewById(R.id.type);
        email = view.findViewById(R.id.email);
        logout = view.findViewById(R.id.logout_button);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (logoutRequested) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_dialog_logout_title)
                        .setMessage(R.string.alert_dialog_logout_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    Log.d(TAG, "Sending unsubscribe request");
                                    Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
                                    intent.setAction(MqttService.ACTION_UNSUBSCRIBE);
                                    intent.putExtra("TOPIC", "etf/#");
                                    getActivity().startService(intent);
                                    Log.d(TAG, "Unsubscribe request sent");

                                    logoutRequested = true;

                                    progress = new ProgressDialog(getActivity());
                                    progress.setMessage(getString(R.string.logging_out_message));
                                    progress.show();

                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to unsubscribe to all topic", e);
                                    Toast.makeText(getActivity(), R.string.logout_failed_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, null);

                builder.show();
            }
        });

        userInfoHolder.setVisibility(View.GONE);

        displayUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentUserInfo == null) {
            UserInfoTask task = new UserInfoTask(emailText);
            task.execute();
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mqttUnsubscriberListener);

        super.onDestroy();
    }

    private void displayUserData() {
        if (currentUserInfo == null || isDetached() || !isAdded()) {
            return;
        }

        String typeCapitalized = currentUserInfo.getType().toLowerCase();
        typeCapitalized = typeCapitalized.substring(0, 1).toUpperCase() + typeCapitalized.substring(1);

        name.setText(currentUserInfo.getName());
        lastName.setText(currentUserInfo.getLastNname());
        type.setText(typeCapitalized);
        email.setText(currentUserInfo.getEmail());

        userInfoHolder.setVisibility(View.VISIBLE);
    }


    private class UserInfoTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;

        private String email;
        private UserInfoDTO response = null;

        public UserInfoTask(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progress = new ProgressDialog(getActivity());

                progress.setMessage(getString(R.string.user_info_progress_dialog));

                progress.show();
                progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        UserInfoTask.this.cancel(true);
                    }
                });
            } catch (Exception e) {
                UserInfoTask.this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG, "Getting user info");

                response = NetworkServicePrivate.getInstance(getActivity().getApplicationContext()).userInfo(email);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to get user info", ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (response != null) {
                Log.d(TAG, "Collected user info");

                currentUserInfo = response;

                displayUserData();
            } else {
                Toast.makeText(getActivity(), R.string.user_data_failed_message, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

}

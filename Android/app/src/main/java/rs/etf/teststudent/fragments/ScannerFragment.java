package rs.etf.teststudent.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.ClassCheckInDTO;
import rs.etf.teststudent.dto.QrCodePayload;
import rs.etf.teststudent.network.NetworkServicePrivate;

public class ScannerFragment extends Fragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();

    private static final int REQUEST_CAMERA_PERMISSION = 123;

    public static ScannerFragment newInstance(String email) {
        ScannerFragment fragment = new ScannerFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);

        return fragment;
    }


    private String email;
    private QrCodePayload currentClassData;

    private CompoundBarcodeView barcodeScannerView = null;
    private LinearLayout classInfoHolder;
    private TextView course;
    private TextView classroom;
    private TextView testName;
    private TextView date;
    private Handler handler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver mqttSubscriberListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttSubscriberListener received message");

                String topic = intent.getStringExtra("MQTT_TOPIC");
                if (currentClassData != null && currentClassData.getMqttThemeQ().equals(topic)) {
                    boolean status = intent.getBooleanExtra("STATUS", false);
                    if (!status) {
                        throw new Exception("Failed to subscribe to topic");
                    }

                    MySharedPreferences.getInstance(getActivity().getApplication()).addActiveClass(currentClassData);

                    course.setText(currentClassData.getCourse());
                    classroom.setText(currentClassData.getClassroom());
                    testName.setText(currentClassData.getTestName());
                    date.setText(currentClassData.getDate());

                    barcodeScannerView.pause();
                    barcodeScannerView.setVisibility(View.GONE);
                    classInfoHolder.setVisibility(View.VISIBLE);

                    currentClassData = null;
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.check_in_failed_message, Toast.LENGTH_SHORT).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barcodeScannerView.resume();
                        } catch (Exception e) {

                        }
                    }
                }, 2500);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            email = bundle.getString("email");
        }


        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_SUBSCRIBER_FILTER);
        getActivity().registerReceiver(mqttSubscriberListener, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        barcodeScannerView = view.findViewById(R.id.qr_code_scanner);
        barcodeScannerView.decodeContinuous(callback);
        barcodeScannerView.setStatusText("");

        classInfoHolder = view.findViewById(R.id.class_info_holder);
        course = view.findViewById(R.id.course);
        classroom = view.findViewById(R.id.classroom);
        testName = view.findViewById(R.id.test_name);
        date = view.findViewById(R.id.date);

        barcodeScannerView.setVisibility(View.VISIBLE);
        classInfoHolder.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mqttSubscriberListener);

        super.onDestroy();
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            Log.d(TAG, "QR Result: " + result.getText());

            parseQrCodeData(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }

        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            try {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this).attach(this).commit();
            } catch (Exception e) {

            }
        }
    }

    private void parseQrCodeData(String scannedData) {
        try {
            barcodeScannerView.pause();

            QrCodePayload payload = new ObjectMapper().readValue(scannedData, QrCodePayload.class);

            new CheckInTask(email, payload).execute();
        } catch (Exception e) {
            Log.d(TAG, "Error while parsing QR code", e);

            Toast.makeText(getActivity(), R.string.invalid_qr_code_message, Toast.LENGTH_SHORT).show();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        barcodeScannerView.resume();
                    } catch (Exception e) {

                    }
                }
            }, 2500);
        }
    }


    private class CheckInTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = CheckInTask.class.getSimpleName();

        private ProgressDialog progress;

        private String email;
        private QrCodePayload classInfo;

        boolean successful = false;

        public CheckInTask(String email, QrCodePayload classInfo) {
            this.email = email;
            this.classInfo = classInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progress = new ProgressDialog(getActivity());

                progress.setMessage(getString(R.string.check_in_progress_dialog));

                progress.show();
                progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        CheckInTask.this.cancel(true);
                    }
                });
            } catch (Exception e) {
                CheckInTask.this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG, "Check-in to class");

                ClassCheckInDTO request = new ClassCheckInDTO(classInfo.getMqttThemeQ(), email);

                NetworkServicePrivate.getInstance(getActivity().getApplicationContext()).classCheckIn(request);

                Log.d(TAG, "Checked-in");

                successful = true;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to check-in", ex);
                return null;
            }

            currentClassData = classInfo;

            try {
                Log.d(TAG, "Sending subscribe request");
                Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
                intent.setAction(MqttService.ACTION_SUBSCRIBE);
                intent.putExtra("TOPIC", classInfo.getMqttThemeQ());
                getActivity().startService(intent);
                Log.d(TAG, "Subscribe request sent");
            } catch (Exception ex) {
                Log.e(TAG, "Failed to subscribe to topic", ex);
                successful = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (getActivity() == null || isDetached() || !isAdded()) {
                return;
            }

            if (!successful) {
                currentClassData = null;

                Toast.makeText(getActivity(), R.string.check_in_failed_message, Toast.LENGTH_SHORT).show();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barcodeScannerView.resume();
                        } catch (Exception e) {

                        }
                    }
                }, 2500);
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

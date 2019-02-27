package rs.etf.teststudent.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.R;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    public static final int USERINGO_FRAGMENT = 0;
    public static final int SCANNER_FRAGMENT = 1;
    public static final int THEMES_FRAGMENT = 2;
    public static final int QUESTIONS_FRAGMENT = 3;
    public static final int TAB_COUNT = 4;

    public static Fragment newInstance(String email, int position) {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        args.putInt("tabPosition", position);
        fragment.setArguments(args);

        return fragment;
    }

    private String email;
    private int tabPosition = 0;
    private ImageView userInfoButton;
    private RelativeLayout userInfoButtonHolder;
    private ImageView scannerButton;
    private RelativeLayout scannerButtonHolder;
    private ImageView themesButton;
    private RelativeLayout themesButtonHolder;
    private ImageView questionsButton;
    private RelativeLayout questionsButtonHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            email = bundle.getString("email");
            tabPosition = bundle.getInt("tabPosition", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        userInfoButton = view.findViewById(R.id.button_user_info);
        userInfoButtonHolder = view.findViewById(R.id.button_user_info_holder);
        userInfoButtonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(USERINGO_FRAGMENT);
            }
        });

        scannerButton = view.findViewById(R.id.button_scanner);
        scannerButtonHolder = view.findViewById(R.id.button_scanner_holder);
        scannerButtonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(SCANNER_FRAGMENT);
            }
        });

        themesButton = view.findViewById(R.id.button_active_themes);
        themesButtonHolder = view.findViewById(R.id.button_active_themes_holder);
        themesButtonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(THEMES_FRAGMENT);
            }
        });

        questionsButton = view.findViewById(R.id.button_questions);
        questionsButtonHolder = view.findViewById(R.id.button_questions_holder);
        questionsButtonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(QUESTIONS_FRAGMENT);
            }
        });

        if (tabPosition < 0 || tabPosition >= TAB_COUNT) {
            tabPosition = 0;
        }

        int position = tabPosition;
        tabPosition = -1;
        openFragment(position);

        startMqttClient();

        return view;
    }

    private void openFragment(int position) {
        if (position == tabPosition) {
            return;
        }
        Log.d(TAG, "Open fragment at position: " + position);

        try {
            Fragment fragment = getFragment(position);
            if (fragment == null) {
                throw new Exception("Fragment not found");
            }

            FragmentManager fragmentManager = getChildFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_fragment_container, fragment);
            transaction.commit();

            userInfoButton.setImageResource(R.drawable.ic_account_card_details_grey600_24dp);
            scannerButton.setImageResource(R.drawable.ic_qrcode_scan_grey600_24dp);
            themesButton.setImageResource(R.drawable.ic_history_grey600_24dp);
            questionsButton.setImageResource(R.drawable.ic_view_list_grey600_24dp);

            switch (position) {
                case USERINGO_FRAGMENT:
                    userInfoButton.setImageResource(R.drawable.ic_account_card_details_white_24dp);
                    break;
                case SCANNER_FRAGMENT:
                    scannerButton.setImageResource(R.drawable.ic_qrcode_scan_white_24dp);
                    break;
                case THEMES_FRAGMENT:
                    themesButton.setImageResource(R.drawable.ic_history_white_24dp);
                    break;
                case QUESTIONS_FRAGMENT:
                    questionsButton.setImageResource(R.drawable.ic_view_list_white_24dp);
                    break;
            }

            tabPosition = position;

        } catch (Exception e) {
            Log.e(TAG, "Failed to open fragment at position: " + position, e);
        }
    }

    private Fragment getFragment(int position) {
        Fragment fragment = null;

        switch (position) {
            case USERINGO_FRAGMENT:
                fragment = UserInfoFragment.newInstance(email);
                break;
            case SCANNER_FRAGMENT:
                fragment = ScannerFragment.newInstance(email);
                break;
            case THEMES_FRAGMENT:
                fragment = TestsFragment.newInstance();
                break;
            case QUESTIONS_FRAGMENT:
                fragment = QuestionsFragment.newInstance(email);
                break;
        }

        return fragment;
    }

    private void startMqttClient() {
        Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
        intent.setAction(MqttService.ACTION_START);
        intent.putExtra("USERNAME", email);
        getActivity().startService(intent);

        Log.d(TAG, "Started MQTT client");
    }
}

package rs.etf.teststudent.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.QrCodePayload;
import rs.etf.teststudent.recycler.ActiveThemesItemHolderFactory;
import rs.etf.teststudent.recycler.ListRecyclerAdapter;
import rs.etf.teststudent.recycler.RecyclerViewBinder;

public class TestsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = TestsFragment.class.getSimpleName();

    public static TestsFragment newInstance() {
        TestsFragment fragment = new TestsFragment();

        return fragment;
    }

    private SwipeRefreshLayout swipeLayout;
    private ListRecyclerAdapter<QrCodePayload> recyclerAdapter;
    private RecyclerView recyclerView;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadThemes();
        }
    };

    private boolean unsubscribeAllRequested = false;
    private String unsubscribeTopic = null;
    private QrCodePayload activeClass = null;
    private ProgressDialog progress;

    private BroadcastReceiver mqttUnsubscriberListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttUnsubscriberListener received message");

                String topic = intent.getStringExtra("MQTT_TOPIC");
                boolean status = intent.getBooleanExtra("STATUS", false);
                if (unsubscribeTopic != null && unsubscribeTopic.equals(topic) && unsubscribeAllRequested) {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                    if (status) {
                        Log.d(TAG, "Unsubscribed");

                        if (unsubscribeTopic.equals("etf/#")) {
                            MySharedPreferences.getInstance(getActivity().getApplication()).deleteActiveThemes();
                        } else {
                            MySharedPreferences.getInstance(getActivity().getApplication()).removeActiveClass(activeClass);
                        }

                        handler.post(refreshRunnable);
                    } else {
                        Toast.makeText(getActivity(), R.string.unsubscribing_failed_message, Toast.LENGTH_SHORT).show();
                    }

                    activeClass = null;
                    unsubscribeTopic = null;
                    unsubscribeAllRequested = false;
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.unsubscribing_failed_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.recyclerAdapter = new ListRecyclerAdapter<>(new ActiveThemesItemHolderFactory(),
                viewBinder);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_UNSUBSCRIBE_FILTER);
        getActivity().registerReceiver(mqttUnsubscriberListener, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_themes, container, false);

        swipeLayout = view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        Button unsubscribeAllButton = view.findViewById(R.id.unsubscribe_button);
        unsubscribeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (unsubscribeAllRequested) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_dialog_unsubscribe_title)
                        .setMessage(R.string.alert_dialog_unsubscribe_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    Log.d(TAG, "Sending unsubscribe request");
                                    Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
                                    intent.setAction(MqttService.ACTION_UNSUBSCRIBE);
                                    intent.putExtra("TOPIC", "etf/#");
                                    getActivity().startService(intent);
                                    Log.d(TAG, "Unsubscribe request sent");

                                    unsubscribeTopic = "etf/#";
                                    unsubscribeAllRequested = true;

                                    progress = new ProgressDialog(getActivity());
                                    progress.setMessage(getString(R.string.unsubscribing_message));
                                    progress.show();

                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to unsubscribe to all topic", e);
                                    Toast.makeText(getActivity(), R.string.unsubscribing_failed_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, null);

                builder.show();

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThemes();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mqttUnsubscriberListener);

        super.onDestroy();
    }

    private final RecyclerViewBinder<QrCodePayload> viewBinder = new
            RecyclerViewBinder<QrCodePayload>() {
                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                             QrCodePayload item, int position) {
                    if (holder instanceof ActiveThemesItemHolderFactory.ItemViewHolder) {
                        //show item
                        onBindItem((ActiveThemesItemHolderFactory.ItemViewHolder) holder, item, position);

                    } else if (holder instanceof ActiveThemesItemHolderFactory.EmptyViewHolder) {
                        //show empty view (no apps available for install)
                        ActiveThemesItemHolderFactory.EmptyViewHolder emptyViewHolder =
                                (ActiveThemesItemHolderFactory.EmptyViewHolder) holder;
                        onBindEmptyView(emptyViewHolder);
                    }
                }

            };

    private void onBindItem(final ActiveThemesItemHolderFactory.ItemViewHolder holder,
                            final QrCodePayload item, final int position) {

        holder.course.setText(item.getCourse());
        holder.testName.setText(item.getTestName());
        holder.date.setText(item.getDate());

        holder.classInfoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: display info
            }
        });

        holder.unsubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_dialog_unsubscribe_single_title)
                        .setMessage(R.string.alert_dialog_unsubscribe_single_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    Log.d(TAG, "Sending unsubscribe request");
                                    Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
                                    intent.setAction(MqttService.ACTION_UNSUBSCRIBE);
                                    intent.putExtra("TOPIC", item.getMqttThemeQ());
                                    getActivity().startService(intent);
                                    Log.d(TAG, "Unsubscribe request sent");

                                    activeClass = item;
                                    unsubscribeTopic = item.getMqttThemeQ();
                                    unsubscribeAllRequested = true;

                                    progress = new ProgressDialog(getActivity());
                                    progress.setMessage(getString(R.string.unsubscribing_message));
                                    progress.show();

                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to unsubscribe to all topic", e);
                                    Toast.makeText(getActivity(), R.string.unsubscribing_failed_message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, null);

                builder.show();
            }
        });
    }

    private void onBindEmptyView(ActiveThemesItemHolderFactory.EmptyViewHolder emptyViewHolder) {
        emptyViewHolder.title.setText(R.string.active_themes_empty_list_message);
    }

    @Override
    public void onRefresh() {
        loadThemes();

        swipeLayout.setRefreshing(false);
    }

    private void loadThemes() {
        try {
            List<QrCodePayload> list = MySharedPreferences.getInstance(getActivity().getApplication()).getActiveClasses();
            Log.d(TAG, "Saved themes: " + list);
            recyclerAdapter.setList(list);
            recyclerAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Failed to load questions", e);
        }
    }
}

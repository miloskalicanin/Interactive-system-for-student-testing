package rs.etf.teststudent.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.GetSentQuestionsDTO;
import rs.etf.teststudent.dto.MqttQuestionDTO;
import rs.etf.teststudent.dto.QrCodePayload;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.recycler.ListRecyclerAdapter;
import rs.etf.teststudent.recycler.QuestionsItemHolderFactory;
import rs.etf.teststudent.recycler.RecyclerViewBinder;

public class QuestionsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BackListener {
    private static final String TAG = QuestionsFragment.class.getSimpleName();

    public static QuestionsFragment newInstance(String email) {
        QuestionsFragment fragment = new QuestionsFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);

        return fragment;
    }

    private String email;

    private ProgressDialog progress;

    private SwipeRefreshLayout swipeLayout;
    private ListRecyclerAdapter<MqttQuestionDTO> recyclerAdapter;
    private RecyclerView recyclerView;
    private FrameLayout questionInfoFrame;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshQuestionsRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                loadQuestions();
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
            } catch (Exception e) {

            }
        }
    };


    private BroadcastReceiver mqttQuestionsListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttQuestionsListener received question, refresh after 1 second");

                handler.postDelayed(refreshQuestionsRunnable, 1000);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse question", e);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.recyclerAdapter = new ListRecyclerAdapter<>(new QuestionsItemHolderFactory(),
                viewBinder);

        Bundle bundle = getArguments();
        if (bundle != null) {
            email = bundle.getString("email");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_MESSAGES_CALLBACK_FILTER);
        getActivity().registerReceiver(mqttQuestionsListener, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_questions, container, false);

        swipeLayout = view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        questionInfoFrame = view.findViewById(R.id.question_info_container);

        recyclerView = view.findViewById(R.id.item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        questionInfoFrame.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        new GetQuestionsTask().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuestions();
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mqttQuestionsListener);

        super.onDestroy();
    }

    private final RecyclerViewBinder<MqttQuestionDTO> viewBinder = new
            RecyclerViewBinder<MqttQuestionDTO>() {
                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                             MqttQuestionDTO item, int position) {
                    if (holder instanceof QuestionsItemHolderFactory.ItemViewHolder) {
                        //show item
                        onBindItem((QuestionsItemHolderFactory.ItemViewHolder) holder, item, position);

                    } else if (holder instanceof QuestionsItemHolderFactory.EmptyViewHolder) {
                        //show empty view (no apps available for install)
                        QuestionsItemHolderFactory.EmptyViewHolder emptyViewHolder =
                                (QuestionsItemHolderFactory.EmptyViewHolder) holder;
                        onBindEmptyView(emptyViewHolder);
                    }
                }

            };

    private void onBindItem(final QuestionsItemHolderFactory.ItemViewHolder holder,
                            final MqttQuestionDTO item, final int position) {

        holder.testName.setText(item.getTestName());
        holder.question.setText(item.getQuestion());
        holder.type.setText(item.getType().name());

        holder.questionInfoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQuestionInfo(item);
            }
        });


        holder.deleteQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress = new ProgressDialog(getActivity());
                progress.setMessage(getString(R.string.deleting_question_message));
                progress.show();
                try {
                    MySharedPreferences.getInstance(getActivity().getApplication()).removeQuestion(item);

                    handler.post(refreshQuestionsRunnable);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to delete qustion", e);
                    Toast.makeText(getActivity(), R.string.delete_question_failed, Toast.LENGTH_SHORT).show();
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }
                }
            }
        });
    }

    private void onBindEmptyView(QuestionsItemHolderFactory.EmptyViewHolder emptyViewHolder) {
        emptyViewHolder.title.setText(R.string.questions_empty_list_message);
    }

    @Override
    public void onRefresh() {
        loadQuestions();

        new GetQuestionsTask().execute();

        swipeLayout.setRefreshing(false);
    }

    private void openQuestionInfo(MqttQuestionDTO question) {
        try {
            QuestionInfoFragment fragment = QuestionInfoFragment.newInstance(email, question);

            FragmentManager fragmentManager = getChildFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.question_info_container, fragment);
            transaction.commit();

            questionInfoFrame.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            swipeLayout.setEnabled(false);

            fragment.setListener(this);
        } catch (Exception e) {

        }
    }

    @Override
    public void backAction() {
        questionInfoFrame.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        swipeLayout.setEnabled(true);

        handler.post(refreshQuestionsRunnable);
    }

    private void loadQuestions() {
        try {
            List<MqttQuestionDTO> questions = MySharedPreferences.getInstance(getActivity().getApplication()).getQuestions();
            Log.d(TAG, "Saved questions: " + questions);
            recyclerAdapter.setList(questions);
            recyclerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load questions", e);
        }
    }


    private class GetQuestionsTask extends AsyncTask<Void, Void, Void> {

        GetSentQuestionsDTO request = new GetSentQuestionsDTO();

        public GetQuestionsTask() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d(TAG, "Sending get questions request");

                request.setEmail(email);

                List<String> mqttThemesQ = new ArrayList<>();

                MySharedPreferences preferences = MySharedPreferences.getInstance(getActivity().getApplication());
                List<QrCodePayload> classes = preferences.getActiveClasses();
                if (classes != null) {
                    for (QrCodePayload c : classes) {
                        mqttThemesQ.add(c.getMqttThemeQ());
                    }
                }
                request.setMqttThemesQ(mqttThemesQ);

                request = NetworkServicePrivate.getInstance(getActivity().getApplicationContext()).getSentQuestions(request);

                preferences.deleteQuestions();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to get questions", ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (request != null && request.getQuestions() != null && !request.getQuestions().isEmpty()) {
                MySharedPreferences.getInstance(getActivity().getApplication()).addQuestions(request.getQuestions());
            }
            handler.post(refreshQuestionsRunnable);
        }
    }

}

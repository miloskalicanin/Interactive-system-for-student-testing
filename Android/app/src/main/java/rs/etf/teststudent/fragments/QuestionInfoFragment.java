package rs.etf.teststudent.fragments;

import android.app.Activity;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import rs.etf.teststudent.MqttService;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.AnswerDTO;
import rs.etf.teststudent.dto.MqttQuestionDTO;
import rs.etf.teststudent.dto.TipPitanja;
import rs.etf.teststudent.dto.UserAnswerDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.recycler.AnswersItemHolderFactory;
import rs.etf.teststudent.recycler.ListRecyclerAdapter;
import rs.etf.teststudent.recycler.RecyclerViewBinder;

public class QuestionInfoFragment extends Fragment {
    private static final String TAG = QuestionInfoFragment.class.getSimpleName();

    public static final String[] letters = {
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"
    };

    public static QuestionInfoFragment newInstance(String email, MqttQuestionDTO question) {
        QuestionInfoFragment fragment = new QuestionInfoFragment();

        Bundle args = new Bundle();
        args.putSerializable("question", question);
        args.putString("email", email);
        fragment.setArguments(args);

        return fragment;
    }


    private ProgressDialog progress;

    private MqttQuestionDTO questionDTO;
    private String email;
    private BackListener listener;

    private TextView question;
    private ListRecyclerAdapter<AnswerDTO> recyclerAdapter;
    private RecyclerView recyclerView;
    private EditText inputAnswer;
    private Button sendAnswerButton;
    private int selectedPosition = 0;


    private String answerTopic = null;

    private BroadcastReceiver mqttPublishListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG, "mqttPublishListener received message");

                String topic = intent.getStringExtra("MQTT_TOPIC");
                boolean status = intent.getBooleanExtra("STATUS", false);
                if (answerTopic != null && answerTopic.equals(topic)) {
                    if (progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                    if (status) {
                        Log.d(TAG, "Answer sent successfully");

                        MySharedPreferences.getInstance(getActivity().getApplication()).removeQuestion(questionDTO);

                        Toast.makeText(getActivity(), R.string.sending_answer_succ_message, Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.backAction();
                        } else {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.sending_answer_failed_message, Toast.LENGTH_SHORT).show();
                    }

                    answerTopic = null;
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.sending_answer_failed_message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.recyclerAdapter = new ListRecyclerAdapter<>(new AnswersItemHolderFactory(),
                viewBinder);

        Bundle bundle = getArguments();
        if (bundle != null) {
            questionDTO = (MqttQuestionDTO) bundle.getSerializable("question");
            email = bundle.getString("email");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttService.MQTT_PUBLISH_FILTER);
        getActivity().registerReceiver(mqttPublishListener, filter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_question_info, container, false);

        question = view.findViewById(R.id.question);

        recyclerView = view.findViewById(R.id.item_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        inputAnswer = view.findViewById(R.id.input_answer);
        inputAnswer.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (listener != null) {
                        listener.backAction();
                    }
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.alert_dialog_send_answer_title)
                            .setMessage(R.string.alert_dialog_send_answer_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new SendAnswerTask().execute();
                                }
                            })
                            .setNegativeButton(R.string.no, null);

                    builder.show();
                    try {
                        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            im.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    } catch (Exception e) {

                    }
                    return true;
                }
                return false;
            }
        });

        sendAnswerButton = view.findViewById(R.id.send_answer_button);
        sendAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_dialog_send_answer_title)
                        .setMessage(R.string.alert_dialog_send_answer_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                new SendAnswerTask().execute();
                            }
                        })
                        .setNegativeButton(R.string.no, null);

                builder.show();
            }
        });

        try {
            question.setText(questionDTO.getQuestion());
            question.setMovementMethod(new ScrollingMovementMethod());

            if (questionDTO.getType().equals(TipPitanja.ZAOKRUZI)) {
                inputAnswer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                List<AnswerDTO> answers = questionDTO.getAnswers();
                if (answers != null) {
                    recyclerAdapter.setList(answers);
                    recyclerAdapter.notifyDataSetChanged();
                }
            } else {
                inputAnswer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // if questionDTO is null - not loaded
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (listener != null) {
                        listener.backAction();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mqttPublishListener);

        super.onDestroy();
    }

    private final RecyclerViewBinder<AnswerDTO> viewBinder = new
            RecyclerViewBinder<AnswerDTO>() {
                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                             AnswerDTO item, int position) {
                    if (holder instanceof AnswersItemHolderFactory.ItemViewHolder) {
                        //show item
                        onBindItem((AnswersItemHolderFactory.ItemViewHolder) holder, item, position);

                    } else if (holder instanceof AnswersItemHolderFactory.EmptyViewHolder) {
                        //show empty view (no apps available for install)
                        AnswersItemHolderFactory.EmptyViewHolder emptyViewHolder =
                                (AnswersItemHolderFactory.EmptyViewHolder) holder;
                        onBindEmptyView(emptyViewHolder);
                    }
                }
            };

    private void onBindItem(final AnswersItemHolderFactory.ItemViewHolder holder,
                            final AnswerDTO item, final int position) {
        holder.letter.setText(letters[position] + ":");
        holder.answer.setText(item.getAnswer());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                recyclerAdapter.notifyDataSetChanged();
            }
        });

        holder.radioButton.setChecked(position == selectedPosition);
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = position;
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onBindEmptyView(AnswersItemHolderFactory.EmptyViewHolder emptyViewHolder) {
        emptyViewHolder.title.setText(R.string.questions_answer_list_message);
    }

    public void setListener(BackListener listener) {
        this.listener = listener;
    }

    private class SendAnswerTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = SendAnswerTask.class.getSimpleName();

        boolean successful = false;

        public SendAnswerTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progress = new ProgressDialog(getActivity());
                progress.setMessage(getString(R.string.sending_answer_progress_dialog));
                progress.show();
            } catch (Exception e) {
                SendAnswerTask.this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String topic = questionDTO.getMqttThemeA();
                String message;
                UserAnswerDTO userAnswer = new UserAnswerDTO();
                userAnswer.setEmail(email);

                if (questionDTO.getType().equals(TipPitanja.ZAOKRUZI)) {
                    AnswerDTO answerDTO = questionDTO.getAnswers().get(selectedPosition);

                    message = new ObjectMapper().writeValueAsString(answerDTO);

                    userAnswer.setAnswer(answerDTO);
                } else {
                    String inputText = inputAnswer.getText().toString();

                    AnswerDTO answerDTO = new AnswerDTO();
                    answerDTO.setAnswer(inputText);
                    answerDTO.setIdQuestion(questionDTO.getId());

                    message = new ObjectMapper().writeValueAsString(answerDTO);

                    userAnswer.setAnswer(answerDTO);
                }

                Log.d(TAG, "Sending answer to server");
                NetworkServicePrivate.getInstance(getActivity().getApplicationContext()).addAnswer(userAnswer);
                Log.d(TAG, "Answer sent to server");

                Log.d(TAG, "Sending answer request: " + message);
                Intent intent = new Intent(getActivity().getApplicationContext(), MqttService.class);
                intent.setAction(MqttService.ACTION_PUBLISH);
                intent.putExtra("TOPIC", topic);
                intent.putExtra("MESSAGE", message);
                getActivity().startService(intent);
                Log.d(TAG, "Answer request sent");

                successful = true;
                answerTopic = topic;
            } catch (Exception e) {
                Log.e(TAG, "Failed to send answer", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (getActivity() == null || isDetached() || !isAdded()) {
                return;
            }

            if (!successful) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                Toast.makeText(getActivity(), R.string.sending_answer_failed_message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

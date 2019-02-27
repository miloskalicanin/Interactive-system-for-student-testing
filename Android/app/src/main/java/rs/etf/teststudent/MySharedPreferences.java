package rs.etf.teststudent;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import rs.etf.teststudent.dto.MqttQuestionDTO;
import rs.etf.teststudent.dto.QrCodePayload;

public class MySharedPreferences {
    private static final String TAG = MySharedPreferences.class.getSimpleName();

    private static final String FILE_NAME = "MySharedPreferences";
    private static final String USER_NAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String ACTIVE_THEMES_KEY = "ACTIVE_THEMES_KEY";
    private static final String QUESTIONS_KEY = "QUESTIONS_KEY";

    private static MySharedPreferences keypSharedPreferences = null;

    public static MySharedPreferences getInstance(Application application) {
        if (keypSharedPreferences == null) {
            keypSharedPreferences = new MySharedPreferences(application);
        }

        return keypSharedPreferences;
    }

    private SharedPreferences preferences;

    private MySharedPreferences(Application application) {
        preferences = application.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void deleteData() {
        Log.d(TAG, "Deleting data");
        preferences.edit().clear().apply();
        Log.d(TAG, "Data deleted");
    }

    public synchronized void deleteUsernameAndPassword() {
        Log.d(TAG, "Deleting username and password");
        preferences.edit().putString(USER_NAME_KEY, "").putString(PASSWORD_KEY, "").apply();
        Log.d(TAG, "Username and password deleted");
    }

    public synchronized void deleteActiveThemes() {
        Log.d(TAG, "Deleting active themes");
        preferences.edit().putString(ACTIVE_THEMES_KEY, "").apply();
        Log.d(TAG, "Active themes deleted");
    }

    public synchronized void deleteQuestions() {
        Log.d(TAG, "Deleting questions");
        preferences.edit().putString(QUESTIONS_KEY, "").apply();
        Log.d(TAG, "Questions deleted");
    }


    public synchronized void saveUsernameAndPassword(String username, String password) {
        Log.d(TAG, "Saving username and password");
        preferences.edit().putString(USER_NAME_KEY, username).putString(PASSWORD_KEY, password).apply();
        Log.d(TAG, "Username and password saved");
    }

    public synchronized String getUsername() {
        return preferences.getString(USER_NAME_KEY, null);
    }

    public synchronized String getPassword() {
        return preferences.getString(PASSWORD_KEY, null);
    }

    public synchronized boolean addActiveClass(QrCodePayload payload) {
        try {
            Log.d(TAG, "Adding active test: " + payload);
            List<QrCodePayload> listOfActive = null;
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(ACTIVE_THEMES_KEY, null);

            try {
                listOfActive = mapper.readValue(jsonList, new TypeReference<List<QrCodePayload>>() {
                });
            } catch (Exception e) {
                listOfActive = new ArrayList<>();
            }

            for (QrCodePayload saved : listOfActive) {
                if (saved.getMqttThemeQ().equals(payload.getMqttThemeQ())) {
                    Log.d(TAG, "Already added");
                    return true;
                }
            }

            listOfActive.add(payload);
            jsonList = mapper.writeValueAsString(listOfActive);

            preferences.edit().putString(ACTIVE_THEMES_KEY, jsonList).apply();

            Log.d(TAG, "Adding active test finished");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to add active test", e);
            return false;
        }
    }

    public synchronized boolean removeActiveClass(QrCodePayload payload) {
        try {
            Log.d(TAG, "Removing active test: " + payload);
            List<QrCodePayload> listOfActive = null;
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(ACTIVE_THEMES_KEY, null);

            try {
                listOfActive = mapper.readValue(jsonList, new TypeReference<List<QrCodePayload>>() {
                });
            } catch (Exception e) {
                listOfActive = new ArrayList<>();
            }

            for (QrCodePayload saved : listOfActive) {
                if (saved.getMqttThemeQ().equals(payload.getMqttThemeQ())) {
                    listOfActive.remove(saved);

                    jsonList = mapper.writeValueAsString(listOfActive);

                    preferences.edit().putString(ACTIVE_THEMES_KEY, jsonList).apply();

                    Log.d(TAG, "Test removed");

                    return true;
                }
            }

            Log.d(TAG, "Failed to remove test");

            return false; // not found
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove test", e);
            return false;
        }
    }

    public synchronized List<QrCodePayload> getActiveClasses() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(ACTIVE_THEMES_KEY, null);

            return mapper.readValue(jsonList, new TypeReference<List<QrCodePayload>>() {
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to get tests", e);
            return null;
        }
    }

    public synchronized boolean addQuestion(MqttQuestionDTO payload) {
        try {
            Log.d(TAG, "Adding question: " + payload);
            List<MqttQuestionDTO> savedQuestions = null;
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(QUESTIONS_KEY, null);

            try {
                savedQuestions = mapper.readValue(jsonList, new TypeReference<List<MqttQuestionDTO>>() {
                });
            } catch (Exception e) {
                savedQuestions = new ArrayList<>();
            }

            for (MqttQuestionDTO saved : savedQuestions) {
                if (saved.getId().equals(payload.getId())) {
                    Log.d(TAG, "Question already added");
                    return true;
                }
            }

            savedQuestions.add(payload);
            jsonList = mapper.writeValueAsString(savedQuestions);

            preferences.edit().putString(QUESTIONS_KEY, jsonList).apply();

            Log.d(TAG, "Question added");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to add question", e);
            return false;
        }
    }

    public synchronized boolean addQuestions(List<MqttQuestionDTO> questions) {
        try {
            Log.d(TAG, "Adding questions: " + questions);
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = mapper.writeValueAsString(questions);

            preferences.edit().putString(QUESTIONS_KEY, jsonList).apply();

            Log.d(TAG, "Questions added");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to add questions", e);
            return false;
        }
    }

    public synchronized boolean removeQuestion(MqttQuestionDTO payload) {
        try {
            Log.d(TAG, "Removing question: " + payload);
            List<MqttQuestionDTO> savedQuestions = null;
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(QUESTIONS_KEY, null);

            try {
                savedQuestions = mapper.readValue(jsonList, new TypeReference<List<MqttQuestionDTO>>() {
                });
            } catch (Exception e) {
                savedQuestions = new ArrayList<>();
            }

            for (MqttQuestionDTO saved : savedQuestions) {
                if (saved.getId().equals(payload.getId())) {
                    savedQuestions.remove(saved);

                    jsonList = mapper.writeValueAsString(savedQuestions);

                    preferences.edit().putString(QUESTIONS_KEY, jsonList).apply();

                    Log.d(TAG, "Question removed");

                    return true;
                }
            }

            Log.d(TAG, "Failed to remove question");
            return false; // not found
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove question", e);
            return false;
        }
    }

    public synchronized List<MqttQuestionDTO> getQuestions() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String jsonList = preferences.getString(QUESTIONS_KEY, null);

            return mapper.readValue(jsonList, new TypeReference<List<MqttQuestionDTO>>() {
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to get questions", e);
            return null;
        }
    }


}

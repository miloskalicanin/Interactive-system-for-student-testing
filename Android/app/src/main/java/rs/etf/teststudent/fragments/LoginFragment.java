package rs.etf.teststudent.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.jce.provider.X509CertificateObject;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import rs.etf.teststudent.Configuration;
import rs.etf.teststudent.CryptoUtils;
import rs.etf.teststudent.MainActivity;
import rs.etf.teststudent.MySharedPreferences;
import rs.etf.teststudent.R;
import rs.etf.teststudent.dto.CertRequestDTO;
import rs.etf.teststudent.dto.CertResponseDTO;
import rs.etf.teststudent.dto.LoginDataDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.network.NetworkServicePublic;
import rs.etf.teststudent.network.NetworkUtils;

public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private EditText email;
    private EditText password;
    private CheckBox checkBox;
    private String loggedInEmail = null;
    private boolean loggedIn = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        checkBox = view.findViewById(R.id.remember_account);

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        loadAccountData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loggedIn) {
            MainActivity activity = (MainActivity) getActivity();
            activity.openMainFragment(loggedInEmail, 1);
        }
    }

    private void loadAccountData() {
        Log.d(TAG, "Loading account");

        MySharedPreferences preferences = MySharedPreferences.getInstance(getActivity().getApplication());

        String savedEmail = preferences.getUsername();
        String savedPassword = preferences.getPassword();
        if (savedEmail == null || savedEmail.isEmpty() || savedPassword == null || savedPassword.isEmpty()) {
            return;
        }

        email.setText(savedEmail, TextView.BufferType.EDITABLE);
        password.setText(savedPassword, TextView.BufferType.EDITABLE);

        checkBox.setChecked(true);

        Log.d(TAG, "Account loaded: email=" + savedEmail + "; password=" + savedPassword + ";");

        login();
    }

    public void login() {
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        try {
            KeyStore keyStore = NetworkUtils.loadKeyStore(getActivity().getApplicationContext());
            KeyStore.Entry entry = keyStore.getEntry(Configuration.KEYSTORE_CERT_ENTRY, new KeyStore.PasswordProtection(Configuration.KEYSTORE_PASSWORD.toCharArray()));
            if (entry != null) {
                X509Certificate cert = (X509Certificate) keyStore.getCertificateChain(Configuration.KEYSTORE_CERT_ENTRY)[0];
                try {
                    cert.checkValidity();
                } catch (Exception e) {
                    entry = null;
                }
            }

            if (entry == null) {
                RegisterTask task = new RegisterTask(keyStore, emailText, passwordText);
                task.execute();
            } else {
                Log.d(TAG, "Already registered");
                Log.d(TAG, "Cert chain:");
                for (Certificate cert : keyStore.getCertificateChain(Configuration.KEYSTORE_CERT_ENTRY)) {
                    Log.d(TAG, cert.toString());
                }
                sendLoginRequest(emailText, passwordText);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.login_failed_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendLoginRequest(String email, String password) {
        LoginTask task = new LoginTask(email, password);
        task.execute();
    }

    private void saveAccount(String email, String password) {
        if (email != null && password != null && !email.isEmpty() && !password.isEmpty()) {
            Log.d(TAG, "Saving account");

            MySharedPreferences preferences = MySharedPreferences.getInstance(getActivity().getApplication());
            preferences.saveUsernameAndPassword(email, password);

            Log.d(TAG, "Account saved");
        }
    }

    private void deleteAccounts() {
        Log.d(TAG, "Deleting accounts");

        MySharedPreferences preferences = MySharedPreferences.getInstance(getActivity().getApplication());
        preferences.deleteUsernameAndPassword();

        Log.d(TAG, "Accounts deleted");
    }


    private class RegisterTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;

        private final KeyStore keyStore;
        private final String email;
        private final String password;

        private KeyPair pair;
        private CertResponseDTO response = null;

        public RegisterTask(KeyStore keyStore, String email, String password) {
            this.keyStore = keyStore;
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progress = new ProgressDialog(getActivity());

                progress.setMessage(getString(R.string.register_progress_dialog));

                progress.show();
                progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        RegisterTask.this.cancel(true);
                    }
                });
            } catch (Exception e) {
                RegisterTask.this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Context context = getActivity().getApplicationContext();
                Log.d(TAG, "Starting registration");

                pair = CryptoUtils.createEcKeyPair(Configuration.KEY_PAIR_EC_ALGORITHM);

                //generate CSR request
                X500Principal subject = new X500Principal("C=RS, O=etf, OU=teststudent, CN=" + email);
                String signatureAlgorithm = "SHA256withECDSA";

                PKCS10CertificationRequest csr = CryptoUtils.createCsr(pair, subject, signatureAlgorithm);

                CertRequestDTO request = new CertRequestDTO();
                request.setEmail(email);
                request.setCsr(CryptoUtils.csrBytes(csr));

                response = NetworkServicePublic.getInstance().register(request);

                X509CertificateHolder clientCertificate = CryptoUtils.decodeCertificate(response.getCertificate());
                X509Certificate[] serverChain = new X509Certificate[]{
                        new X509CertificateObject(clientCertificate.toASN1Structure())
                };

                Log.d(TAG, "Cert chain:");
                for (Certificate cert : serverChain) {
                    Log.d(TAG, cert.toString());
                }

                keyStore.setEntry(
                        Configuration.KEYSTORE_CERT_ENTRY,
                        new KeyStore.PrivateKeyEntry(pair.getPrivate(), serverChain),
                        new KeyStore.PasswordProtection(Configuration.KEY_PASSWORD.toCharArray())
                );

                NetworkUtils.storeKeyStore(context);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to register", ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (response == null) {
                Toast.makeText(getActivity(), R.string.login_failed_message, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Log.d(TAG, "Registration completed");

                    sendLoginRequest(email, password);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to save certificate", ex);
                    Toast.makeText(getActivity(), R.string.login_failed_message, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            if (progress.isShowing()) {
                progress.dismiss();
            }
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;

        private String email;
        private String password;

        public LoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progress = new ProgressDialog(getActivity());

                progress.setMessage(getString(R.string.login_progress_dialog));

                progress.show();
                progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        LoginTask.this.cancel(true);
                    }
                });
            } catch (Exception e) {
                LoginTask.this.cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loggedIn = false;
            try {
                Log.d(TAG, "Sending login request");

                LoginDataDTO request = new LoginDataDTO();
                request.setEmail(email);
                request.setPassword(password);

                NetworkServicePrivate.getInstance(getActivity().getApplicationContext()).login(request);

                loggedIn = true;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to log in", ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            if (loggedIn) {
                Log.d(TAG, "Logged in");
                loggedInEmail = email;

                try {
                    if (checkBox.isChecked()) {
                        saveAccount(email, password);
                    } else {
                        deleteAccounts();
                    }

                    MainActivity activity = (MainActivity) getActivity();
                    activity.openMainFragment(email, 3);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to finish login", e);
                }
            } else {
                Toast.makeText(getActivity(), R.string.login_failed_message, Toast.LENGTH_SHORT).show();
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

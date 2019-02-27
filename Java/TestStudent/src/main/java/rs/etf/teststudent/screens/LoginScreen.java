package rs.etf.teststudent.screens;

import java.awt.*;
import java.awt.event.*;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import javax.swing.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.*;
import rs.etf.teststudent.network.*;
import rs.etf.teststudent.utils.*;

public class LoginScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(LoginScreen.class);

    private JLabel labelEmail;
    private JTextField inputEmail;
    private JLabel labelPassword;
    private JPasswordField inputPassword;
    private JCheckBox checkBox;
    private JButton loginButton;

    private boolean inProgress = false;

    public LoginScreen() {
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.LOGIN_SCREEN_SUFFIX);

        labelEmail = new JLabel("Email:");
        inputEmail = new JTextField();
        labelPassword = new JLabel("Sifra:");
        inputPassword = new JPasswordField();

        checkBox = new JCheckBox("Zapamti", false);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        try {
            LoginData data = Encryptor.loadLoginData();
            if (data != null) {
                inputEmail.setText(data.getEmail());
                inputPassword.setText(data.getPassword());
                checkBox.setSelected(true);
            }
        } catch (Exception e) {
            logger.error("Failed to load user data", e);
        }

        add(labelEmail);
        add(inputEmail);
        add(labelPassword);
        add(inputPassword);
        add(checkBox);
        add(loginButton);

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        };

        inputEmail.addActionListener(action);
        inputPassword.addActionListener(action);

        setMinimumSize(new Dimension(500, 200));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                setBounds();
            }
        });

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setBounds() {
        int width = getWidth();
        int height = getHeight();

        int startX = (width - 400) / 2;
        int startY = (height - 150) / 2;

        labelEmail.setBounds(startX, startY, 100, 30);
        inputEmail.setBounds(startX + 100, startY, 300, 30);

        labelPassword.setBounds(startX, startY + 50, 100, 30);
        inputPassword.setBounds(startX + 100, startY + 50, 300, 30);

        loginButton.setBounds(startX + 100, startY + 100, 100, 30);

        checkBox.setBounds(startX + 250, startY + 100, 100, 30);
    }

    private void login() {
        if (inProgress) {
            return;
        }
        inProgress = true;
        try {
            String email = inputEmail.getText();
            String password = String.valueOf(inputPassword.getPassword());

            KeyStore keyStore = NetworkUtils.loadKeyStore();
            Entry entry = keyStore.getEntry(Configuration.KEYSTORE_CERT_ENTRY, new KeyStore.PasswordProtection(Configuration.KEYSTORE_PASSWORD.toCharArray()));
            if (entry != null) {
                X509Certificate cert = (X509Certificate) keyStore.getCertificateChain(Configuration.KEYSTORE_CERT_ENTRY)[0];
                try {
                    cert.checkValidity();
                } catch (Exception e) {
                    entry = null;
                }
            }

            if (entry == null) {
                RegisterTask task = new RegisterTask(keyStore, email, password);
                task.execute();
            } else {
                logger.info("Already registered");
                logger.info("Cert chain:");
                for (Certificate cert : keyStore.getCertificateChain(Configuration.KEYSTORE_CERT_ENTRY)) {
                    logger.info(cert.toString());
                }
                sendLoginRequest(email, password);
            }
        } catch (Exception e) {
            logger.error("Failed to log in", e);
            displayFailedLoginMessage("Greska", "Logovanje nije uspesno, pokusajte ponovo");
        }
    }

    private void sendLoginRequest(String email, String password) {
        LoginTask task = new LoginTask(email, password);
        task.execute();
    }

    private void displayFailedLoginMessage(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
        inProgress = false;
    }

    private class RegisterTask extends SwingWorker<Void, String> {

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
        protected Void doInBackground() {
            try {
                logger.info("Starting registration");

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

                logger.info("Cert chain:");
                for (Certificate cert : serverChain) {
                    logger.info(cert.toString());
                }

                keyStore.setEntry(
                        Configuration.KEYSTORE_CERT_ENTRY,
                        new KeyStore.PrivateKeyEntry(pair.getPrivate(), serverChain),
                        new KeyStore.PasswordProtection(Configuration.KEY_PASSWORD.toCharArray())
                );

                NetworkUtils.storeKeyStore();
            } catch (Exception ex) {
                logger.error("Failed to register", ex);
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (response == null) {
                displayFailedLoginMessage("Greska", "Registracija nije uspesna, pokusajte ponovo");
            } else {
                try {
                    logger.info("Registration completed");

                    sendLoginRequest(email, password);
                } catch (Exception ex) {
                    logger.error("Failed to save certificate", ex);
                    displayFailedLoginMessage("Greska", "Registracija nije uspesna, pokusajte ponovo");
                }
            }
        }

    }

    private class LoginTask extends SwingWorker<Void, String> {

        private String email;
        private String password;
        private boolean loggedIn = false;

        public LoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Void doInBackground() {
            try {
                logger.info("Sending login request");

                LoginDataDTO request = new LoginDataDTO();
                request.setEmail(email);
                request.setPassword(password);

                NetworkServicePrivate.getInstance().login(request);

                try {
                    if (checkBox.isSelected()) {
                        LoginData data = new LoginData(email, password);
                        Encryptor.saveLoginData(data);
                    } else {
                        Encryptor.deleteLoginData();
                    }
                } catch (Exception e) {
                    logger.error("Failed to save user data", e);
                }

                loggedIn = true;
            } catch (Exception ex) {
                logger.error("Failed to log in", ex);
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (loggedIn) {
                logger.info("Logged in");
                dispose();
                new MainScreen(email);
            } else {
                displayFailedLoginMessage("Greska", "Logovanje nije uspesno, pokusajte ponovo");
            }
        }

    }

    public static void main(String[] args) {
        new LoginScreen();
    }
}

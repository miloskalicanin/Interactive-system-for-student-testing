/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.MqttQuestionDTO;
import rs.etf.teststudent.dto.QrCodePayload;
import rs.etf.teststudent.dto.QuestionDTO;
import rs.etf.teststudent.dto.TestDTO;
import rs.etf.teststudent.dto.TestInfoDTO;
import rs.etf.teststudent.dto.TipPitanja;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.network.NetworkUtils;
import rs.etf.teststudent.utils.Configuration;

public class TestInfoScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(TestInfoScreen.class);

    private String email;
    private TestDTO test;
    private TestInfoDTO testInfo;
    private TestsScreen testsScreen;
    private boolean active;

    public TestInfoScreen(String email, TestDTO test, TestInfoDTO testInfo, boolean active, TestsScreen testsScreen) {
        this.email = email;
        this.test = test;
        this.testInfo = testInfo;
        this.active = active;
        this.testsScreen = testsScreen;
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.TESTINFO_SCREEN_SUFFIX);

        setLayout(new BorderLayout());

        if (active) {
            activeTestInfo();
        } else {

            finishedTestInfo();
        }

        setMinimumSize(new Dimension(800, 300));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void reloadScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        if (active) {
            activeTestInfo();
        } else {
            finishedTestInfo();
        }

        validate();
    }

    private void activeTestInfo() {
        /*
            Top panel
         */
        JLabel labelSubject = new JLabel("Kurs: " + test.getCourseKey(), JLabel.CENTER);
        JLabel labelClassroom = new JLabel("Sala: " + test.getClassroom(), JLabel.CENTER);
        JLabel labelDate = new JLabel("Datum: " + test.getDate(), JLabel.CENTER);
        JLabel labelClassTheme = new JLabel("Tema: " + test.getTestName(), JLabel.CENTER);
        JLabel studentNum = new JLabel("Broj prisutnih: " + testInfo.getStudentNum(), JLabel.CENTER);

        JPanel topPanel = new JPanel(new GridLayout(1, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2));
        topPanel.add(labelSubject);
        topPanel.add(labelClassroom);
        topPanel.add(labelDate);
        topPanel.add(labelClassTheme);
        topPanel.add(studentNum);

        add(topPanel, BorderLayout.NORTH);

        /*
            Center panel
         */
        if (testInfo.getQuestions() != null && testInfo.getQuestions().size() != 0) {
            JPanel questionsPanel = new JPanel(new GridLayout(testInfo.getQuestions().size(), 1));
            questionsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            JPanel editQuestionGridPanel = new JPanel(new GridLayout(testInfo.getQuestions().size(), 1));
            //editQuestionPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            if (testInfo.getQuestions() != null) {
                for (int i = 0; i < testInfo.getQuestions().size(); i++) {
                    JPanel editQuestionPanel = new JPanel(new GridLayout(2, 3));
                    QuestionDTO q = testInfo.getQuestions().get(i);
                    String questionText;
                    if (q.getVisible() == null || q.getVisible() == false) {
                        questionText = "Pitanje " + (i + 1);
                    } else {
                        questionText = q.getQuestion();
                    }
                    JTextArea textArea = new JTextArea(questionText, 0, 2);
                    textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setOpaque(true);
                    textArea.setEditable(false);
                    if (q.isFinished()) {
                        textArea.setBackground(new Color(255, 0, 0, 25)); // red with transparency 10%
                    } else if (q.isSent()) {
                        textArea.setBackground(new Color(255, 255, 0, 25)); // yellow with transparency 10%
                    } else {
                        textArea.setBackground(new Color(0, 255, 0, 25)); // green with transparency 10%
                    }
                    questionsPanel.add(new JScrollPane(textArea));

                    JLabel label = new JLabel("Izmeni", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            changeQuestion(q);
                        }
                    });

                    label = new JLabel("Obrisi", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            deleteQuestion(q);
                        }
                    });

                    label = new JLabel("Prikazi/Sakri", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            showHide(q);
                        }
                    });

                    label = new JLabel("Posalji", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            sendQuestion(q);
                        }
                    });

                    label = new JLabel("Zavrsi", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            finishQuestion(q);
                        }
                    });

                    label = new JLabel("Detalji", JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
                    editQuestionPanel.add(label);
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            details(q);
                        }
                    });
                    editQuestionGridPanel.add(editQuestionPanel);
                }
            }

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(questionsPanel, BorderLayout.CENTER);
            centerPanel.add(editQuestionGridPanel, BorderLayout.EAST);

            add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        } else {
            JLabel label = new JLabel("Ne postoje pitanja za ovaj test", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }

        /*
            Bottom panel
         */
        JLabel labelQrCode = new JLabel("Preuzmi QR kod", JLabel.CENTER);
        labelQrCode.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                saveQrCodeImage();
            }
        });
        JLabel labelAddQuestion = new JLabel("Dodaj pitanje", JLabel.CENTER);
        labelAddQuestion.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                newQuestion();
            }
        });
        JLabel labelFinishTest = new JLabel("Zavrsi test", JLabel.CENTER);
        labelFinishTest.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                finishTest();
            }
        });

        JPanel bottomPanel = new JPanel(new GridLayout(1, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2));
        bottomPanel.add(new JLabel(""));
        bottomPanel.add(labelQrCode);
        bottomPanel.add(labelAddQuestion);
        bottomPanel.add(labelFinishTest);
        bottomPanel.add(new JLabel(""));

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void finishedTestInfo() {
        /*
            Top panel
         */
        JLabel labelSubject = new JLabel("Kurs: " + test.getCourseKey(), JLabel.CENTER);
        JLabel labelClassroom = new JLabel("Sala: " + test.getClassroom(), JLabel.CENTER);
        JLabel labelDate = new JLabel("Datum: " + test.getDate(), JLabel.CENTER);
        JLabel labelClassTheme = new JLabel("Tema: " + test.getTestName(), JLabel.CENTER);
        JLabel studentNum = new JLabel("Broj prisutnih: " + testInfo.getStudentNum(), JLabel.CENTER);

        JPanel topPanel = new JPanel(new GridLayout(1, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2));
        topPanel.add(labelSubject);
        topPanel.add(labelClassroom);
        topPanel.add(labelDate);
        topPanel.add(labelClassTheme);
        topPanel.add(studentNum);

        add(topPanel, BorderLayout.NORTH);

        /*
            Center panel
         */
        if (testInfo.getQuestions() != null && testInfo.getQuestions().size() != 0) {
            JPanel questionsPanel = new JPanel(new GridLayout(testInfo.getQuestions().size(), 1));
            questionsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            JPanel editQuestionPanel = new JPanel(new GridLayout(testInfo.getQuestions().size(), 1));
            editQuestionPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            for (QuestionDTO q : testInfo.getQuestions()) {
                JTextArea textArea = new JTextArea(q.getQuestion(), 0, 2);
                textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(false);
                textArea.setEditable(false);
                questionsPanel.add(new JScrollPane(textArea));

                JLabel label = new JLabel("Detalji", JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                editQuestionPanel.add(label);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        details(q);
                    }
                });
            }

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(questionsPanel, BorderLayout.CENTER);
            centerPanel.add(editQuestionPanel, BorderLayout.EAST);

            add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        } else {
            JLabel label = new JLabel("Ne postoje pitanja za ovaj test", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }

    private void displayMessage(String title, String message, int code) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                code);
    }

    private void saveQrCodeImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.png", "png"));
        fileChooser.setDialogTitle("Sacuvaj QR kod");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getName();
            if (fileName == null || fileName.isEmpty()) {
                displayMessage("Greska", "Morate uneti ime fajla", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String path = fileChooser.getSelectedFile().getAbsolutePath();
            logger.info("File path: " + path);

            if (path.endsWith(".png")) {
                logger.info("Has extendion");
            } else if (path.endsWith(".jpg") || path.endsWith(".bmp")) {
                displayMessage("Greska", "Ekstenzija fajla mora biti .png", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                if (path.endsWith(".")) {
                    path = path + "png";
                } else {
                    path = path + ".png";
                }
            }
            logger.info("File path: " + path);

            String qrCodeText = test.getMqttThemeQ();

            QrCodePayload qrCodePayload = new QrCodePayload();
            qrCodePayload.setCourse(test.getCourseKey());
            qrCodePayload.setTestName(test.getTestName());
            qrCodePayload.setClassroom(test.getClassroom());
            qrCodePayload.setDate(test.getDate());
            qrCodePayload.setMqttThemeQ(test.getMqttThemeQ());

            try {
                qrCodeText = new ObjectMapper().writeValueAsString(qrCodePayload);
            } catch (Exception ex) {
                logger.error("Failed to parse qr code object", ex);
            }

            int size = Configuration.QR_CODE_SIZE;
            String fileType = "png";
            File qrFile = new File(path);
            try {
                createQRImage(qrFile, qrCodeText, size, fileType);
            } catch (Exception e) {
                displayMessage("Greska", "Fajl ne moze da se sacuva", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void createQRImage(File qrFile, String qrCodeText, int size, String fileType)
            throws Exception {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }

    private void changeQuestion(QuestionDTO q) {
        new CreateQuestionScreen(email, test, q, this);
    }

    public void addQuestion(QuestionDTO oldQuestion, QuestionDTO newQuestion) {
        if (testInfo.getQuestions() == null) {
            testInfo.setQuestions(new ArrayList<>(1));
        }
        testInfo.getQuestions().remove(oldQuestion);
        testInfo.getQuestions().add(newQuestion);
        reloadScreen();
    }

    private void showHide(QuestionDTO q) {
        if (q.getVisible() == null || q.getVisible() == false) {
            q.setVisible(true);
        } else {
            q.setVisible(false);
        }

        reloadScreen();
    }

    private void details(QuestionDTO q) {
        if (q.getType().equals(TipPitanja.ZAOKRUZI)) {
            new QuestionDetailsGivenAScreen(email, test, q, !active);
        } else {
            new QuestionDetailsInputAScreen(email, test, q, !active);
        }
    }

    private void deleteQuestion(QuestionDTO q) {
        Object[] options = {"DA", "NE"};
        int dialogResult = JOptionPane.showOptionDialog(this, 
                "Da li ste sigurni da zelite da obrisete ovo pitanje?", 
                "Obrisi pitanje", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null,
                options,
                options[1]);
        if (dialogResult == JOptionPane.YES_OPTION) {
            DeleteQuestionsTask task = new DeleteQuestionsTask(q);
            task.execute();
        }
    }

    private void sendQuestion(QuestionDTO q) {
        if (!q.isSent()) {
            new SendQuestionTask(q).execute();
        } else {
            displayMessage("Slanje pitanja", "Pitanje je vec poslato", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void finishQuestion(QuestionDTO q) {
        if (!q.isFinished()) {
            new FinishQuestionTask(q).execute();
        } else {
            displayMessage("Zavrsi pitanje", "Pitanje je vec zavrseno", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void newQuestion() {
        new CreateQuestionScreen(email, test, null, this);
    }

    private void finishTest() {
        Object[] options = {"DA", "NE"};
        int dialogResult = JOptionPane.showOptionDialog(this, 
                "Da li ste sigurni da zelite da zavrsite test?", 
                "Zavrsi test", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null,
                options,
                options[1]);
        if (dialogResult == JOptionPane.YES_OPTION) {
            FinishTestTask task = new FinishTestTask(test);
            task.execute();
        }
    }

    private class DeleteQuestionsTask extends SwingWorker<Void, String> {

        private QuestionDTO question;
        private boolean status = false;
        private String errorMessage = null;

        public DeleteQuestionsTask(QuestionDTO question) {
            this.question = question;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Deleting question: " + question);

                NetworkServicePrivate.getInstance().deleteQuestion(question);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to delete question", ex);
                try {
                    Map<String, Object> map = new ObjectMapper().readValue(ex.getMessage().substring(ex.getMessage().indexOf('{')), new TypeReference<Map<String, Object>>() {
                    });
                    errorMessage = (String) map.get("message");
                } catch (Exception ex1) {
                    logger.error("Failed to parse message", ex1);
                    errorMessage = ex.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (status) {
                logger.info("Deleted question successfully");

                displayMessage("Brisanje pitanja", "Pitanje uspesno obrisano", JOptionPane.INFORMATION_MESSAGE);

                testInfo.getQuestions().remove(question);
                if (testInfo.getQuestions().size() == 0) {
                    dispose();
                }

                reloadScreen();
            } else {
                String message = "Brisanje pitanja nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class FinishTestTask extends SwingWorker<Void, String> {

        private TestDTO test;
        private boolean status = false;
        private String errorMessage = null;

        public FinishTestTask(TestDTO test) {
            this.test = test;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Finishing test: " + test);

                NetworkServicePrivate.getInstance().finishTest(test);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to finish test", ex);
                try {
                    Map<String, Object> map = new ObjectMapper().readValue(ex.getMessage().substring(ex.getMessage().indexOf('{')), new TypeReference<Map<String, Object>>() {
                    });
                    errorMessage = (String) map.get("message");
                } catch (Exception ex1) {
                    logger.error("Failed to parse message", ex1);
                    errorMessage = ex.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (status) {
                logger.info("Finish test successfully");

                displayMessage("Zavrsi test", "Test uspesno zavrsen", JOptionPane.INFORMATION_MESSAGE);
                testsScreen.finishTest(test);

                dispose();
            } else {
                String message = "Zavrsavanje testa nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class SendQuestionTask extends SwingWorker<Void, String> {

        private QuestionDTO q;

        public SendQuestionTask(QuestionDTO q) {
            this.q = q;
        }

        @Override
        protected Void doInBackground() throws Exception {
            MqttClient client = null;
            try {
                logger.info("Sending question: " + q);

                NetworkServicePrivate.getInstance().sendQuestion(q);
                q.setSent(true);

                String clientId = email;
                client = new MqttClient(Configuration.MQTT_BROKER_URL, clientId);

                MqttCallback callback = new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        logger.info("Delivery Complete");
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                displayMessage("Slanje pitanja", "Pitanje uspesno poslato", JOptionPane.INFORMATION_MESSAGE);
                                reloadScreen();
                            }
                        });
                    }
                };

                client.setCallback(callback);

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(false);
                options.setAutomaticReconnect(true);

                //options.setUserName(clientId);
                //options.setPassword(password.toCharArray());
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };

                KeyStore ks = NetworkUtils.loadKeyStore();
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, "".toCharArray());

                SSLContext sslContext = SSLContext.getInstance("TLSv1.1");
                sslContext.init(kmf.getKeyManagers(), trustAllCerts, null);
                options.setSocketFactory(sslContext.getSocketFactory());

                client.connect(options);

                final String topic = "etf/#";

                // Unsubscribe from all subtopics of etf
                client.unsubscribe(topic);

                // publish question to questions topic
                MqttQuestionDTO mqttQuestion = new MqttQuestionDTO();
                mqttQuestion.setId(q.getId());
                mqttQuestion.setIdTest(q.getIdTest());
                mqttQuestion.setQuestion(q.getQuestion());
                mqttQuestion.setType(q.getType());
                mqttQuestion.setAnswers(q.getAnswers());
                mqttQuestion.setTestName(test.getTestName());
                mqttQuestion.setMqttThemeA(test.getMqttThemeA());

                final MqttTopic sendQuestionTopic = client.getTopic(test.getMqttThemeQ());

                String message = new ObjectMapper().writeValueAsString(mqttQuestion);

                MqttMessage msg = new MqttMessage(message.getBytes());
                msg.setQos(2);
                MqttDeliveryToken token = sendQuestionTopic.publish(msg);
                token.waitForCompletion();
                Thread.sleep(100);
                client.disconnect();
            } catch (Exception ex) {
                logger.error("Failed to send question", ex);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayMessage("Slanje pitanja", "Slanje pitanja nije uspelo", JOptionPane.ERROR_MESSAGE);
                    }
                });

                try {
                    client.disconnect();
                } catch (Exception ex1) {

                }
            }
            return null;
        }
    }

    private class FinishQuestionTask extends SwingWorker<Void, String> {

        private QuestionDTO q;
        private boolean status = false;
        private String errorMessage = null;

        public FinishQuestionTask(QuestionDTO q) {
            this.q = q;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Finishing question: " + q);

                NetworkServicePrivate.getInstance().finishQuestion(q);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to finish question", ex);
                try {
                    Map<String, Object> map = new ObjectMapper().readValue(ex.getMessage().substring(ex.getMessage().indexOf('{')), new TypeReference<Map<String, Object>>() {
                    });
                    errorMessage = (String) map.get("message");
                } catch (Exception ex1) {
                    logger.error("Failed to parse message", ex1);
                    errorMessage = ex.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (status) {
                logger.info("Question finished");

                displayMessage("Zavrsi pitanje", "Pitanje je uspesno zavrseno", JOptionPane.INFORMATION_MESSAGE);
                q.setFinished(true);
                reloadScreen();
            } else {
                String message = "Zavrsavanje pitanja nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, JOptionPane.ERROR_MESSAGE);
            }
        }

    }
}

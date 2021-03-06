/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.AnswerDTO;
import rs.etf.teststudent.dto.QuestionDTO;
import rs.etf.teststudent.dto.TestDTO;
import rs.etf.teststudent.network.NetworkUtils;
import rs.etf.teststudent.utils.Configuration;

public class QuestionDetailsInputAScreen extends JFrame implements MqttCallback {

    Logger logger = LoggerFactory.getLogger(QuestionDetailsInputAScreen.class);

    private String email;
    private TestDTO test;
    private QuestionDTO question;
    private boolean finished;

    private JPanel questionInfoPanel;
    private JPanel jChartPanel;

    private MqttClient client;

    public QuestionDetailsInputAScreen(String email, TestDTO test, QuestionDTO question, boolean finished) {
        this.email = email;
        this.test = test;
        this.question = question;
        this.finished = finished;
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.QUESTION_DETAILS_SCREEN_SUFFIX);
        /*
            Create question text, answers and diagram panel
         */
        displayAnswers();

        /*
            Merge all panels
         */
        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.add(questionInfoPanel);
        mainPanel.add(jChartPanel);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(600, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        logger.info("Finished test: " + finished);
        if (!finished && !question.isFinished()) {
            startMqttClient();
        }

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                new DisconnectMqttClientTask().execute();
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                updateScreen();
            }
        });
    }

    private void displayAnswers() {
        /*
            Question text
         */
        JLabel labelQuestion = new JLabel("Pitanje");
        labelQuestion.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));

        JTextArea textArea = new JTextArea(question.getQuestion(), 0, 2);
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.add(labelQuestion, BorderLayout.NORTH);
        questionPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        /*
            Given answers
         */
        JLabel labelGivenAnswers = new JLabel("Odgovori:");
        labelGivenAnswers.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30));

        JPanel answersPanel = new JPanel(new GridLayout(question.getAnswers().size(), 1));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        for (int i = 0; i < question.getAnswers().size(); i++) {
            AnswerDTO answer = question.getAnswers().get(i);
            JLabel label = new JLabel(answer.getAnswer(), JLabel.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            answersPanel.add(label);
        }

        JPanel givenAnswersListPanel = new JPanel();
        givenAnswersListPanel.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 2));
        givenAnswersListPanel.setLayout(new BoxLayout(givenAnswersListPanel, BoxLayout.X_AXIS));
        givenAnswersListPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        givenAnswersListPanel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        givenAnswersListPanel.add(Box.createHorizontalGlue());
        givenAnswersListPanel.add(answersPanel);
        givenAnswersListPanel.add(Box.createHorizontalGlue());

        JPanel givenAnswersPanel = new JPanel(new BorderLayout());
        givenAnswersPanel.add(labelGivenAnswers, BorderLayout.NORTH);
        givenAnswersPanel.add(new JScrollPane(givenAnswersListPanel), BorderLayout.CENTER);

        questionInfoPanel = new JPanel(new GridLayout(1, 2));
        questionInfoPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        questionInfoPanel.add(questionPanel);
        questionInfoPanel.add(givenAnswersPanel);

        List<AnswerDTO> answers = question.getAnswers();
        if (answers == null) {
            answers = new ArrayList<>();
            question.setAnswers(answers);
        }

        /*
            Bar chart
         */
        int numLetters = 0;
        if (answers.size() > 0) {
            numLetters = getWidth() / answers.size() / 10;
        }
        if (numLetters < 10) {
            numLetters = 10;
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < answers.size(); i++) {
            Integer numAnswered = answers.get(i).getNumAnswered();
            /*
            if (numAnswered == null || numAnswered == 0) {
                continue;
            }
             */
            if (numAnswered == null) {
                numAnswered = 0;
            }

            String answerToDisplay = answers.get(i).getAnswer();
            if (answerToDisplay.length() > numLetters) {
                answerToDisplay = answerToDisplay.substring(0, numLetters) + "...";
            }
            dataset.addValue(numAnswered, answerToDisplay, "Odgovori");
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "BAR dijagram",
                null /* x-axis label*/,
                "Broj odgovora" /* y-axis label */, dataset);
        // set a custom background for the chart
        chart.setBackgroundPaint(new GradientPaint(new Point(0, getHeight() * 2 / 3),
                Color.LIGHT_GRAY, new Point(getWidth(), getHeight()), Color.LIGHT_GRAY));

        // customise the title position and font
        TextTitle t = chart.getTitle();
        t.setHorizontalAlignment(HorizontalAlignment.CENTER);
        t.setPaint(Color.DARK_GRAY);
        t.setFont(new Font("Arial", Font.BOLD, 16));

        CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
        categoryPlot.setBackgroundPaint(null);
        categoryPlot.setOutlineVisible(false);
        categoryPlot.setRangeGridlinePaint(Color.DARK_GRAY);

        NumberAxis rangeAxis = (NumberAxis) categoryPlot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer renderer = (BarRenderer) categoryPlot.getRenderer();
        renderer.setDrawBarOutline(false);
        chart.getLegend().setFrame(BlockBorder.NONE);

        ChartPanel barChartPanel = new ChartPanel(chart, false);
        barChartPanel.setFillZoomRectangle(false);
        barChartPanel.setMouseWheelEnabled(false);
        barChartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jChartPanel = new JPanel();
        jChartPanel.setLayout(new BoxLayout(jChartPanel, BoxLayout.X_AXIS));
        jChartPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        jChartPanel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        jChartPanel.add(Box.createHorizontalGlue());
        jChartPanel.add(barChartPanel);
        jChartPanel.add(Box.createHorizontalGlue());
        jChartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jChartPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    }

    private void displayMessage(String title, String message, int code) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                code);
    }

    private void startMqttClient() {
        new ConncectMqttTask().execute();
    }

    private class ConncectMqttTask extends SwingWorker<Void, String> {

        public ConncectMqttTask() {
        }

        @Override
        protected Void doInBackground() throws Exception {
            logger.info("Initialize mqtt client");
            try {
                String clientId = email;
                client = new MqttClient(Configuration.MQTT_BROKER_URL, clientId);
                client.setCallback(QuestionDetailsInputAScreen.this);

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

                // Subscribe to question topic
                client.subscribe(test.getMqttThemeA());

                logger.info("Mqtt client initialized");
            } catch (Exception ex) {
                logger.error("Failed to start mqtt client", ex);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayMessage("Inicijalizacija", "Inicijalizacija MQTT klijenta za prikupljanje odgovora nije uspela, pokusajte ponovo.", JOptionPane.ERROR_MESSAGE);
                        QuestionDetailsInputAScreen.this.dispose();
                    }
                });
            }
            return null;
        }
    }

    private class DisconnectMqttClientTask extends SwingWorker<Void, String> {

        public DisconnectMqttClientTask() {
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Disconnect mqtt client");

                client.disconnect();

                logger.info("Mqtt client disconnected");
            } catch (Exception ex) {
                logger.error("Failed to disconnect mqtt client", ex);
            }
            return null;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String data = new String(message.getPayload());
            logger.info("Arrived answer " + data);
            AnswerDTO answer = new ObjectMapper().readValue(data, AnswerDTO.class);
            receivedAnswer(answer);
        } catch (Exception e) {
            logger.error("Failed to parse answer", e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private synchronized void receivedAnswer(AnswerDTO answer) throws Exception {
        if (!answer.getIdQuestion().equals(question.getId())) {
            return;
        }
        boolean found = false;
        String formatedAnswer = answer.getAnswer().replaceAll("\\s+", "");
        for (AnswerDTO collectedAnswer : question.getAnswers()) {
            if (collectedAnswer.getAnswer().replaceAll("\\s+", "").equalsIgnoreCase(formatedAnswer)) {
                Integer numAnswered = collectedAnswer.getNumAnswered();
                if (numAnswered == null) {
                    numAnswered = 0;
                }
                numAnswered++;
                collectedAnswer.setNumAnswered(numAnswered);
                found = true;
                break;
            }
        }
        if (!found) {
            logger.info("Given answer not found, add it to list");
            answer.setNumAnswered(1);
            question.getAnswers().add(answer);
        }

        /*
                Update screen
         */
        updateScreen();
    }

    private void updateScreen() {
        displayAnswers();

        getContentPane().removeAll();

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.add(questionInfoPanel);
        mainPanel.add(jChartPanel);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        validate();
    }

}

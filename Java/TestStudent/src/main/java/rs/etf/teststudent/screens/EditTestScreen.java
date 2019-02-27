/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.TestDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class EditTestScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(EditTestScreen.class);

    private TestDTO test;

    private JLabel labelSubject;
    private JTextField inputSubject;
    private JLabel labelClassroom;
    private JTextField inputClassroom;
    private JLabel labelDate;
    private DateTimePicker inputDate;
    private JLabel labelClassTheme;
    private JTextField inputClassTheme;
    private JButton createButton;

    public EditTestScreen(TestDTO test) {
        this.test = test;

        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.EDIT_TEST_SCREEN_SUFFIX);

        labelSubject = new JLabel("Sifra kursa:");
        inputSubject = new JTextField(test.getCourseKey());
        inputSubject.setEnabled(false);

        labelClassroom = new JLabel("Sala:");
        inputClassroom = new JTextField(test.getClassroom());

        labelDate = new JLabel("Datum i vreme odrzavanja:");

        Locale locale = new Locale("sr", "RS");
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFirstDayOfWeek(DayOfWeek.MONDAY);
        dateSettings.setLocale(locale);
        dateSettings.setAllowEmptyDates(false);

        TimePickerSettings timeSettings = new TimePickerSettings(locale);
        timeSettings.setAllowEmptyTimes(false);
        //timeSettings.initialTime = LocalTime.now();

        inputDate = new DateTimePicker(dateSettings, timeSettings);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
        LocalDateTime currentDateTime = LocalDateTime.parse(test.getDate(), formatter);
        inputDate.setDateTimeStrict(currentDateTime);

        dateSettings.setVetoPolicy(new SampleDateVetoPolicy());

        labelClassTheme = new JLabel("Tema predavanja:");
        inputClassTheme = new JTextField(test.getTestName());
        inputClassTheme.setEnabled(false);

        createButton = new JButton("Izmeni test");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTest();
            }
        });

        add(labelSubject);
        add(inputSubject);
        add(labelClassroom);
        add(inputClassroom);
        add(labelDate);
        add(inputDate);
        add(labelClassTheme);
        add(inputClassTheme);
        add(createButton);

        setMinimumSize(new Dimension(700, 440));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);

        addComponentListener(new ComponentAdapter() {
            // this method invokes each time you resize the frame
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

        int startX = (width - 600) / 2;
        int startY = (height - 340) / 2;

        labelSubject.setBounds(startX, startY, 300, 30);
        inputSubject.setBounds(startX + 300, startY, 300, 30);

        labelClassroom.setBounds(startX, startY + 60, 300, 30);
        inputClassroom.setBounds(startX + 300, startY + 60, 300, 30);

        labelDate.setBounds(startX, startY + 120, 300, 30);
        inputDate.setBounds(startX + 300, startY + 120, 300, 30);

        labelClassTheme.setBounds(startX, startY + 180, 300, 30);
        inputClassTheme.setBounds(startX + 300, startY + 180, 300, 30);

        createButton.setBounds(startX, startY + 240, 600, 70);
    }

    private void editTest() {
        String classroom = inputClassroom.getText();
        LocalDateTime date = inputDate.getDateTimeStrict();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formatDateTime = date.format(formatter);

        if (classroom == null || classroom.isEmpty()) {
            displayMessage("Greska", "Sala mora biti uneta", false);
            return;
        }

        if (formatDateTime == null || formatDateTime.isEmpty()) {
            displayMessage("Greska", "Datum i vreme predavanja mora biti uneto", false);
            return;
        }

        try {
            TestDTO editedTest = test.clone();
            editedTest.setClassroom(classroom);
            editedTest.setDate(formatDateTime);

            new EditTestTask(editedTest).execute();
        } catch (Exception ex) {
            displayMessage("Greska", "Izmena testa nije uspela", false);
        }
    }

    private class EditTestTask extends SwingWorker<Void, String> {

        private TestDTO editedTest;
        private boolean status = false;
        private String errorMessage = null;

        public EditTestTask(TestDTO editedTest) {
            this.editedTest = editedTest;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending edit test request: " + editedTest);

                NetworkServicePrivate.getInstance().editTest(editedTest);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to edit test", ex);
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
                logger.info("Test is edited");

                test.setDate(editedTest.getDate());
                test.setClassroom(editedTest.getClassroom());

                displayMessage("Test izmenjen", "Test je uspesno izmenjen", true);
            } else {
                String message = "Izmena testa nije uspela";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }
        }

    }

    private void displayMessage(String title, String message, boolean status) {
        if (status) {
            JOptionPane.showMessageDialog(this,
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    message,
                    title,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class SampleDateVetoPolicy implements DateVetoPolicy {

        /**
         * isDateAllowed, Return true if a date should be allowed, or false if a
         * date should be vetoed.
         */
        @Override
        public boolean isDateAllowed(LocalDate date) {
            // Disallow sunday.
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return false;
            }

            // Allow only time in future.
            if (date.isBefore(LocalDate.now())) {
                return false;
            }

            // Allow all other days.
            return true;
        }
    }
}

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.CreateTestDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class CreateTestScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(CreateTestScreen.class);

    private String email;
    private String[] courses;
    private JLabel labelSubject;
    private JComboBox inputSubject;
    private JLabel labelClassroom;
    private JTextField inputClassroom;
    private JLabel labelDate;
    private DateTimePicker inputDate;
    private JLabel labelClassTheme;
    private JTextField inputClassTheme;
    private JButton createButton;

    public CreateTestScreen(String email, List<String> coursesList) {
        this.email = email;
        if (coursesList != null && coursesList.size() > 0) {
            courses = coursesList.toArray(new String[coursesList.size()]);
        } else {
            courses = new String[1];
            courses[0] = "Ne postoje kursevi";
        }
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.CREATE_TEST_SCREEN_SUFFIX);

        labelSubject = new JLabel("Sifra kursa:");
        inputSubject = new JComboBox(courses);

        labelClassroom = new JLabel("Sala:");
        inputClassroom = new JTextField();
        
        labelDate = new JLabel("Datum i vreme odrzavanja:");

        Locale locale = new Locale("sr", "RS");
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFirstDayOfWeek(DayOfWeek.MONDAY);
        dateSettings.setLocale(locale);
        dateSettings.setAllowEmptyDates(false);

        TimePickerSettings timeSettings = new TimePickerSettings(locale);
        timeSettings.setAllowEmptyTimes(false);
        timeSettings.initialTime = LocalTime.now();

        inputDate = new DateTimePicker(dateSettings, timeSettings);

        dateSettings.setVetoPolicy(new SampleDateVetoPolicy());

        labelClassTheme = new JLabel("Tema predavanja:");
        inputClassTheme = new JTextField();

        createButton = new JButton("Kreiraj test");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTest();
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

    private void createTest() {
        int index = inputSubject.getSelectedIndex();
        String course = courses[index];
        String classroom = inputClassroom.getText();
        String theme = inputClassTheme.getText();
        LocalDateTime date = inputDate.getDateTimeStrict();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formatDateTime = date.format(formatter);

        if (course == null || course.isEmpty() || course.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        if (classroom == null || classroom.isEmpty()) {
            displayMessage("Greska", "Sala mora biti uneta", false);
            return;
        }

        if (theme == null || theme.isEmpty()) {
            displayMessage("Greska", "Tema predavanja mora biti uneta", false);
            return;
        }

        if (formatDateTime == null || formatDateTime.isEmpty()) {
            displayMessage("Greska", "Datum i vreme predavanja mora biti uneto", false);
            return;
        }

        CreateTestDTO createTestDTO = new CreateTestDTO(email, course, classroom, theme, formatDateTime);

        CreateTestTask task = new CreateTestTask(createTestDTO);
        task.execute();
    }

    private class CreateTestTask extends SwingWorker<Void, String> {

        private CreateTestDTO createTestDTO;
        private boolean created = false;
        private String errorMessage = null;

        public CreateTestTask(CreateTestDTO createTestDTO) {
            this.createTestDTO = createTestDTO;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending create test request: " + createTestDTO);

                NetworkServicePrivate.getInstance().createTest(createTestDTO);

                created = true;
            } catch (Exception ex) {
                logger.error("Failed to create test", ex);
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
            if (created) {
                logger.info("Test is created");

                inputSubject.setSelectedIndex(0);
                inputClassroom.setText("");
                inputClassTheme.setText("");
                inputDate.setDateTimePermissive(LocalDateTime.now());

                displayMessage("Test napravljen", "Test je uspesno napravljen", true);
            } else {
                String message = "Kreiranje testa nije uspelo";
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

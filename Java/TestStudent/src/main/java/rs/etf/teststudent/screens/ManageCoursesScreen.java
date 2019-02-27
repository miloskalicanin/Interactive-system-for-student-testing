/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.CopyTestsDTO;
import rs.etf.teststudent.dto.CreateCourseDTO;
import rs.etf.teststudent.dto.DeleteCourseDTO;
import rs.etf.teststudent.dto.GetCoursesDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class ManageCoursesScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(ManageCoursesScreen.class);

    private GetCoursesDTO coursesAndSubjects;

    private String[] courses;
    private String[] subjects;

    JComboBox inputSubject;
    JTextField inputCourse;
    DatePicker inputDateStart;
    DatePicker inputDateEnd;

    JComboBox inputDeleteCourse;

    JComboBox inputCourseFrom;
    JComboBox inputCourseTo;

    public ManageCoursesScreen(GetCoursesDTO coursesAndSubjects) {
        this.coursesAndSubjects = coursesAndSubjects;

        updateCoursesAndSubjects();
    }

    private void updateCoursesAndSubjects() {
        List<String> subjectsList = coursesAndSubjects.getSubjects();
        List<String> coursesList = coursesAndSubjects.getCourses();

        if (subjectsList != null && subjectsList.size() > 0) {
            subjects = subjectsList.toArray(new String[subjectsList.size()]);
        } else {
            subjects = new String[1];
            subjects[0] = "Ne postoje predmeti";
        }
        if (coursesList != null && coursesList.size() > 0) {
            courses = coursesList.toArray(new String[coursesList.size()]);
        } else {
            courses = new String[1];
            courses[0] = "Ne postoje kursevi";
        }

        getContentPane().removeAll();
        setupScreen();
        validate();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.MANAGE_COURSES_SCREEN_SUFFIX);

        /*
            Create course
         */
        JLabel labelCreateCourse = new JLabel("Kreiraj kurs", JLabel.CENTER);
        labelCreateCourse.setBorder(BorderFactory.createEmptyBorder(8, 15, 18, 15));

        JLabel labelSubject = new JLabel("Sifra predmeta:");
        labelSubject.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputSubject = new JComboBox(subjects);
        inputSubject.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JLabel labelCourse = new JLabel("Sifra kursa:");
        labelCourse.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputCourse = new JTextField();
        inputCourse.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        Locale locale = new Locale("sr", "RS");
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFirstDayOfWeek(DayOfWeek.MONDAY);
        dateSettings.setLocale(locale);
        dateSettings.setAllowEmptyDates(false);
        JLabel labelDateStart = new JLabel("Datum pocetka kursa:");
        labelDateStart.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputDateStart = new DatePicker(dateSettings);
        inputDateStart.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        dateSettings.setVetoPolicy(new SampleDateVetoPolicy());

        dateSettings = new DatePickerSettings();
        dateSettings.setFirstDayOfWeek(DayOfWeek.MONDAY);
        dateSettings.setLocale(locale);
        dateSettings.setAllowEmptyDates(false);
        JLabel labelDateEnd = new JLabel("Datum kraja kursa:");
        labelDateEnd.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputDateEnd = new DatePicker(dateSettings);
        inputDateEnd.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        dateSettings.setVetoPolicy(new SampleDateVetoPolicy());

        JButton createButton = new JButton("Kreiraj kurs");
        createButton.setBorder(BorderFactory.createEmptyBorder(13, 10, 13, 10));
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createCourse();
            }
        });

        JPanel createCoursePanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        createCoursePanel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.ipadx = 0;
        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(labelCreateCourse, c);
        createCoursePanel.add(labelCreateCourse);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(labelSubject, c);
        createCoursePanel.add(labelSubject);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(inputSubject, c);
        createCoursePanel.add(inputSubject);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(labelCourse, c);
        createCoursePanel.add(labelCourse);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 2;
        gridbag.setConstraints(inputCourse, c);
        createCoursePanel.add(inputCourse);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(labelDateStart, c);
        createCoursePanel.add(labelDateStart);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 3;
        gridbag.setConstraints(inputDateStart, c);
        createCoursePanel.add(inputDateStart);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 4;
        gridbag.setConstraints(labelDateEnd, c);
        createCoursePanel.add(labelDateEnd);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 4;
        gridbag.setConstraints(inputDateEnd, c);
        createCoursePanel.add(inputDateEnd);

        JLabel empty = new JLabel("");
        c.ipady = 10;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 5;
        gridbag.setConstraints(empty, c);
        createCoursePanel.add(empty);

        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 6;
        gridbag.setConstraints(createButton, c);
        createCoursePanel.add(createButton);

        /*
            Delete course
         */
        JLabel labelDeleteCourse = new JLabel("Obrisi kurs", JLabel.CENTER);
        labelDeleteCourse.setBorder(BorderFactory.createEmptyBorder(8, 15, 18, 15));

        JLabel labelCourseDelete = new JLabel("Sifra kursa:");
        labelCourseDelete.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 60));
        inputDeleteCourse = new JComboBox(courses);
        inputDeleteCourse.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JButton deleteButton = new JButton("Obrisi kurs");
        deleteButton.setBorder(BorderFactory.createEmptyBorder(13, 10, 13, 10));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCourse();
            }
        });

        JPanel deleteCoursePanel = new JPanel();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        deleteCoursePanel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.ipadx = 0;
        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(labelDeleteCourse, c);
        deleteCoursePanel.add(labelDeleteCourse);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(labelCourseDelete, c);
        deleteCoursePanel.add(labelCourseDelete);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(inputDeleteCourse, c);
        deleteCoursePanel.add(inputDeleteCourse);

        empty = new JLabel("");
        c.ipady = 10;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(empty, c);
        deleteCoursePanel.add(empty);

        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 4;
        gridbag.setConstraints(deleteButton, c);
        deleteCoursePanel.add(deleteButton);

        /*
            Copy tests
         */
        JLabel labelCopyTests = new JLabel("Kopiraj testove", JLabel.CENTER);
        labelCopyTests.setBorder(BorderFactory.createEmptyBorder(8, 15, 18, 15));

        JLabel labelCourseFrom = new JLabel("Kopiraj iz:");
        labelCourseFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 60));
        inputCourseFrom = new JComboBox(courses);
        inputCourseFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JLabel labelCourseTo = new JLabel("Kopiraj u:");
        labelCourseTo.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 60));
        inputCourseTo = new JComboBox(courses);
        inputCourseTo.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JButton copyButton = new JButton("Kopiraj testove");
        copyButton.setBorder(BorderFactory.createEmptyBorder(13, 10, 13, 10));
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyTests();
            }
        });

        JPanel copyTestsPanel = new JPanel();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        copyTestsPanel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.ipadx = 0;
        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(labelCopyTests, c);
        copyTestsPanel.add(labelCopyTests);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(labelCourseFrom, c);
        copyTestsPanel.add(labelCourseFrom);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(inputCourseFrom, c);
        copyTestsPanel.add(inputCourseFrom);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(labelCourseTo, c);
        copyTestsPanel.add(labelCourseTo);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 2;
        gridbag.setConstraints(inputCourseTo, c);
        copyTestsPanel.add(inputCourseTo);

        empty = new JLabel("");
        c.ipady = 10;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(empty, c);
        copyTestsPanel.add(empty);

        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 4;
        gridbag.setConstraints(copyButton, c);
        copyTestsPanel.add(copyButton);

        /*
            Set layout
         */
        JPanel mainPanel = new JPanel(new GridLayout(3, 1));
        mainPanel.add(createCoursePanel);
        mainPanel.add(deleteCoursePanel);
        mainPanel.add(copyTestsPanel);
        setLayout(new BorderLayout());
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        setMinimumSize(new Dimension(600, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void createCourse() {
        int index = inputSubject.getSelectedIndex();
        String subject = subjects[index];
        String course = inputCourse.getText();
        LocalDate dateStart = inputDateStart.getDate();
        LocalDate dateEnd = inputDateEnd.getDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formatDateTimeStart = dateStart.format(formatter);
        String formatDateTimeEnd = dateEnd.format(formatter);

        if (subject == null || subject.isEmpty() || subject.equals("Ne postoje predmeti")) {
            displayMessage("Greska", "Sifra predmeta mora biti uneta", false);
            return;
        }

        if (course == null || course.isEmpty()) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        if (formatDateTimeStart == null || formatDateTimeStart.isEmpty()) {
            displayMessage("Greska", "Datum pocetka kursa mora biti unet", false);
            return;
        }

        if (formatDateTimeEnd == null || formatDateTimeEnd.isEmpty()) {
            displayMessage("Greska", "Datum kraja kursa mora biti unet", false);
            return;
        }

        CreateCourseDTO createCourseDTO = new CreateCourseDTO();
        createCourseDTO.setSubject(subject);
        createCourseDTO.setCourse(course);
        createCourseDTO.setDateStart(formatDateTimeStart);
        createCourseDTO.setDateEnd(formatDateTimeEnd);

        new CreateCourseTask(createCourseDTO).execute();
    }

    private void deleteCourse() {
        int index = inputDeleteCourse.getSelectedIndex();
        String course = courses[index];

        if (course == null || course.isEmpty() || course.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        new DeleteCourseTask(course).execute();
    }

    private void copyTests() {
        int index = inputCourseFrom.getSelectedIndex();
        String copyFrom = courses[index];

        index = inputCourseTo.getSelectedIndex();
        String copyTo = courses[index];

        if (copyFrom == null || copyFrom.isEmpty() || copyFrom.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        if (copyTo == null || copyTo.isEmpty() || copyTo.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        CopyTestsDTO copyTestsDTO = new CopyTestsDTO(copyFrom, copyTo);
        new CopyTestsTask(copyTestsDTO).execute();
    }

    private class CreateCourseTask extends SwingWorker<Void, String> {

        private CreateCourseDTO createCourseDTO;
        private boolean created = false;
        private String errorMessage = null;

        public CreateCourseTask(CreateCourseDTO createCourseDTO) {
            this.createCourseDTO = createCourseDTO;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending create course request: " + createCourseDTO);

                NetworkServicePrivate.getInstance().createCourse(createCourseDTO);

                created = true;
            } catch (Exception ex) {
                logger.error("Failed to create course", ex);
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
                logger.info("Course is created");

                List<String> coursesList = coursesAndSubjects.getCourses();
                if (coursesList == null) {
                    coursesList = new ArrayList<>();
                }
                coursesList.add(createCourseDTO.getCourse());
                coursesAndSubjects.setCourses(coursesList);

                updateCoursesAndSubjects();

                displayMessage("Kurs napravljen", "Kurs je uspesno napravljen", true);
            } else {
                String message = "Kreiranje kursa nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }
        }
    }

    private class DeleteCourseTask extends SwingWorker<Void, String> {

        private DeleteCourseDTO deleteCourseDTO;
        private boolean deleted = false;
        private String errorMessage = null;

        public DeleteCourseTask(String course) {
            this.deleteCourseDTO = new DeleteCourseDTO(course);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending delete course request: " + deleteCourseDTO);

                NetworkServicePrivate.getInstance().deleteCourse(deleteCourseDTO);

                deleted = true;
            } catch (Exception ex) {
                logger.error("Failed to delete course", ex);
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
            if (deleted) {
                logger.info("Course is deleted");

                List<String> coursesList = coursesAndSubjects.getCourses();
                for (String c : coursesList) {
                    if (c.equals(deleteCourseDTO.getCourse())) {
                        coursesList.remove(c);
                        break;
                    }
                }
                coursesAndSubjects.setCourses(coursesList);

                updateCoursesAndSubjects();

                displayMessage("Kurs obrisan", "Kurs je uspesno obrisan", true);
            } else {
                String message = "Brisanje kursa nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }
        }
    }

    private class CopyTestsTask extends SwingWorker<Void, String> {

        private CopyTestsDTO copyTestsDTO;
        private boolean status = false;
        private String errorMessage = null;

        public CopyTestsTask(CopyTestsDTO copyTestsDTO) {
            this.copyTestsDTO = copyTestsDTO;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending copy tests request: " + copyTestsDTO);

                NetworkServicePrivate.getInstance().copyTests(copyTestsDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to copy tests", ex);
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
                logger.info("Tests are copied");

                displayMessage("Kopiranje testova", "Kopiranje testova uspesno", true);
            } else {
                String message = "Kopiranje testova nije uspelo";
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
            // Allow only time in future.
            if (date.isBefore(LocalDate.now())) {
                return false;
            }

            // Allow all other days.
            return true;
        }
    }
}

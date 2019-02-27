/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.GetStudentsAndCoursesDTO;
import rs.etf.teststudent.dto.GetStudentsFromCourseDTO;
import rs.etf.teststudent.dto.UpdateCourseStudentDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class ManageStudentsAndCoursesScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(ManageStudentsAndCoursesScreen.class);

    private GetStudentsAndCoursesDTO getStudentsAndCoursesDTO;

    private String[] courses;
    private String[] students;
    private String[] studentsOnSelectedCourse;

    JComboBox inputCourse;
    JList listOfStudents;

    JComboBox inputCourseFrom;
    JComboBox inputStudentFrom;

    public ManageStudentsAndCoursesScreen(GetStudentsAndCoursesDTO getStudentsAndCoursesDTO) {
        this.getStudentsAndCoursesDTO = getStudentsAndCoursesDTO;

        setupScreen();
    }

    private void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.MANAGE_STUDENTS_AND_COURSES_SCREEN_SUFFIX);

        List<String> studentList = getStudentsAndCoursesDTO.getStudents();
        List<String> coursesList = getStudentsAndCoursesDTO.getCourses();

        if (studentList != null && studentList.size() > 0) {
            students = studentList.toArray(new String[studentList.size()]);
        } else {
            students = new String[1];
            students[0] = "Ne postoje studenti";
        }
        if (coursesList != null && coursesList.size() > 0) {
            courses = coursesList.toArray(new String[coursesList.size()]);
        } else {
            courses = new String[1];
            courses[0] = "Ne postoje kursevi";
        }
        studentsOnSelectedCourse = new String[1];
        studentsOnSelectedCourse[0] = "Nema informacija o prijavljenim studentima na kursu";

        initComponents();
        getStudentsForCourse();

        setMinimumSize(new Dimension(600, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addComponentListener(new ComponentAdapter() {
            // this method invokes each time you resize the frame
            public void componentResized(ComponentEvent e) {
                updateCoursesAndStudents();
            }
        });
    }

    private void updateCoursesAndStudents() {
        getContentPane().removeAll();
        initComponents();
        validate();
    }

    public void initComponents() {
        /*
            Subscribe - unsubscribe
         */
        JLabel labelCopyTests = new JLabel("Dodaj ili ukloni sa kursa", JLabel.CENTER);
        labelCopyTests.setBorder(BorderFactory.createEmptyBorder(8, 15, 18, 15));

        JLabel labelCourseFrom = new JLabel("Izaberi kurs:");
        labelCourseFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 60));
        inputCourseFrom = new JComboBox(courses);
        inputCourseFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JLabel labelStudentFrom = new JLabel("Izaberi studenta:");
        labelStudentFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 60));
        inputStudentFrom = new JComboBox(students);
        inputStudentFrom.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        JButton addStudentToCourseButton = new JButton("Dodaj na kurs");
        addStudentToCourseButton.setBorder(BorderFactory.createEmptyBorder(13, 10, 13, 10));
        addStudentToCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStudentToCourse();
            }
        });

        JButton deleteStudentFromCourseButton = new JButton("Ukloni sa kursa");
        deleteStudentFromCourseButton.setBorder(BorderFactory.createEmptyBorder(13, 10, 13, 10));
        deleteStudentFromCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudentFromCourse();
            }
        });

        JPanel addRemoveStudentsPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        addRemoveStudentsPanel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.ipadx = 0;
        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(labelCopyTests, c);
        addRemoveStudentsPanel.add(labelCopyTests);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(labelCourseFrom, c);
        addRemoveStudentsPanel.add(labelCourseFrom);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(inputCourseFrom, c);
        addRemoveStudentsPanel.add(inputCourseFrom);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(labelStudentFrom, c);
        addRemoveStudentsPanel.add(labelStudentFrom);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 2;
        gridbag.setConstraints(inputStudentFrom, c);
        addRemoveStudentsPanel.add(inputStudentFrom);

        JLabel empty = new JLabel("");
        c.ipady = 10;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(empty, c);
        addRemoveStudentsPanel.add(empty);

        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 4;
        gridbag.setConstraints(addStudentToCourseButton, c);
        addRemoveStudentsPanel.add(addStudentToCourseButton);

        empty = new JLabel("");
        c.ipady = 10;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 5;
        gridbag.setConstraints(empty, c);
        addRemoveStudentsPanel.add(empty);

        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 6;
        gridbag.setConstraints(deleteStudentFromCourseButton, c);
        addRemoveStudentsPanel.add(deleteStudentFromCourseButton);

        /*
            Students attending courses
         */
        JLabel labelStudentsOnCourse = new JLabel("Lista prijavljenih", JLabel.CENTER);
        labelStudentsOnCourse.setBorder(BorderFactory.createEmptyBorder(8, 15, 18, 15));

        JLabel labelSelectCourse = new JLabel("Izaberi kurs:");
        labelSelectCourse.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputCourse = new JComboBox(courses);
        inputCourse.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        inputCourse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getStudentsForCourse();
            }
        });

        DefaultListModel listModel = new DefaultListModel();
        for (String s : studentsOnSelectedCourse) {
            listModel.addElement(s);
        }
        listOfStudents = new JList(listModel);
        listOfStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listOfStudents.setLayoutOrientation(JList.VERTICAL);
        listOfStudents.setVisibleRowCount(-1);

        JPanel studentsOnCoursePanel = new JPanel();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        studentsOnCoursePanel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.ipadx = 0;
        c.ipady = 0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(labelStudentsOnCourse, c);
        studentsOnCoursePanel.add(labelStudentsOnCourse);

        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(labelSelectCourse, c);
        studentsOnCoursePanel.add(labelSelectCourse);
        c.ipadx = 80;
        c.gridx = 1;
        c.gridy = 1;
        gridbag.setConstraints(inputCourse, c);
        studentsOnCoursePanel.add(inputCourse);

        JScrollPane listScroller = new JScrollPane(listOfStudents,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        int height = getHeight() / 4;
        listScroller.setPreferredSize(new Dimension(150, height));
        c.ipadx = 80;
        c.gridwidth = 2;
        c.gridheight = 8;
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(listScroller, c);
        studentsOnCoursePanel.add(listScroller);

        /*
            Set layout
         */
        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.add(addRemoveStudentsPanel);
        mainPanel.add(studentsOnCoursePanel);
        setLayout(new BorderLayout());
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listOfStudents != null) {
                    listOfStudents.clearSelection();
                }
            }
        });
    }

    private void getStudentsForCourse() {
        int index = inputCourse.getSelectedIndex();
        String course = courses[index];

        if (course == null || course.isEmpty() || course.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        new GetStudentsFromCourseTask(course).execute();
    }

    private void addStudentToCourse() {
        int index = inputCourseFrom.getSelectedIndex();
        String course = courses[index];

        index = inputStudentFrom.getSelectedIndex();
        String student = students[index];

        if (course == null || course.isEmpty() || course.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        if (student == null || student.isEmpty() || student.equals("Ne postoje studenti")) {
            displayMessage("Greska", "Email studenta mora biti unet", false);
            return;
        }

        new AddStudentTask(course, student).execute();
    }

    private void deleteStudentFromCourse() {
        int index = inputCourseFrom.getSelectedIndex();
        String course = courses[index];

        index = inputStudentFrom.getSelectedIndex();
        String student = students[index];

        if (course == null || course.isEmpty() || course.equals("Ne postoje kursevi")) {
            displayMessage("Greska", "Sifra kursa mora biti uneta", false);
            return;
        }

        if (student == null || student.isEmpty()) {
            displayMessage("Greska", "Email studenta mora biti unet", false);
            return;
        }

        new DeleteStudentTask(course, student).execute();
    }

    private class GetStudentsFromCourseTask extends SwingWorker<Void, String> {

        private GetStudentsFromCourseDTO getStudentsFromCourseDTO;
        private boolean status = false;
        private String errorMessage = null;

        public GetStudentsFromCourseTask(String course) {
            getStudentsFromCourseDTO = new GetStudentsFromCourseDTO();
            getStudentsFromCourseDTO.setCourse(course);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending get students from course request: " + getStudentsFromCourseDTO);

                getStudentsFromCourseDTO = NetworkServicePrivate.getInstance().getStudentsFromCourse(getStudentsFromCourseDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to get students from course", ex);
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
                logger.info("Students getting was successful");

                List<String> students = getStudentsFromCourseDTO.getStudents();
                if (students == null || students.isEmpty()) {
                    studentsOnSelectedCourse = new String[1];
                    studentsOnSelectedCourse[0] = "Nema prijavljenih studenata na kursu";
                } else {
                    studentsOnSelectedCourse = students.toArray(new String[students.size()]);
                }
            } else {
                studentsOnSelectedCourse = new String[1];
                studentsOnSelectedCourse[0] = "Nema informacija o prijavljenim studentima na kursu";
                String message = "Dohvatanje studenata za selektovani kurs nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }

            updateCoursesAndStudents();
        }
    }

    private class AddStudentTask extends SwingWorker<Void, String> {

        private UpdateCourseStudentDTO updateCourseStudentDTO;
        private boolean status = false;
        private String errorMessage = null;

        public AddStudentTask(String course, String student) {
            updateCourseStudentDTO = new UpdateCourseStudentDTO();
            updateCourseStudentDTO.setCourse(course);
            updateCourseStudentDTO.setStudent(student);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending add students to course request: " + updateCourseStudentDTO);

                NetworkServicePrivate.getInstance().addStudentToCourse(updateCourseStudentDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to add student to course", ex);
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
                logger.info("Students adding was successful");

                if (updateCourseStudentDTO.getCourse().equals(courses[inputCourse.getSelectedIndex()])) {
                    getStudentsForCourse();
                }

                displayMessage("Dodavanje studenta", "Student je uspesno dodat", true);
            } else {
                String message = "Dodavanje studenta na selektovani kurs nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }
        }
    }

    private class DeleteStudentTask extends SwingWorker<Void, String> {

        private UpdateCourseStudentDTO updateCourseStudentDTO;
        private boolean status = false;
        private String errorMessage = null;

        public DeleteStudentTask(String course, String student) {
            updateCourseStudentDTO = new UpdateCourseStudentDTO();
            updateCourseStudentDTO.setCourse(course);
            updateCourseStudentDTO.setStudent(student);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending delete students from course request: " + updateCourseStudentDTO);

                NetworkServicePrivate.getInstance().deleteStudentFromCourse(updateCourseStudentDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to delete student from course", ex);
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
                logger.info("Students deleting was successful");

                if (updateCourseStudentDTO.getCourse().equals(courses[inputCourse.getSelectedIndex()])) {
                    getStudentsForCourse();
                }

                displayMessage("Brisanje studenta", "Student je uspesno obrisan", true);
            } else {
                String message = "Brisanje studenta sa selektovanog kursa nije uspelo";
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
}

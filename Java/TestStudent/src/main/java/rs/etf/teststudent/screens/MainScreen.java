package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.CreateUsersDTO;
import rs.etf.teststudent.dto.GetCoursesDTO;
import rs.etf.teststudent.dto.GetStudentsAndCoursesDTO;
import rs.etf.teststudent.dto.TestsDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class MainScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(MainScreen.class);

    private String email;

    private JButton newTest;
    private JButton activeTests;
    private JButton history;
    private JButton addUsers;
    private JButton manageCourses;
    private JButton manageStudents;

    public MainScreen(String email) {
        this.email = email;
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.MAIN_SCREEN_SUFFIX);

        newTest = new JButton("Novi test");
        newTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GetCoursesTask(email, 0).execute();
            }
        });

        activeTests = new JButton("Nezavrseni testovi");
        activeTests.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTests();
            }
        });

        history = new JButton("Istorija");
        history.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getFinishedTests();
            }
        });

        addUsers = new JButton("Dodaj korisnike");
        addUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Izaberi fajl");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = fileChooser.showOpenDialog(MainScreen.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String data = null;
                    try {
                        byte[] encoded = Files.readAllBytes(selectedFile.toPath());
                        data = new String(encoded, StandardCharsets.UTF_8);
                    } catch (Exception ex) {

                    }

                    if (data == null) {
                        displayMessage("Citanje fajla", "Citanje fajla nije uspelo", false);
                    } else {
                        new AddUsersTask(email, data).execute();
                    }
                }
            }
        });

        manageCourses = new JButton("Upravljanje kursevima");
        manageCourses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GetCoursesTask(email, 1).execute();
            }
        });

        manageStudents = new JButton("Upravljanje pracenjem kursa");
        manageStudents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GetStudentsAndCoursesTask().execute();
            }
        });

        setLayout(null);
        add(newTest);
        add(activeTests);
        add(history);
        add(addUsers);
        add(manageCourses);
        add(manageStudents);

        setMinimumSize(new Dimension(400, 560));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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

        int startX = (width - 300) / 2;
        int startY = (height - 480) / 2;

        newTest.setBounds(startX, startY, 300, 50);

        activeTests.setBounds(startX, startY + 80, 300, 50);

        history.setBounds(startX, startY + 160, 300, 50);

        addUsers.setBounds(startX, startY + 240, 300, 50);

        manageCourses.setBounds(startX, startY + 320, 300, 50);

        manageStudents.setBounds(startX, startY + 400, 300, 50);
    }

    private void setButtonsEnabled(boolean value) {
        newTest.setEnabled(value);
        activeTests.setEnabled(value);
        history.setEnabled(value);
        addUsers.setEnabled(value);
        manageCourses.setEnabled(value);
        manageStudents.setEnabled(value);
    }

    private void getActiveTests() {
        setButtonsEnabled(false);
        try {
            GetTestsTask task = new GetTestsTask(email, false);
            task.execute();
        } catch (Exception e) {
            setButtonsEnabled(true);
        }
    }

    private void getFinishedTests() {
        setButtonsEnabled(false);
        try {
            GetTestsTask task = new GetTestsTask(email, true);
            task.execute();
        } catch (Exception e) {
            setButtonsEnabled(true);
        }
    }

    private class GetTestsTask extends SwingWorker<Void, String> {

        private TestsDTO tests;
        private boolean finished;
        private boolean status = false;
        private String errorMessage = null;

        public GetTestsTask(String email, boolean finished) {
            this.tests = new TestsDTO(email, null);
            this.finished = finished;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending get test request: " + tests + ", finished: " + finished);

                TestsDTO tmp;
                if (finished) {
                    tmp = NetworkServicePrivate.getInstance().getFinishedTests(tests);
                } else {
                    tmp = NetworkServicePrivate.getInstance().getUnfinishedTests(tests);
                }
                tests = tmp;

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to get tests", ex);
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
                logger.info("Get tests successfully: " + tests);

                new TestsScreen(tests, finished);
            } else {
                String message = "Dohvatanje testova nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }

            setButtonsEnabled(true);
        }
    }

    private class AddUsersTask extends SwingWorker<Void, String> {

        private String email;
        private String data;
        private boolean status = false;
        private String errorMessage = null;

        public AddUsersTask(String email, String data) {
            this.email = email;
            this.data = data;

            setButtonsEnabled(false);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending create users request");

                CreateUsersDTO createUsersDTO = new CreateUsersDTO(email, data);

                NetworkServicePrivate.getInstance().createUsers(createUsersDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to create users", ex);
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
                logger.info("Users created");

                displayMessage("Dodavanje korisnika", "Dodavanje korisnika uspesno", true);
            } else {
                String message = "Dodavanje korisnika nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }

            setButtonsEnabled(true);
        }
    }

    private class GetCoursesTask extends SwingWorker<Void, String> {

        private int screen; // 0 - create test, 1 - manage courses

        private String email;
        private GetCoursesDTO getCoursesDTO;
        private boolean status = false;
        private String errorMessage = null;

        public GetCoursesTask(String email, int screen) {
            this.email = email;
            this.screen = screen;

            setButtonsEnabled(false);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending get courses request");

                getCoursesDTO = new GetCoursesDTO();
                getCoursesDTO.setEmail(email);

                getCoursesDTO = NetworkServicePrivate.getInstance().getCourses(getCoursesDTO);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to get courses", ex);
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
                logger.info("Courses list obtained");

                if (screen == 0) {
                    new CreateTestScreen(email, getCoursesDTO.getCourses());
                } else {
                    new ManageCoursesScreen(getCoursesDTO);
                }
            } else {
                String message = "Dohvatanje kurseva nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }

            setButtonsEnabled(true);
        }
    }

    private class GetStudentsAndCoursesTask extends SwingWorker<Void, String> {

        private GetStudentsAndCoursesDTO getStudentsAndCoursesDTO = null;
        private boolean status = false;
        private String errorMessage = null;

        public GetStudentsAndCoursesTask() {
            setButtonsEnabled(false);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Sending get students and courses request");

                getStudentsAndCoursesDTO = NetworkServicePrivate.getInstance().getStudentsAndCourses();

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to get students and courses", ex);
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
                logger.info("Students and courses list obtained");

                new ManageStudentsAndCoursesScreen(getStudentsAndCoursesDTO);
            } else {
                String message = "Dohvatanje studenata i kurseva nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, false);
            }

            setButtonsEnabled(true);
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.screens;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.TestDTO;
import rs.etf.teststudent.dto.TestInfoDTO;
import rs.etf.teststudent.dto.TestsDTO;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class TestsScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(TestsScreen.class);

    private String email;
    private TestsDTO tests;
    private boolean finished;

    private boolean inProgress = false;

    public TestsScreen(TestsDTO tests, boolean finished) {
        this.email = tests.getEmail();
        this.tests = tests;
        this.finished = finished;
        setupScreen();
    }

    public void setupScreen() {
        setTitle(Configuration.APP_NAME + Configuration.TESTS_SCREEN_SUFFIX);

        JLabel labelSubject = new JLabel("Sifra kursa:", JLabel.CENTER);
        JLabel labelClassroom = new JLabel("Sala:", JLabel.CENTER);
        JLabel labelDate = new JLabel("Datum i vreme odrzavanja:", JLabel.CENTER);
        JLabel labelClassTheme = new JLabel("Tema predavanja:", JLabel.CENTER);

        JPanel titlePanel = new JPanel(new GridLayout(1, 4));
        if (finished) {
            titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 80));
        } else {
            titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 270));
        }
        titlePanel.add(labelSubject);
        titlePanel.add(labelClassroom);
        titlePanel.add(labelDate);
        titlePanel.add(labelClassTheme);

        JPanel testsPanel = new JPanel(new GridLayout(tests.getTests().size(), 4));
        testsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel editTestsPanel;
        if (finished) {
            editTestsPanel = new JPanel(new GridLayout(tests.getTests().size(), 1));
        } else {
            editTestsPanel = new JPanel(new GridLayout(tests.getTests().size(), 3));
        }
        editTestsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        for (TestDTO t : tests.getTests()) {
            JLabel label = new JLabel(t.getCourseKey(), JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            testsPanel.add(label);

            label = new JLabel(t.getClassroom(), JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            testsPanel.add(label);

            label = new JLabel(t.getDate(), JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            testsPanel.add(label);

            label = new JLabel(t.getTestName(), JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            testsPanel.add(label);

            label = new JLabel("Detaljnije", JLabel.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            editTestsPanel.add(label);

            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    testInfo(t);
                }
            });

            if (!finished) {               
                label = new JLabel("Izmeni", JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                editTestsPanel.add(label);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        editTest(t);
                    }
                });
                
                label = new JLabel("Obrisi", JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                editTestsPanel.add(label);
                label.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        deleteTest(t);
                    }
                });
            }
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(testsPanel, BorderLayout.CENTER);
        centerPanel.add(editTestsPanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(new JScrollPane(centerPanel), BorderLayout.CENTER);

        setMinimumSize(new Dimension(800, 200));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void displayMessage(String title, String message, int code) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                code);
    }

    private void testInfo(TestDTO test) {
        if (inProgress) {
            return;
        }

        inProgress = true;
        GetQuestionsTask task = new GetQuestionsTask(test);
        task.execute();
    }
    private void editTest(TestDTO test) {
        if (inProgress) {
            return;
        }

        Object[] options = {"DA", "NE"};
        int dialogResult = JOptionPane.showOptionDialog(this, 
                "Da li ste sigurni da zelite da izmenite ovaj test?", 
                "Izmeni test", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null,
                options,
                options[1]);
        if (dialogResult == JOptionPane.YES_OPTION) {
            new EditTestScreen(test);
            dispose();
        }
    }

    private void deleteTest(TestDTO test) {
        if (inProgress) {
            return;
        }

        Object[] options = {"DA", "NE"};
        int dialogResult = JOptionPane.showOptionDialog(this, 
                "Da li ste sigurni da zelite da obrisete ovaj test?", 
                "Obrisi test", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null,
                options,
                options[1]);
        if (dialogResult == JOptionPane.YES_OPTION) {
            inProgress = true;
            DeleteTestTask task = new DeleteTestTask(test);
            task.execute();
        }
    }

    private class GetQuestionsTask extends SwingWorker<Void, String> {

        private TestDTO test;
        private TestInfoDTO testInfo = null;
        private boolean status = false;
        private String errorMessage = null;

        public GetQuestionsTask(TestDTO test) {
            this.test = test;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Getting test info for test: " + test);

                testInfo = NetworkServicePrivate.getInstance().getTestInfo(test);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to get test info", ex);
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
                logger.info("Get test info successfully: " + testInfo);

                new TestInfoScreen(email, test, testInfo, !finished, TestsScreen.this);
            } else {
                String message = "Dohvatanje informacija o testu nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, JOptionPane.ERROR_MESSAGE);
            }
            inProgress = false;
        }
    }

    private class DeleteTestTask extends SwingWorker<Void, String> {

        private TestDTO test;
        private boolean status = false;
        private String errorMessage = null;

        public DeleteTestTask(TestDTO test) {
            this.test = test;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                logger.info("Deleting test: " + test);

                NetworkServicePrivate.getInstance().deleteTest(test);

                status = true;
            } catch (Exception ex) {
                logger.error("Failed to delete test", ex);
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
                logger.info("Deleted test successfully");

                tests.getTests().remove(test);
                if (tests.getTests().size() == 0) {
                    dispose();
                }

                getContentPane().removeAll();
                setupScreen();
                validate();

                displayMessage("Brisanje testa", "Test uspesno obrisan", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String message = "Brisanje pitanja nije uspelo";
                if (errorMessage != null) {
                    message += ":\n" + errorMessage;
                }
                displayMessage("Greska", message, JOptionPane.ERROR_MESSAGE);
            }
            inProgress = false;
        }
    }

    public void finishTest(TestDTO test) {
        logger.info("Finished test: " + test);

        tests.getTests().remove(test);
        if (tests.getTests().size() == 0) {
            dispose();
        }

        getContentPane().removeAll();
        setupScreen();
        validate();
    }
}

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.etf.teststudent.dto.AnswerDTO;
import rs.etf.teststudent.dto.QuestionDTO;
import rs.etf.teststudent.dto.TestDTO;
import rs.etf.teststudent.dto.TipPitanja;
import rs.etf.teststudent.network.NetworkServicePrivate;
import rs.etf.teststudent.utils.Configuration;

public class CreateQuestionScreen extends JFrame {

    Logger logger = LoggerFactory.getLogger(CreateQuestionScreen.class);

    private static final String questionTypes[] = {TipPitanja.POPUNI.name(), TipPitanja.ZAOKRUZI.name()};

    private String email;
    private TestDTO test;
    private QuestionDTO question;
    private TestInfoScreen testInfoScreen;

    private JLabel labelFinishButton;
    private Component inputQuestionPanel;
    private JPanel chooseTypePanel;
    private TipPitanja selectedType;
    private Component addedAnswersPanel;

    private List<String> answers;

    private JTextArea inputQuestion;

    public CreateQuestionScreen(String email, TestDTO test, QuestionDTO question, TestInfoScreen testInfoScreen) {
        this.email = email;
        this.test = test;
        this.question = question;
        this.testInfoScreen = testInfoScreen;
        setupScreen();
    }

    public void setupScreen() {
        if (question != null && question.getId() != null) {
            setTitle(Configuration.APP_NAME + Configuration.EDIT_QUESTION_SCREEN_SUFFIX);
        } else {
            setTitle(Configuration.APP_NAME + Configuration.CREATE_QUESTION_SCREEN_SUFFIX);
        }

        String initQuestion;
        answers = new LinkedList<>();
        if (question == null) {
            initQuestion = "";
            selectedType = TipPitanja.POPUNI;
        } else {
            initQuestion = question.getQuestion();
            selectedType = question.getType();
            if (selectedType == TipPitanja.ZAOKRUZI) {
                for (AnswerDTO answerDTO : question.getAnswers()) {
                    answers.add(answerDTO.getAnswer());
                }
            }
        }

        /*
            Finish button
         */
        labelFinishButton = new JLabel("Sacuvaj", JLabel.CENTER);
        labelFinishButton.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        labelFinishButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                saveQuestion();
            }
        });

        /*
            Question
         */
        JLabel labelQuestion = new JLabel("Unesite pitanje:");
        labelQuestion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputQuestion = new JTextArea(initQuestion, 0, 2);
        inputQuestion.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inputQuestion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputQuestion.setLineWrap(true);
        inputQuestion.setWrapStyleWord(true);
        inputQuestion.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(labelQuestion, BorderLayout.NORTH);
        panel.add(new JScrollPane(inputQuestion), BorderLayout.CENTER);

        inputQuestionPanel = new JScrollPane(panel);

        /*
            Type
         */
        JLabel labelType = new JLabel("Tip pitanja:");
        labelType.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JComboBox typeList = new JComboBox(questionTypes);
        typeList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        typeList.setSelectedIndex(selectedType.ordinal());
        typeList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = typeList.getSelectedIndex();
                if (index >= 0 && index < questionTypes.length) {
                    updateScreen(index);
                }
            }
        });

        typeList.setMaximumSize(new Dimension(200, 30));

        labelType.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeList.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooseTypePanel = new JPanel();
        chooseTypePanel.setLayout(new BoxLayout(chooseTypePanel, BoxLayout.Y_AXIS));
        chooseTypePanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        chooseTypePanel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        chooseTypePanel.add(Box.createVerticalGlue());
        chooseTypePanel.add(labelType);
        chooseTypePanel.add(typeList);
        chooseTypePanel.add(Box.createVerticalGlue());

        updateScreen(typeList.getSelectedIndex());

        setMinimumSize(new Dimension(600, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                updateScreen(null);
            }
        });
    }

    private void updateScreen(Integer index) {
        if (index != null) {
            if (index == 0) {
                selectedType = TipPitanja.POPUNI;
            } else {
                selectedType = TipPitanja.ZAOKRUZI;
            }
        }

        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel answersPanel = new JPanel(new GridLayout(answers.size(), 1));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel editAnswersPanel = new JPanel(new GridLayout(answers.size(), 1));
        editAnswersPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        for (String s : answers) {
            JLabel label = new JLabel(s, JLabel.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            answersPanel.add(label);

            label = new JLabel("Obrisi");
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            editAnswersPanel.add(label);

            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    answers.remove(s);
                    updateScreen(null);
                }
            });
        }

        JLabel label = new JLabel("Dodaj ponudjeni odgovor", JLabel.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String answer = JOptionPane.showInputDialog(CreateQuestionScreen.this, "Ponudjeni odgovor:", "");
                if (answer != null && !answer.isEmpty()) {
                    answers.add(answer);
                    updateScreen(null);
                }
            }
        });

        JPanel listAnswersPanel = new JPanel();
        listAnswersPanel.setLayout(new BoxLayout(listAnswersPanel, BoxLayout.X_AXIS));
        listAnswersPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        listAnswersPanel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listAnswersPanel.add(Box.createHorizontalGlue());
        listAnswersPanel.add(Box.createHorizontalGlue());
        listAnswersPanel.add(answersPanel);
        listAnswersPanel.add(editAnswersPanel);
        listAnswersPanel.add(Box.createHorizontalGlue());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(listAnswersPanel), BorderLayout.CENTER);
        panel.add(label, BorderLayout.SOUTH);

        int width = (int) (1.0 * getWidth() / 5 * 4);
        int height = (int) (1.0 * getHeight() / 3);
        if (answers.size() > 0) {
            panel.setPreferredSize(new Dimension(width, height));
        }

        addedAnswersPanel = panel;

        add(labelFinishButton, BorderLayout.NORTH);
        add(inputQuestionPanel, BorderLayout.CENTER);
        add(chooseTypePanel, BorderLayout.EAST);

        if (selectedType == TipPitanja.ZAOKRUZI) {
            add(addedAnswersPanel, BorderLayout.SOUTH);
        }

        validate();
    }

    private void saveQuestion() {
        UpdateQuestionTask task = new UpdateQuestionTask();
        task.execute();
    }

    private class UpdateQuestionTask extends SwingWorker<Void, String> {

        private QuestionDTO oldQuestion;
        private boolean created = false;
        private String errorMessage = null;

        public UpdateQuestionTask() {

        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                if (question == null) {
                    question = new QuestionDTO();
                }

                oldQuestion = question;
                question = question.clone();

                question.setIdTest(test.getId());
                question.setQuestion(inputQuestion.getText());
                question.setType(selectedType);

                List<AnswerDTO> givenAnswerDTOs = new ArrayList<>(answers.size());

                for (String answer : answers) {
                    AnswerDTO givenAnswerDTO = new AnswerDTO();
                    givenAnswerDTO.setIdQuestion(question.getId());
                    givenAnswerDTO.setAnswer(answer);
                    givenAnswerDTOs.add(givenAnswerDTO);
                }

                question.setAnswers(givenAnswerDTOs);

                logger.info("Sending update question request: " + question);

                question = NetworkServicePrivate.getInstance().updateQuestion(question);

                created = true;
            } catch (Exception ex) {
                logger.error("Failed to update question", ex);
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
                logger.info("Question is updated");

                if (testInfoScreen != null) {
                    testInfoScreen.addQuestion(oldQuestion, question);
                }

                displayMessage("Pitanje sacuvano", "Pitanje je usesno sacuvano", true);
            } else {
                String message = "Cuvanje pitanja nije uspelo";
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

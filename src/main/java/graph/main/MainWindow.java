package graph.main;

import com.google.common.collect.ImmutableMap;
import com.mxgraph.swing.mxGraphComponent;
import exceptions.CycleFoundException;
import exceptions.VertexDuplicationException;
import exceptions.VertexIdUndefinedException;
import graph.FileGraphProviderImpl;
import graph.GraphProvider;
import graph.Utils;
import graph.model.*;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

public class MainWindow extends JFrame {

    private final FileDialog fileDialog;
    private final MxGraphWrapper mxGraphWrapper;
    private long lastActionMillis = -1;
    private final Map<Integer, LevelsProgressText> textPanesByCourseLevel = new HashMap<>();

    public MainWindow() throws IOException {
        mxGraphWrapper = getGraphWrapper();
        mxGraphComponent graphComponent = initGraphComponent();
        fileDialog = new FileDialog(this, "Выберите файл", FileDialog.SAVE);
        ((JComponent)getComponent(0)).getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK),
                "revertLastAction");
        ((JComponent)getComponent(0)).getActionMap().put("revertLastAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mxGraphWrapper.revertPreviousAction();
                refreshCourseLevelProgress();
            }
        });
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton exportResultButton = new JButton("Сохранить выбор...");
        exportResultButton.setVisible(true);
        exportResultButton.addActionListener(event -> {
            List<String> selectedCourses = mxGraphWrapper.getSelectedCourses();
            fileDialog.setVisible(true);
            try {
                String filename = fileDialog.getFile();
                Files.write(Paths.get(filename), selectedCourses, StandardOpenOption.CREATE);
                Utils.showInfoMessage("Выбранные курсы успешно записаны в файл " + filename);
            } catch (IOException e) {
                Utils.showErrorMessage("Ошибка записи");
            }
        });
        buttonPanel.add(exportResultButton);
        RequirementsProvider requirementsProvider = new FileRequirementsProviderImpl();
        Map<String, ? extends Map<Integer, Integer>> req = requirementsProvider.getRequirements();
        String firstKey = req.keySet().iterator().next();
        addRestrictionsInfo(buttonPanel, req.get(firstKey));
        JComboBox<String> comboBox = new JComboBox<>(req.keySet().toArray(new String[0]));
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(comboBox);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                Map<Integer, Integer> restrictions = req.get(selected);
                updateRestrictions(restrictions);
            }
        });
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(graphComponent);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        getContentPane().add(panel);
    }

    private void refreshCourseLevelProgress() {
        Map<Integer, Integer> stats = mxGraphWrapper.getCourseLevelStats();
        stats.forEach((key, value) -> {
            LevelsProgressText text = textPanesByCourseLevel.get(key);
            if (text != null) {
                text.setCoursesSelectedCount(value);
            }
        });
    }

    private void onLeftClick(MouseEvent e, mxGraphComponent graphComponent) {
        Optional<CourseVertex> target = Utils.getCourseVertexFromEvent(e, graphComponent);
        Map<Integer, Integer> changeOfCoursesByLevel = ImmutableMap.of();
        if (target.isPresent()) {
            CourseVertex clickedVertex = target.get();
            if (!clickedVertex.isChoosen()) {
                changeOfCoursesByLevel = mxGraphWrapper.selectAllParents(clickedVertex);
            } else {
                changeOfCoursesByLevel = mxGraphWrapper.disselectAllChildren(clickedVertex);
            }
        }
        refreshCourseLevelProgress();
    }

    private void onMove(MouseEvent e, mxGraphComponent graphComponent) {
        Optional<CourseVertex> target = Utils.getCourseVertexFromEvent(e, graphComponent);
        if (target.isPresent() && !mxGraphWrapper.hasOpaqueVertices()) {
            long currentTime = System.currentTimeMillis();
            if (lastActionMillis + 100 < currentTime) {
                lastActionMillis = currentTime;
                mxGraphWrapper.highlightAllChildren(target.get());
            }
        }
        if (!target.isPresent() && mxGraphWrapper.hasOpaqueVertices()) {
            mxGraphWrapper.removeOpaqueVertices();
        }
    }

    private MxGraphWrapper getGraphWrapper() {
        GraphProvider graphProvider = new FileGraphProviderImpl("courseGraph");
        DirectedAcyclicGraph<CourseVertex, CourseEdge> rawGraph = null;
        try {
            rawGraph = graphProvider.getGraph();
        } catch (IOException e) {
            Utils.showErrorMessageAndExit("Файл с графом курсов не найден");
        } catch (VertexDuplicationException e) {
            Utils.showErrorMessageAndExit("Дупликация вершины с id = " + e.getDuplicatedId());
        } catch (VertexIdUndefinedException e) {
            Utils.showErrorMessageAndExit("Не найдено вершины с id = " + e.getId());
        } catch (CycleFoundException e) {
            Utils.showErrorMessageAndExit("Ребро \"" + e.getFrom() + "\"->\"" + e.getTo() + "\" создает цикл");
        }

        return new MxGraphWrapper(rawGraph);
    }

    private mxGraphComponent initGraphComponent() {
        mxGraphComponent graphComponent = mxGraphWrapper.getGraphComponent();
        graphComponent.setEnabled(true);
        graphComponent.setConnectable(false);
        graphComponent.setWheelScrollingEnabled(true);
        graphComponent.getGraphHandler().setMarkerEnabled(true);
        graphComponent.getGraphControl().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMove(e, graphComponent);
            }
        });
        graphComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        graphComponent.setAlignmentY(TOP_ALIGNMENT);
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onLeftClick(e, graphComponent);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMove(e, graphComponent);
            }
        });
        graphComponent.setSize(new Dimension(750, 750));
        graphComponent.setBorder(null);
        return graphComponent;
    }

    private void addRestrictionsInfo(JPanel panel, Map<Integer, Integer> restrictions) {
        Map<Integer, Integer> courseLevelStats = mxGraphWrapper.getCourseLevelStats();
        JTextArea helpTextArea = new JTextArea("Статистика по количеству выбранных курсов");
        helpTextArea.setFont(helpTextArea.getFont().deriveFont(LevelsProgressText.getFontSize()));
        helpTextArea.setOpaque(false);
        helpTextArea.setEditable(false);
        panel.add(helpTextArea);
        panel.add(Box.createHorizontalStrut(20));
        restrictions.forEach((key, value) -> {
            LevelsProgressText textArea = new LevelsProgressText(key, mxGraphWrapper::highlightVerticesWithCourseLevel,
                    mxGraphWrapper::removeOpaqueVertices, value);
            textPanesByCourseLevel.put(key, textArea);
            panel.add(Box.createHorizontalStrut(20));
            panel.add(textArea);
        });
    }

    private void updateRestrictions(Map<Integer, Integer> restrictions) {
        restrictions.forEach((key, value) -> {
            LevelsProgressText text = textPanesByCourseLevel.get(key);
            if (text != null) {
                text.setCoursesRequired(value);
            }
        });
    }

}

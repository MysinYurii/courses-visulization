package graph.main;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mxgraph.swing.mxGraphComponent;
import exceptions.CycleFoundException;
import exceptions.VertexDuplicationException;
import exceptions.VertexIdUndefinedException;
import graph.*;
import graph.model.CourseEdge;
import graph.model.CourseVertex;
import graph.model.LevelsProgressText;
import graph.model.MxGraphWrapper;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

/**
 * Created by Yury on 05.12.2016.
 */
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
        List<Integer> tempList = Lists.newArrayList(0,0,12,3,0,0,0);
        for (int i = 1; i <= 5; ++i) {
            LevelsProgressText textArea = new LevelsProgressText(i, mxGraphWrapper::highlightVerticesWithCourseLevel,
                    mxGraphWrapper::removeOpaqueVertices, tempList.get(i));
            textPanesByCourseLevel.put(i, textArea);
            buttonPanel.add(Box.createHorizontalStrut(20));
            buttonPanel.add(textArea);
        }
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(graphComponent);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        getContentPane().add(panel);
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
        changeOfCoursesByLevel.forEach((key, value) -> textPanesByCourseLevel.get(key).addCourses(value));
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

}

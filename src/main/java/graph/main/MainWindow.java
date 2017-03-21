package graph.main;

import com.mxgraph.swing.mxGraphComponent;
import exceptions.CycleFoundException;
import exceptions.VertexDuplicationException;
import exceptions.VertexIdUndefinedException;
import graph.*;
import graph.model.CourseEdge;
import graph.model.CourseVertex;
import graph.model.MxGraphWrapper;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;

/**
 * Created by Yury on 05.12.2016.
 */
public class MainWindow extends JFrame {

    private final MxGraphWrapper mxGraphWrapper;

    public MainWindow() throws IOException {
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
        mxGraphWrapper = new MxGraphWrapper(rawGraph);
        mxGraphComponent graphComponent = mxGraphWrapper.getGraphComponent();
        graphComponent.setEnabled(false);
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onLeftClick(e, graphComponent);
                }
            }
        });
        graphComponent.setSize(new Dimension(750, 750));
        graphComponent.setBorder(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(graphComponent, BorderLayout.CENTER);
        JButton exportResutButton = new JButton("Экспортировать");
        exportResutButton.setVisible(true);
        exportResutButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                List<String> selectedCourses = mxGraphWrapper.getSelectedCourses();
                try {
                    Files.write(Paths.get("selectedCourses.txt"), selectedCourses, StandardOpenOption.CREATE);
                    Utils.showInfoMessage("Выбранные курсы успешно записаны в файл selectedCources.txt");
                } catch (IOException e) {
                    Utils.showErrorMessage("Ошибка записи");
                }
            }
        });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(exportResutButton);
        getContentPane().setLayout(new FlowLayout(FlowLayout.LEADING));
        getContentPane().add(panel);
    }

    private void onLeftClick(MouseEvent e, mxGraphComponent graphComponent) {
        Optional<CourseVertex> target = Utils.getCourseVertexFromEvent(e, graphComponent);
        if (target.isPresent()) {
            CourseVertex clickedVertex = target.get();
            clickedVertex.switchChoise();
            if (clickedVertex.isChoosen()) {
                mxGraphWrapper.selectAllParents(clickedVertex);
            } else {
                mxGraphWrapper.disselectAllChilds(clickedVertex);
            }
        }
    }


}

package graph;

import com.google.common.collect.ImmutableSet;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import exceptions.VertexDuplicationException;
import org.jgrapht.DirectedGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by Yury on 05.12.2016.
 */
public class MainWindow extends JFrame {

    private static final int VERTEX_WIDTH = 200;
    private static final int VERTEX_HEIGHT = 60;
    private static final String STYLE_NAME = "MyStyle";
    private static final String RESOURCE_PREFIX = "src/main/resources";

    private final Map<String, CourseVertex> courseVerticesByCourseName = new HashMap<>();
    private final Map<CourseVertex, mxCell> mxCellsByCourseVertices = new HashMap<>();
    private DirectedGraph<CourseVertex, CourseEdge> rawGraph;
    private mxGraph graph = new mxGraph();


    public MainWindow() throws IOException {
        initStyle();
        initGraph();
        // define layout
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setFineTuning(false);
        layout.setUseBoundingBox(true);
        layout.setOrientation(SwingConstants.WEST);

        layout.execute(graph.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setEnabled(false);
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    onClick(e, graphComponent);
                }
            }
        });
        graphComponent.setSize(new Dimension(1000, 1000));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(graphComponent, BorderLayout.CENTER);
    }

    private void selectAllParents(CourseVertex vertex) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(vertex);
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(true);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, updatedVertex.getColor(),
                    new Object[]{mxCellsByCourseVertices.get(updatedVertex)});
            Set<CourseVertex> incomingVertices = rawGraph.incomingEdgesOf(updatedVertex).stream()
                    .map(CourseEdge::getFrom)
                    .filter(v -> !v.isChoosen())
                    .collect(Collectors.toSet());
            disselectedChilds.addAll(incomingVertices);
        }
    }

    private void onClick(MouseEvent e, mxGraphComponent graphComponent) {
        Object target = graphComponent.getCellAt(e.getX(), e.getY());
        if (target instanceof mxCell) {
            mxCell cell = (mxCell) target;
            Object clickedObject = cell.getValue();
            if (clickedObject instanceof CourseVertex) {
                CourseVertex clickedVertex = (CourseVertex) clickedObject;
                clickedVertex.switchChoise();
                if (clickedVertex.isChoosen()) {
                    selectAllParents(clickedVertex);
                } else {
                    disselectAllChilds(clickedVertex);
                }
            }
        }
    }

    public static void main(String[] args) {
        MainWindow frame;
        try {
            frame = new MainWindow();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(2000, 2000);
            frame.setVisible(true);
        } catch (Exception e) {
            showErrorMessage(e.toString());
            System.exit(-1);
        }
    }

    private void insertVertex(CourseVertex courseVertex) {
        courseVerticesByCourseName.put(courseVertex.getCourseName(), courseVertex);
        mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, courseVertex,
                0, 0, VERTEX_WIDTH, VERTEX_HEIGHT, STYLE_NAME);
        mxCellsByCourseVertices.put(courseVertex, cell);
    }

    private void insertEdge(CourseEdge edge) {
        CourseVertex vertexFrom = edge.getFrom();
        CourseVertex vertexTo = edge.getTo();
        graph.insertEdge(graph.getDefaultParent(), null, "",
                mxCellsByCourseVertices.get(vertexFrom), mxCellsByCourseVertices.get(vertexTo));
    }

    private void disselectAllChilds(CourseVertex vertex) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(vertex);
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(false);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, updatedVertex.getColor(),
                    new Object[]{mxCellsByCourseVertices.get(updatedVertex)});
            Set<CourseVertex> childVertices = rawGraph.outgoingEdgesOf(updatedVertex).stream()
                    .map(CourseEdge::getTo)
                    .filter((edge) -> edge.isChoosen())
                    .collect(Collectors.toSet());
            disselectedChilds.addAll(childVertices);
        }
    }

    private void initStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_EDITABLE, false);
        style.put(mxConstants.STYLE_RESIZABLE, false);
        style.put(mxConstants.STYLE_AUTOSIZE, true);
        style.put(mxConstants.STYLE_MOVABLE, false);
        style.put(mxConstants.STYLE_CLONEABLE, false);
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(253, 146, 37)));

        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle(STYLE_NAME, style);
        graph.setStylesheet(stylesheet);
    }

    private void initGraph() throws IOException {
        GraphProvider graphProvider = new FileGraphProviderImpl(RESOURCE_PREFIX + "/courseGraph");
        rawGraph = graphProvider.getGraph();

        graph.getModel().beginUpdate();
        try {
            for (CourseVertex vertex : rawGraph.vertexSet()) {
                insertVertex(vertex);
            }
            for (CourseEdge edge : rawGraph.edgeSet()) {
                insertEdge(edge);
            }

        } finally {
            graph.getModel().endUpdate();
        }
    }

    private static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

}

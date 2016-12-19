package graph;

import com.google.common.collect.ImmutableSet;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Queue;

/**
 * Created by Yury on 05.12.2016.
 */
public class MainWindow extends JFrame {

    private static final int VERTEX_WIDTH = 200;
    private static final int VERTEX_HEIGHT = 60;

    private final Map<String, CourseVertex> verticesByCourseName = new HashMap<>();
    private final Map<CourseVertex, Set<CourseVertex>> requiredCourses = new HashMap<>();
    private final Map<CourseVertex, Set<CourseVertex>> childCourses = new HashMap<>();
    mxGraph graph;


    public MainWindow() {
        graph = new mxGraph();

        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_EDITABLE, false);
        style.put(mxConstants.STYLE_RESIZABLE, false);
        style.put(mxConstants.STYLE_AUTOSIZE, true);
        style.put(mxConstants.STYLE_MOVABLE, false);
        style.put(mxConstants.STYLE_CLONEABLE, false);
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(253, 146, 37)));
//
//        Map<String, Object> edge = new HashMap<String, Object>();
//        edge.put(mxConstants.STYLE_MOVABLE, false);
//        edge.put(mxConstants.STYLE_BENDABLE, false);
//        edge.put(mxConstants.STYLE_RESIZABLE, false);
//        edge.put(mxConstants.STYLE_EDITABLE, false);
//        edge.put(mxConstants.STYLE_FOLDABLE, false);
//        edge.put(mxConstants.STYLE_ROUNDED, true);
//        edge.put(mxConstants.STYLE_ORTHOGONAL, false);
//        edge.put(mxConstants.STYLE_EDGE, "elbowEdgeStyle");
//        edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
//        edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
//        edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
//        edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
//        edge.put(mxConstants.STYLE_STROKECOLOR, "#000000"); // default is #6482B9
//        edge.put(mxConstants.STYLE_FONTCOLOR, "#446299");


        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle("MyStyle", style);
//        stylesheet.setDefaultEdgeStyle(edge);
        graph.setStylesheet(stylesheet);

        graph.getModel().beginUpdate();
        try {

            insertVertex("a");
            insertVertex("b");
            insertVertex("c");
            insertVertex("d");
            insertVertex("g");
            insertVertex("h");
            insertVertex("e");
            insertVertex("f");
            insertVertex("v");
            insertVertex("n");
            insertVertex("2");

            insertEdge("a", "b");
            insertEdge("a", "c");
            insertEdge("c", "d");
            insertEdge("e", "d");
            insertEdge("g", "a");
            insertEdge("b", "d");
            insertEdge("h", "e");

        } finally {
            graph.getModel().endUpdate();
        }
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
            public void mouseClicked(MouseEvent e) {
                Object target = graphComponent.getCellAt(e.getX(), e.getY());
                if (target instanceof mxCell) {
                    mxCell cell = (mxCell) target;
                    CourseVertex vertex = (CourseVertex) cell.getValue();
                    vertex.switchChoise();
                    if (vertex.isChoosen()) {
                        selectAllParents(vertex);
                    } else {
                        disselectAllChilds(vertex);
                    }

                }
            }
        });
        graphComponent.setSize(new Dimension(2000, 2000));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(graphComponent, BorderLayout.CENTER);
    }

    private void selectAllParents(CourseVertex vertex) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(vertex);
        disselectedChilds.addAll(requiredCourses.computeIfAbsent(vertex, (key) -> new HashSet<>()));
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(true);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, updatedVertex.getColor(),
                    new Object[]{updatedVertex.getGraphCell()});
            disselectedChilds.addAll(requiredCourses.getOrDefault(updatedVertex, ImmutableSet.of()));
        }
    }

    public static void main(String[] args) {
        MainWindow frame = new MainWindow();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2000, 2000);
        frame.setVisible(true);
    }

    private void insertVertex(String text) {
        CourseVertex courseVertex = new CourseVertex(text);
        verticesByCourseName.put(text, courseVertex);
        mxICell cell = (mxICell) graph.insertVertex(graph.getDefaultParent(), null, courseVertex,
                0, 0, VERTEX_WIDTH, VERTEX_HEIGHT, "MyStyle");
        courseVertex.setGraphCell(cell);
    }

    private void insertEdge(String from, String to) {
        CourseVertex vertexFrom = verticesByCourseName.get(from);
        CourseVertex vertexTo = verticesByCourseName.get(to);
        graph.insertEdge(graph.getDefaultParent(), null, "", vertexFrom.getGraphCell(), vertexTo.getGraphCell());
        requiredCourses.computeIfAbsent(vertexTo, (key) -> new HashSet<>()).add(vertexFrom);
        childCourses.computeIfAbsent(vertexFrom, (key) -> new HashSet<>()).add(vertexTo);
    }

    private void disselectAllChilds(CourseVertex vertex) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(vertex);
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(false);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, updatedVertex.getColor(),
                    new Object[]{updatedVertex.getGraphCell()});
            mxICell updatedCell = updatedVertex.getGraphCell();
            disselectedChilds.addAll(childCourses.getOrDefault(updatedVertex, ImmutableSet.of()));
        }
    }

}

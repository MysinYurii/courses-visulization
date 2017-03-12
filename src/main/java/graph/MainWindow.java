package graph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
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
    private static final int VERTEX_HEIGHT = 20;
    private static final String STYLE_NAME = "MyStyle";

    private final Map<CourseVertex, mxCell> mxCellsByCourseVertices = new HashMap<>();
    private DirectedGraph<CourseVertex, CourseEdge> rawGraph;
    private mxGraph graph = new mxGraph();


    public MainWindow() throws IOException {
        initStyle();
        initGraph();
        initLayout();

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
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
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(graphComponent, BorderLayout.CENTER);
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
        GraphProvider graphProvider = new FileGraphProviderImpl("courseGraph");
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

    private void initLayout() {
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setUseBoundingBox(true);
        layout.setInterHierarchySpacing(15.0);
        layout.setIntraCellSpacing(15.0);
        layout.setInterRankCellSpacing(150.0);
        layout.setOrientation(SwingConstants.WEST);
        layout.execute(graph.getDefaultParent());
    }

    private void onLeftClick(MouseEvent e, mxGraphComponent graphComponent) {
        Optional<CourseVertex> target = Utils.getCourseVertexFromEvent(e, graphComponent);
        if (target.isPresent()) {
            CourseVertex clickedVertex = target.get();
            clickedVertex.switchChoise();
            if (clickedVertex.isChoosen()) {
                selectAllParents(clickedVertex);
            } else {
                disselectAllChilds(clickedVertex);
            }
        }
    }

    private void insertVertex(CourseVertex courseVertex) {
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
        updateGraph(vertex, true, false);
    }

    private void selectAllParents(CourseVertex vertex) {
        updateGraph(vertex, false, true);
    }

    private void updateGraph(CourseVertex root, boolean workWithChilds, boolean desiredValue) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(root);
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(desiredValue);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, updatedVertex.getColor(),
                    new Object[]{mxCellsByCourseVertices.get(updatedVertex)});
            Set<CourseEdge> firedEdges = workWithChilds ? rawGraph.outgoingEdgesOf(updatedVertex)
                    : rawGraph.incomingEdgesOf(updatedVertex);
            Set<CourseVertex> incomingVertices = firedEdges.stream()
                    .map(edge -> edge.getFrom().equals(updatedVertex) ? edge.getTo() : edge.getFrom())
                    .filter(v -> v.isChoosen() == !desiredValue)
                    .collect(Collectors.toSet());
            disselectedChilds.addAll(incomingVertices);
        }
    }

}

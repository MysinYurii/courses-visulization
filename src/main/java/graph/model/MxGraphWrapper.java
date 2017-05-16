package graph.model;

import com.google.common.base.Preconditions;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyModel;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import graph.Utils;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by Yury on 21.03.2017.
 */
public class MxGraphWrapper {

    private static final int VERTEX_WIDTH = 200;
    private static final int VERTEX_HEIGHT = 20;

    private static final String STYLE_NAME = "MyStyle";
    private final Map<CourseVertex, mxCell> mxCellsByCourseVertices = new HashMap<>();
    private DirectedAcyclicGraph<CourseVertex, CourseEdge> rawGraph;
    private mxGraph graph = new mxGraph();

    public MxGraphWrapper(DirectedAcyclicGraph<CourseVertex, CourseEdge> rawGraph) {
        Preconditions.checkNotNull(rawGraph);
        this.rawGraph = rawGraph;
        initStyle();
        initGraph();
        initLayout();
    }

    public mxGraphComponent getGraphComponent() {
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setSize(1000, 1000);
        return graphComponent;
    }

    public List<String> getSelectedCourses() {
        return rawGraph.vertexSet().stream()
                .filter(CourseVertex::isChoosen)
                .map(CourseVertex::getCourseName)
                .collect(Collectors.toList());
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

    private void initGraph() {

        graph.getModel().beginUpdate();
        try {
            for (CourseVertex vertex : rawGraph.vertexSet()) {
                insertVertex(vertex);
            }
            for (CourseEdge edge : rawGraph.edgeSet()) {
                insertEdge(edge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graph.getModel().endUpdate();
        }
    }

    private void initLayout() {
        MxHierarchicalLayoutExtention layout = new MxHierarchicalLayoutExtention(graph);
        layout.setUseBoundingBox(true);
        layout.setInterHierarchySpacing(15.0);
        layout.setIntraCellSpacing(15.0);
        layout.setInterRankCellSpacing(100);
        layout.setOrientation(SwingConstants.WEST);
        layout.execute(graph.getDefaultParent());
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

    public void disselectAllChilds(CourseVertex vertex) {
        updateGraph(vertex, true, false);
    }

    public void selectAllParents(CourseVertex vertex) {
        updateGraph(vertex, false, true);
    }

    private void updateGraph(CourseVertex root, boolean workWithChilds, boolean desiredValue) {
        Queue<CourseVertex> disselectedChilds = new LinkedList<>();
        disselectedChilds.add(root);
        while (!disselectedChilds.isEmpty()) {
            CourseVertex updatedVertex = disselectedChilds.remove();
            updatedVertex.setIsChoosen(desiredValue);
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, Utils.getMxColorOfVertex(updatedVertex),
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

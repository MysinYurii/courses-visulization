package graph.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.*;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import graph.Utils;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

public class MxGraphWrapper {

    private static final int VERTEX_WIDTH = 200;
    private static final int VERTEX_HEIGHT = 20;

    private static final String STYLE_NAME = "MyStyle";
    private final Map<CourseVertex, mxCell> mxCellsByCourseVertices = new HashMap<>();
    private final Multimap<Integer, mxCell> mxCellByCourseLevel = HashMultimap.create();
    private DirectedAcyclicGraph<CourseVertex, CourseEdge> rawGraph;
    private Stack<List<CourseVertex>> changesHistory;
    private mxGraphComponent graphComponent = null;
    private Set<Object> opaqueCells = new HashSet<>();
    private mxGraph graph = new mxGraph();

    public MxGraphWrapper(DirectedAcyclicGraph<CourseVertex, CourseEdge> rawGraph) {
        Preconditions.checkNotNull(rawGraph);
        this.rawGraph = rawGraph;
        this.changesHistory = new Stack<>();
        initStyle();
        initGraph();
        initLayout();
    }

    public mxGraphComponent getGraphComponent() {
        if (graphComponent == null) {
            graphComponent = new mxGraphComponent(graph);
            graphComponent.setSize(1000, 1200);
        }
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
        style.put(mxConstants.STYLE_DELETABLE, false);
        style.put(mxConstants.STYLE_AUTOSIZE, true);
        style.put(mxConstants.STYLE_MOVABLE, false);
        style.put(mxConstants.STYLE_FOLDABLE, false);
        style.put(mxConstants.STYLE_CLONEABLE, false);
        style.put(mxConstants.STYLE_BENDABLE, false);
        style.put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(new Color(253, 146, 37)));

        mxStylesheet stylesheet = graph.getStylesheet();
        stylesheet.putCellStyle(STYLE_NAME, style);
        graph.setCellsLocked(true);
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
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setUseBoundingBox(true);
        layout.setInterHierarchySpacing(15.0);
        layout.setIntraCellSpacing(10.0);
        layout.setInterRankCellSpacing(50.0);
        layout.setOrientation(SwingConstants.WEST);
        layout.execute(graph.getDefaultParent());
    }

    private void insertVertex(CourseVertex courseVertex) {
        mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, courseVertex,
                0, 0, VERTEX_WIDTH, VERTEX_HEIGHT, STYLE_NAME);
        mxCellsByCourseVertices.put(courseVertex, cell);
        mxCellByCourseLevel.put(courseVertex.getCourseLevel(), cell);
    }

    private void insertEdge(CourseEdge edge) {
        CourseVertex vertexFrom = edge.getFrom();
        CourseVertex vertexTo = edge.getTo();
        graph.insertEdge(graph.getDefaultParent(), null, "",
                mxCellsByCourseVertices.get(vertexFrom), mxCellsByCourseVertices.get(vertexTo));
    }

    public Map<Integer, Integer> disselectAllChildren(CourseVertex vertex) {
        return updateGraph(vertex, true, false, null);
    }

    public Map<Integer, Integer> selectAllParents(CourseVertex vertex) {
         return updateGraph(vertex, false, true, null);
    }

    public void highlightAllChildren(CourseVertex vertex) {
        setOpacityForCells(10);
        updateGraph(vertex, false, null, true);
        updateGraph(vertex, true, null, true);
    }

    public void highlightVerticesWithCourseLevel(int courseLevel) {
        setOpacityForCells(10);
        setOpacityForCells(100, mxCellByCourseLevel.get(courseLevel).toArray());
    }

    public void revertPreviousAction() {
        if (!changesHistory.isEmpty()) {
            List<CourseVertex> lastChange = changesHistory.pop();
            for (CourseVertex changedVertex : lastChange) {
                changedVertex.switchChoise();
                repaintVertex(changedVertex);
            }
        }
    }

    private Map<Integer, Integer> updateGraph(CourseVertex root, boolean workWithChildren, Boolean desiredValue, Boolean makeOpaque) {
        Queue<CourseVertex> disselectedChildren = new LinkedList<>();
        List<CourseVertex> changedVertices = new ArrayList<>();
        Map<Integer, Integer> result = new HashMap<>();
        int courseCountIncrement = Boolean.TRUE.equals(desiredValue) ? 1 : Boolean.FALSE.equals(desiredValue) ? -1 : 0;
        disselectedChildren.add(root);
        while (!disselectedChildren.isEmpty()) {
            CourseVertex updatedVertex = disselectedChildren.remove();
            if (desiredValue != null) {
                if (updatedVertex.isChoosen() != desiredValue) {
                    changedVertices.add(updatedVertex);
                    updatedVertex.setIsChoosen(desiredValue);
                    result.put(updatedVertex.getCourseLevel(), result.getOrDefault(updatedVertex.getCourseLevel(), 0) + courseCountIncrement);
                }
                repaintVertex(updatedVertex);
            }
            if (TRUE.equals(makeOpaque)) {
                highlightVertex(updatedVertex, workWithChildren);
            }
            Set<CourseEdge> firedEdges = workWithChildren ? rawGraph.outgoingEdgesOf(updatedVertex)
                    : rawGraph.incomingEdgesOf(updatedVertex);
            Set<CourseVertex> incomingVertices = firedEdges.stream()
                    .map(edge -> edge.getFrom().equals(updatedVertex) ? edge.getTo() : edge.getFrom())
                    .collect(Collectors.toSet());
            disselectedChildren.addAll(incomingVertices);
        }
        if (!changedVertices.isEmpty()) {
            changesHistory.push(changedVertices);
        }
        return result;
    }

    private void repaintVertex(CourseVertex repaintedVertex) {
        graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, Utils.getMxColorOfVertex(repaintedVertex),
                new Object[]{mxCellsByCourseVertices.get(repaintedVertex)});
    }

    private void highlightVertex(CourseVertex repaintedVertex, boolean workWithChildren) {
        mxCell repaintedMxCell = mxCellsByCourseVertices.get(repaintedVertex);
        if (repaintedMxCell != null) {
            setOpacityForCells(100, new Object[]{repaintedMxCell});
            for (int i = 0; i < repaintedMxCell.getEdgeCount(); ++i) {
                mxCell edge = (mxCell) repaintedMxCell.getEdgeAt(i);
                if ((workWithChildren && repaintedMxCell.equals(edge.getSource()))
                        || (!workWithChildren && repaintedMxCell.equals(edge.getTarget())))
                    setOpacityForCells(100, new Object[]{edge});
            }
        }
    }

    public boolean hasOpaqueVertices() {
        return !opaqueCells.isEmpty();
    }

    public void removeOpaqueVertices() {
        setOpacityForCells(100);
    }

    private void setOpacityForCells(int opacity) {
        Object[] allEdges = graph.getAllEdges(new Object[] {graph.getDefaultParent()} );
        setOpacityForCells(opacity, allEdges);
        Object[] allVertices = mxCellsByCourseVertices.values().toArray();
        setOpacityForCells(opacity, allVertices);
    }

    private void setOpacityForCells(int opacity, Object[] affectedCells) {
        if (opacity == 100) {
            opaqueCells.removeAll(Lists.newArrayList(affectedCells));
        } else {
            opaqueCells.addAll(Lists.newArrayList(affectedCells));
        }
        graph.setCellStyles(mxConstants.STYLE_OPACITY, String.valueOf(opacity), affectedCells);
        graph.setCellStyles(mxConstants.STYLE_TEXT_OPACITY, String.valueOf(opacity), affectedCells);
    }

}

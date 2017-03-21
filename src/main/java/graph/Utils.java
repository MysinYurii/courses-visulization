package graph;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import graph.model.CourseEdge;
import graph.model.CourseVertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Yury on 11.03.2017.
 */
public class Utils {

    private static final String selectedColor = mxUtils.getHexColorString(new Color(48, 255, 132));
    private static final String defaultColor = mxUtils.getHexColorString(new Color(255, 255, 255));

    public static Optional<CourseVertex> getCourseVertexFromEvent(MouseEvent event, mxGraphComponent graphComponent) {
        Object target = graphComponent.getCellAt(event.getX(), event.getY());
        CourseVertex resultVertex = null;
        if (target instanceof mxCell) {
            mxCell targetCell = (mxCell) target;
            if (targetCell.getValue() instanceof CourseVertex) {
                resultVertex = (CourseVertex) targetCell.getValue();
            }
        }
        return Optional.ofNullable(resultVertex);
    }

    public static void showErrorMessageAndExit(String message) {
        showErrorMessage(message);
        System.exit(-1);
    }

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static String getMxColorOfVertex(CourseVertex vertex) {
        if (vertex == null) return null;
        return vertex.isChoosen() ? selectedColor : defaultColor;
    }

    public static void showInfoMessage(String info) {
        JOptionPane.showMessageDialog(new JFrame(), info, "", JOptionPane.INFORMATION_MESSAGE);
    }
}

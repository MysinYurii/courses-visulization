package graph;


import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Created by Yury on 11.03.2017.
 */
public class Utils {

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

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}

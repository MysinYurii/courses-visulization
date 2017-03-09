package graph;

import com.google.common.base.Objects;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxUtils;
import org.jgraph.JGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Yury on 04.12.2016.
 */
public class CourseVertex {
    private final String courseName;
    private static final String selectedColor = mxUtils.getHexColorString(new Color(48, 255, 132));
    private static final String defaultColor = mxUtils.getHexColorString(new Color(255, 255, 255));
    private boolean isChoosen;

    public CourseVertex(String courseName) {
        this.courseName = courseName;
        isChoosen = false;
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public void switchChoise() {
        isChoosen = !isChoosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseVertex that = (CourseVertex) o;
        return Objects.equal(courseName, that.courseName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(courseName);
    }

    public void setIsChoosen(boolean isChoosen) {
        this.isChoosen = isChoosen;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getColor() {
        return isChoosen ? selectedColor : defaultColor;
    }

    @Override
    public String toString() {
        return courseName;
    }

}

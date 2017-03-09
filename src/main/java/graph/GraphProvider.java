package graph;

import org.jgrapht.DirectedGraph;

/**
 * Created by Yury on 19.02.2017.
 */
public interface GraphProvider {
    DirectedGraph<CourseVertex, CourseEdge> getGraph();
}

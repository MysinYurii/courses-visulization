package graph;

import graph.model.CourseEdge;
import graph.model.CourseVertex;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.io.IOException;

/**
 * Created by Yury on 19.02.2017.
 */
public interface GraphProvider {
    DirectedAcyclicGraph<CourseVertex, CourseEdge> getGraph() throws IOException;
}

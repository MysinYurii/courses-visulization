package graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import exceptions.VertexDuplicationException;
import exceptions.VertexIdUndefinedException;
import javafx.util.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Yury on 19.02.2017.
 */
public class FileGraphProviderImpl implements GraphProvider {

    private final DirectedGraph<CourseVertex, CourseEdge> courseGraph;
    private static final Pattern vertexDescriptionFormat = Pattern.compile("(\\d)+:[^:]*");
    private static final Pattern edgesDescriptionFormat = Pattern.compile("(\\d)+-(\\d)+(,(\\d)+)*");


    public FileGraphProviderImpl(String fileName) throws IOException {
        courseGraph = getGraphFromFile(fileName);
    }

    @Override
    public DirectedGraph<CourseVertex, CourseEdge> getGraph() {
        return courseGraph;
    }

    private DirectedGraph<CourseVertex, CourseEdge> getGraphFromFile(String fileName) throws IOException {
        DirectedGraph<CourseVertex, CourseEdge> result = new DirectedAcyclicGraph<>(new ClassBasedEdgeFactory<>(CourseEdge.class));
        List<String> fileLines = Files.readAllLines(Paths.get(fileName));
        Map<Integer, CourseVertex> vertexDesriptions = new HashMap<>();
        Map<Integer, Set<Integer>> edgesDesriptions = new HashMap<>();
        for (String fileLine : fileLines) {
            fileLine = fileLine.trim();
            String[] tokens = fileLine.split(":");
            if (vertexDescriptionFormat.matcher(fileLine).matches()) {
                Pair<Integer, String> vertexAddition = parseVertexAdditionLine(fileLine);
                Integer id = vertexAddition.getKey();
                CourseVertex newCourseVertex = new CourseVertex(vertexAddition.getValue());
                if (vertexDesriptions.putIfAbsent(id, newCourseVertex) != null) {
                    throw new VertexDuplicationException(id);
                }
                result.addVertex(newCourseVertex);
            } else if (edgesDescriptionFormat.matcher(fileLine).matches()) {
                Pair<Integer, List<Integer>> edgesAddition = parseEdgesAdditionLine(fileLine);
                Integer fromId = edgesAddition.getKey();
                List<Integer> toIds = edgesAddition.getValue();
                edgesDesriptions.computeIfAbsent(fromId, key -> Sets.newHashSet()).addAll(toIds);
            }
        }
        for (HashMap.Entry<Integer, Set<Integer>> entry : edgesDesriptions.entrySet()) {
            CourseVertex fromVertex = vertexDesriptions.get(entry.getKey());
            if (fromVertex != null) {
                for (Integer toId : entry.getValue()) {
                    CourseVertex toVertex = vertexDesriptions.get(toId);
                    if (toVertex != null) {
                        result.addEdge(fromVertex, toVertex, new CourseEdge(fromVertex, toVertex));
                    } else {
                        throw new VertexIdUndefinedException(toId);
                    }
                }
            } else {
                throw new VertexIdUndefinedException(entry.getKey());
            }
        }
        return result;
    }

    private Pair<Integer, String> parseVertexAdditionLine(String line) {
        String[] tokens = line.split(":");
        Integer id = Integer.valueOf(tokens[0]);
        return new Pair<>(id, tokens[1]);
    }

    private Pair<Integer, List<Integer>> parseEdgesAdditionLine(String line) {
        int delimeterPos = line.indexOf('-');
        String fromIdStr = line.substring(0, delimeterPos);
        Integer fromId = Integer.valueOf(fromIdStr);
        String[] toIdsStr = line.substring(delimeterPos + 1).split(",");
        List<Integer> toIds = Lists.newArrayList(toIdsStr).stream()
                .map(Integer::valueOf).collect(Collectors.toList());
        return new Pair<>(fromId, toIds);
    }
}

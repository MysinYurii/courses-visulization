package graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import exceptions.CycleFoundException;
import exceptions.VertexDuplicationException;
import exceptions.VertexIdUndefinedException;
import graph.model.CourseEdge;
import graph.model.CourseVertex;
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

    private final String filename;
    private static final Pattern vertexDescriptionFormat = Pattern.compile("(\\d)+;[^:]*;(\\d)");
    private static final Pattern edgesDescriptionFormat = Pattern.compile("(\\d)+-(\\d)+(,(\\d)+)*");


    public FileGraphProviderImpl(String filename) {
        this.filename = filename;
    }

    @Override
    public DirectedAcyclicGraph<CourseVertex, CourseEdge> getGraph() throws IOException {
        return getGraphFromFile(filename);
    }

    private DirectedAcyclicGraph<CourseVertex, CourseEdge> getGraphFromFile(String fileName) throws IOException {
        DirectedAcyclicGraph<CourseVertex, CourseEdge> result = new DirectedAcyclicGraph<>(new ClassBasedEdgeFactory<>(CourseEdge.class));
        List<String> fileLines = Files.readAllLines(Paths.get(fileName));
        Map<Integer, CourseVertex> vertexDesriptions = new HashMap<>();
        Map<Integer, Set<Integer>> edgesDesriptions = new HashMap<>();
        for (String fileLine : fileLines) {
            fileLine = fileLine.trim();
            String[] tokens = fileLine.split(":");
            if (vertexDescriptionFormat.matcher(fileLine).matches()) {
                Pair<Integer, CourseVertex> vertexAddition = parseVertexAdditionLine(fileLine);
                Integer id = vertexAddition.getKey();
                if (vertexDesriptions.putIfAbsent(id, vertexAddition.getValue()) != null) {
                    throw new VertexDuplicationException(id);
                }
                result.addVertex(vertexAddition.getValue());
            } else if (edgesDescriptionFormat.matcher(fileLine).matches()) {
                Pair<Integer, List<Integer>> edgesAddition = parseEdgesAdditionLine(fileLine);
                Integer toId = edgesAddition.getKey();
                List<Integer> fromIds = edgesAddition.getValue();
                edgesDesriptions.computeIfAbsent(toId, key -> Sets.newHashSet()).addAll(fromIds);
            }
        }
        for (HashMap.Entry<Integer, Set<Integer>> entry : edgesDesriptions.entrySet()) {
            CourseVertex toVertex = vertexDesriptions.get(entry.getKey());
            if (toVertex != null) {
                for (Integer toId : entry.getValue()) {
                    CourseVertex fromVertex = vertexDesriptions.get(toId);
                    if (fromVertex != null) {
                        try {
                            result.addEdge(fromVertex, toVertex, new CourseEdge(fromVertex, toVertex));
                        } catch (IllegalArgumentException e) {
                            if (e.getCause() instanceof DirectedAcyclicGraph.CycleFoundException) {
                                throw new CycleFoundException(fromVertex, toVertex);
                            }
                        }
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

    private Pair<Integer, CourseVertex> parseVertexAdditionLine(String line) {
        String[] tokens = line.split(";");
        Integer id = Integer.valueOf(tokens[0]);
        return new Pair<>(id, new CourseVertex(tokens[1], 5 - Integer.valueOf(tokens[2])));
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

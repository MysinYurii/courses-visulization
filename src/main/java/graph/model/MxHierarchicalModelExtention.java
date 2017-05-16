package graph.model;

import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyModel;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyNode;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyRank;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Yury on 14.05.2017.
 */
public class MxHierarchicalModelExtention extends mxGraphHierarchyModel {

    public MxHierarchicalModelExtention(mxHierarchicalLayout layout, Object[] vertices, List<Object> roots, Object parent, boolean deterministic, boolean scanRanksFromSinks) {
        super(layout, vertices, roots, parent, deterministic, scanRanksFromSinks);
    }

    @Override
    public void initialRank() {
        ranks = new HashMap<>();
        this.maxRank = 50;
        for (int i = 0; i < this.maxRank; ++i) {
            ranks.put(i, new mxGraphHierarchyRank());
        }
        vertexMapper.forEach((key, value) -> {
            mxCell cell = (mxCell) key;
            CourseVertex courseVertex = (CourseVertex) cell.getValue();
            mxGraphHierarchyNode node = new mxGraphHierarchyNode(cell);
            ranks.get(courseVertex.getCourseLevel()).add(node);
            node.minRank = courseVertex.getCourseLevel();
            node.maxRank = courseVertex.getCourseLevel();
            node.connectsAsSource = new HashSet<>();
            node.connectsAsTarget = new HashSet<>();
            vertexMapper.put(cell, node);
        });
        edgeMapper.forEach((key,value) -> {
            mxGraphHierarchyNode target = vertexMapper.get(value.target.cell);
            mxGraphHierarchyNode source = vertexMapper.get(value.source.cell);
            value.minRank = Math.min(source.minRank, target.minRank);
            value.minRank = Math.max(source.maxRank, target.maxRank);
            value.target = target;
            value.source = source;
            target.connectsAsTarget.add(value);
            source.connectsAsSource.add(value);
        });
    }

}

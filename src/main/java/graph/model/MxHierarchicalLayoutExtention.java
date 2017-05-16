package graph.model;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.view.mxGraph;

/**
 * Created by Yury on 14.05.2017.
 */
public class MxHierarchicalLayoutExtention extends mxHierarchicalLayout
{
    public MxHierarchicalLayoutExtention(mxGraph graph) {
        super(graph);
    }

    public MxHierarchicalLayoutExtention(mxGraph graph, int orientation) {
        super(graph, orientation);
    }

    @Override
    public void layeringStage() {
        this.model = new MxHierarchicalModelExtention(this, model.getVertexMapper().keySet().toArray(), roots, parent, deterministic, true);
        model.initialRank();
    }
}

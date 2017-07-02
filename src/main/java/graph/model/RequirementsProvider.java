package graph.model;

import java.util.Map;

/**
 * Created by Yury on 02.07.2017.
 */
public interface RequirementsProvider {
    Map<String, ? extends Map<Integer, Integer>> getRequirements();
}

package graph.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FileRequirementsProviderImpl implements RequirementsProvider {

    private final ImmutableMap<String, ImmutableMap<Integer, Integer>> requirements;

    private final Pattern requirementNameTemplate = Pattern.compile("[^:]*:");
    private final Pattern restrictionTemplate = Pattern.compile("(\\d+)-(\\d+)");

    public FileRequirementsProviderImpl() throws IOException {
        ImmutableMap.Builder<String, ImmutableMap<Integer, Integer>> requirementsBuilder = ImmutableMap.builder();
        List<String> fileLines = Files.readAllLines(Paths.get("requirements"));
        List<Integer> namesPositions = Lists.newArrayList();
        for (int i = 0; i < fileLines.size(); ++i) {
            if (requirementNameTemplate.matcher(fileLines.get(i)).matches()) {
                namesPositions.add(i);
            }
        }
        namesPositions.add(fileLines.size());
        for (int i = 0; i < namesPositions.size() - 1; ++i) {
            ImmutableMap.Builder<Integer, Integer> restrictions = ImmutableMap.builder();
            String restrictionName = fileLines.get(namesPositions.get(i));
            restrictionName = restrictionName.substring(0, restrictionName.length() - 1);
            for (int j = namesPositions.get(i); j < namesPositions.get(i + 1); ++j) {
                if (restrictionTemplate.matcher(fileLines.get(j)).matches()) {
                    String[] tokens = fileLines.get(j).split("-");
                    restrictions.put(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]));
                }
            }
            requirementsBuilder.put(restrictionName, restrictions.build());
        }
        requirements = requirementsBuilder.build();
    }

    @Override
    public Map<String, ? extends Map<Integer, Integer>> getRequirements() {
        return null;
    }
}

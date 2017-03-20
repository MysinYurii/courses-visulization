package graph;

import exceptions.CycleFoundException;
import exceptions.VertexDuplicationException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Yury on 19.02.2017.
 */
public class FileGraphProviderImplTest {

    private final String RESOURCE_PREFIX = "src/test/resources/";

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void itShouldParseValidFile() throws IOException {
        FileGraphProviderImpl graphProvider = new FileGraphProviderImpl(RESOURCE_PREFIX + "validGraph");
        System.out.println(graphProvider.getGraph());
    }

    @Test(expected = IOException.class)
    public void itShouldFailIfFileNotPresent() throws IOException {
        FileGraphProviderImpl graphProvider = new FileGraphProviderImpl(RESOURCE_PREFIX + "nonExistantFile");
        System.out.println(graphProvider.getGraph());
    }

    @Test(expected = VertexDuplicationException.class)
    public void itShouldFailIfTwoVerticesAreDuplicated() throws IOException {
        FileGraphProviderImpl graphProvider = new FileGraphProviderImpl(RESOURCE_PREFIX + "duplicatedVertex");
        System.out.println(graphProvider.getGraph());
    }

    @Test(expected = CycleFoundException.class)
    public void itShouldFailIfGraphHasCycle() throws IOException {
        FileGraphProviderImpl graphProvider = new FileGraphProviderImpl(RESOURCE_PREFIX + "cyclicGraph");
        System.out.println(graphProvider.getGraph());
    }

}
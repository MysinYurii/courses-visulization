package exceptions;

/**
 * Created by Yury on 28.02.2017.
 */
public class VertexIdUndefinedException extends RuntimeException {

    private final int id;

    public VertexIdUndefinedException(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

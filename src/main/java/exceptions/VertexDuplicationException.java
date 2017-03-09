package exceptions;

/**
 * Created by Yury on 28.02.2017.
 */
public class VertexDuplicationException extends RuntimeException {
    private final int duplicatedId;

    public VertexDuplicationException(int duplicatedId) {
        this.duplicatedId = duplicatedId;
    }

    public int getDuplicatedId() {
        return duplicatedId;
    }
}

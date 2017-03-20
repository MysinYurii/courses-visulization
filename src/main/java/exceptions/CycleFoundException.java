package exceptions;

import graph.model.CourseVertex;

/**
 * Created by Yury on 21.03.2017.
 */
public class CycleFoundException extends RuntimeException {
    private final CourseVertex from;
    private final CourseVertex to;

    public CycleFoundException(CourseVertex from, CourseVertex to) {
        this.from = from;
        this.to = to;
    }

    public CourseVertex getFrom() {
        return from;
    }

    public CourseVertex getTo() {
        return to;
    }
}

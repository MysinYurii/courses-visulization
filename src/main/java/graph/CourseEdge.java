package graph;

import com.google.common.base.Objects;

/**
 * Created by Yury on 09.03.2017.
 */
public class CourseEdge {

    private final CourseVertex from;
    private final CourseVertex to;

    public CourseEdge(CourseVertex from, CourseVertex to) {
        this.from = from;
        this.to = to;
    }

    public CourseVertex getTo() {
        return to;
    }

    public CourseVertex getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseEdge that = (CourseEdge) o;
        return Objects.equal(from, that.from) &&
                Objects.equal(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from, to);
    }
}

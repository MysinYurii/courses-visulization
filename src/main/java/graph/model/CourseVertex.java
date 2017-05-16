package graph.model;

import com.google.common.base.Objects;

/**
 * Created by Yury on 04.12.2016.
 */
public class CourseVertex {
    private final String courseName;
    private boolean isChoosen;
    private int courseLevel = 0;

    public CourseVertex(String courseName, int courseLevel) {
        this.courseName = courseName;
        this.courseLevel = courseLevel;
        isChoosen = false;
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public void switchChoise() {
        isChoosen = !isChoosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseVertex that = (CourseVertex) o;
        return Objects.equal(courseName, that.courseName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(courseName);
    }

    public void setIsChoosen(boolean isChoosen) {
        this.isChoosen = isChoosen;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCourseLevel() {
        return courseLevel;
    }

    @Override
    public String toString() {
        return courseName;
    }

}

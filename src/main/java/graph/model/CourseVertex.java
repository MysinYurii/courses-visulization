package graph.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class CourseVertex implements Serializable {
    private final String courseName;
    private boolean isChoosen;
    private final int courseLevel;

    public CourseVertex(String courseName, int courseLevel) {
        this.courseName = courseName;
        this.courseLevel = courseLevel;
        isChoosen = false;
    }

    public int getCourseLevel() {
        return courseLevel;
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public void switchChoise() {
        isChoosen = !isChoosen;
    }

    public void setIsChoosen(boolean isChoosen) {
        this.isChoosen = isChoosen;
    }

    public String getCourseName() {
        return courseName;
    }

    @Override
    public String toString() {
        return courseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseVertex that = (CourseVertex) o;
        return courseLevel == that.courseLevel &&
                Objects.equal(courseName, that.courseName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(courseName, courseLevel);
    }

}

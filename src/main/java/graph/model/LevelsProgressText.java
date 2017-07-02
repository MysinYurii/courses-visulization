package graph.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Yury on 01.07.2017.
 */
public class LevelsProgressText extends JTextArea {

    private final int levelNumber;
    private static final float fontSize = 20.0F;
    private int coursesSelected = 0;
    private int coursesRequired = 10;

    public LevelsProgressText(int levelNumber, Consumer<Integer> onMouseEnter, Runnable onMouseExit, int coursesRequired) {
        super();
        this.levelNumber = levelNumber;
        this.coursesRequired = coursesRequired;
        setEditable(false);
        setFont(getFont().deriveFont(fontSize));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                onMouseEnter.accept(levelNumber);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onMouseExit.run();
            }
        });
        refreshText();
    }

    public static float getFontSize() {
        return fontSize;
    }

    public boolean hasMinimumNumberOfCourses() {
        return coursesSelected >= coursesRequired;
    }

    public void refreshText() {
        setText(levelNumber + ": " + coursesSelected + "/" + coursesRequired);
        if (hasMinimumNumberOfCourses()) {
            setBackground(Color.GREEN);
        } else {
            setBackground(Color.RED);
        }
    }

    public void setCoursesRequired(int coursesRequired) {
        this.coursesRequired = coursesRequired;
        this.refreshText();
    }

    public void addCourses(int coursesNum) {
        this.coursesSelected += coursesNum;
        this.refreshText();
    }

    public void removeCourses(int coursesNum) {
        this.coursesSelected -= coursesNum;
        this.refreshText();
    }

}

package graph.main;

import graph.Utils;

import javax.swing.*;
import java.awt.*;

public class Main {


    public static void main(String[] args) {
        MainWindow frame;
        try {
            frame = new MainWindow();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } catch (Exception e) {
            Utils.showErrorMessageAndExit(e.toString());
            System.exit(-1);
        }
    }
}

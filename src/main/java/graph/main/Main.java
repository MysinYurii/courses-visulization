package graph.main;

import graph.Utils;

import javax.swing.*;

/**
 * Created by Yury on 12.03.2017.
 */
public class Main {


    public static void main(String[] args) {
        MainWindow frame;
        try {
            frame = new MainWindow();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(frame.getMaximumSize());
            frame.setVisible(true);
        } catch (Exception e) {
            Utils.showErrorMessageAndExit(e.toString());
            System.exit(-1);
        }
    }
}

package graph;

import javax.swing.*;

/**
 * Created by Yury on 12.03.2017.
 */
public class Main {


    public static void main(String[] args) {
        MainWindow frame;
        try {
            frame = new MainWindow();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1500, 1500);
            frame.setVisible(true);
        } catch (Exception e) {
            Utils.showErrorMessage(e.toString());
            System.exit(-1);
        }
    }
}

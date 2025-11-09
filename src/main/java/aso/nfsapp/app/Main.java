package aso.nfsapp.app;

import aso.nfsapp.controller.NfsController;
import aso.nfsapp.view.MainWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            NfsController controller = new NfsController(mainWindow);
            controller.initialize();
            mainWindow.setVisible(true);
        });
    }
}

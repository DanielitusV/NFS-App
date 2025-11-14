package aso.nfsapp.app;

import aso.nfsapp.controller.NfsController;
import aso.nfsapp.view.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Punto de entrada de la aplicación NFS App.
 * Inicializa la ventana principal, el controlador y maneja el cierre de la aplicación.
 * Verifica cambios sin guardar antes de permitir que el usuario cierre la app.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            NfsController controller = new NfsController(mainWindow);
            controller.initialize();
            
            // Agregar listener para confirmar cierre si hay cambios sin guardar
            mainWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (mainWindow.hasUnsavedChanges()) {
                        int confirm = JOptionPane.showConfirmDialog(
                            mainWindow,
                            "Hay cambios sin guardar. ¿Desea salir sin guardar?",
                            "Cambios Pendientes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (confirm != JOptionPane.YES_OPTION) {
                            // No cerrar la ventana, mantener abierta
                            return;
                        }
                    }
                    // Si no hay cambios o el usuario confirma, cerrar
                    System.exit(0);
                }
            });
            
            mainWindow.setVisible(true);
        });
    }
}

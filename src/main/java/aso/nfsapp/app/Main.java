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
                        // Crear diálogo con 3 opciones: Guardar y Salir, Salir sin Guardar, Cancelar
                        String[] options = {"Guardar y Salir", "Salir sin Guardar", "Cancelar"};
                        int choice = JOptionPane.showOptionDialog(
                            mainWindow,
                            "Hay cambios sin guardar en /etc/exports\n\n" +
                            "¿Qué desea hacer?",
                            "Cambios Pendientes",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[0]); // Default = "Guardar y Salir"
                        
                        if (choice == 0) {
                            // "Guardar y Salir" - Guardar cambios y cerrar
                            controller.saveAndApplyChanges();
                            System.exit(0);
                        } else if (choice == 1) {
                            // "Salir sin Guardar" - Cerrar sin guardar
                            System.exit(0);
                        }
                        // choice == 2 o CLOSED_OPTION (X) = "Cancelar" - No hacer nada, mantener abierta
                        return;
                    }
                    // Si no hay cambios, cerrar directamente
                    System.exit(0);
                }
            });
            
            mainWindow.setVisible(true);
        });
    }
}

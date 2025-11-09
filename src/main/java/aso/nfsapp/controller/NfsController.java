package aso.nfsapp.controller;

import aso.nfsapp.model.ExportEntry;
import aso.nfsapp.view.MainWindow;

import javax.swing.JList;
import javax.swing.JOptionPane;

public class NfsController {
    private final MainWindow mainWindow;

    public NfsController(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void initialize() {
        mainWindow.getAddDirectoryButton().addActionListener(e -> addExportEntry());
    }

    private void addExportEntry() {
        String directoryPath = JOptionPane.showInputDialog(mainWindow, "Carpeta a compartir:", "/opt/docus");
        if (directoryPath == null || directoryPath.isBlank()) return;

        String clientHost = JOptionPane.showInputDialog(mainWindow, "Host (ej: * o 192.168.1.0/24):", "*");
        if (clientHost == null || clientHost.isBlank()) return;

        String exportOptions = JOptionPane.showInputDialog(mainWindow, "Opciones(rw,ro,sync,...):", "rw");
        if (exportOptions == null || exportOptions.isBlank()) return;

        ExportEntry newEntry = new ExportEntry(directoryPath, clientHost, exportOptions);
        mainWindow.getDirectoryListPanel().getDirectoryListModel().addElement(newEntry.toString());
    }

    private void removeExportEntry() {
        JList<String> directoryList = mainWindow.getDirectoryListPanel().getDirectoryList();
        int selectedIndex = directoryList.getSelectedIndex();
        if(selectedIndex != -1) {
            mainWindow.getDirectoryListPanel().getDirectoryListModel().remove(selectedIndex);
        }
    }

    private void simulateSaveToFile() {
        JOptionPane.showMessageDialog(mainWindow,
        "Simulación: los cambios se guardarían en /etc/exports\n(próximamente se implementará escritura real).");
    }
}

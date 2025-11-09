package aso.nfsapp.view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

public class MainWindow extends JFrame {
    private final DirectoryListPanel directoryList = new DirectoryListPanel();
    private final JButton addDirectoryButton = new JButton("Agregar carpeta");
    private final JButton removeDirectoryButton = new JButton("Eliminar carpeta");
    private final JButton saveChangesButton = new JButton("Guardar cambios");

    public MainWindow() {
        super ("NFS App - Configurar /etc/exports");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addDirectoryButton);
        buttonPanel.add(removeDirectoryButton);
        buttonPanel.add(saveChangesButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(directoryList, BorderLayout.CENTER);
    }

    public DirectoryListPanel getDirectoryListPanel() {
        return this.directoryList;
    }
    public JButton getAddDirectoryButton() {
        return addDirectoryButton;
    }
    public JButton getRemoveDirectoryButton() {
        return removeDirectoryButton;
    }
    public JButton getSaveChangesButton() {
        return saveChangesButton;
    }

}

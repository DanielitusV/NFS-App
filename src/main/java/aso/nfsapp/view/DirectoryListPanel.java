package aso.nfsapp.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Panel que muestra la lista de directorios a exportar.
 * Permite seleccionar un directorio para ver y configurar sus reglas de host.
 */
public class DirectoryListPanel extends JPanel {
    private final DefaultListModel<String> directoryListModel = new DefaultListModel<>();
    private final JList<String> directoryList = new JList<>(directoryListModel);
    
    private final JButton addDirectoryButton = new JButton("Agregar Directorio");
    private final JButton editDirectoryButton = new JButton("Editar Directorio");
    private final JButton deleteDirectoryButton = new JButton("Eliminar Directorio");

    public DirectoryListPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Directorios a Exportar",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        
        // Panel superior con lista
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel subTitle = new JLabel("Directorios");
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(subTitle, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(directoryList), BorderLayout.CENTER);
        
        // Panel inferior con botones centrados
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(addDirectoryButton);
        buttonPanel.add(editDirectoryButton);
        buttonPanel.add(deleteDirectoryButton);
        
        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public DefaultListModel<String> getDirectoryListModel() {
        return this.directoryListModel;
    }
    public JList<String> getDirectoryList() {
        return this.directoryList;
    }
    
    public JButton getAddDirectoryButton() {
        return addDirectoryButton;
    }
    public JButton getEditDirectoryButton() {
        return editDirectoryButton;
    }
    public JButton getDeleteDirectoryButton() {
        return deleteDirectoryButton;
    }
}

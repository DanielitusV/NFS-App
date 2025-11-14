package aso.nfsapp.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;

/**
 * Panel que muestra la lista de directorios a exportar.
 * Permite seleccionar un directorio para ver y configurar sus reglas de host.
 */
public class DirectoryListPanel extends JPanel {
    private final DefaultListModel<String> directoryListModel = new DefaultListModel<>();
    private final JList<String> directoryList = new JList<>(directoryListModel);

    public DirectoryListPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Directorios a Exportar",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        
        JLabel subTitle = new JLabel("Directorios");
        subTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(subTitle, BorderLayout.NORTH);
        add(new JScrollPane(directoryList), BorderLayout.CENTER);
    }

    public DefaultListModel<String> getDirectoryListModel() {
        return this.directoryListModel;
    }
    public JList<String> getDirectoryList() {
        return this.directoryList;
    }
}

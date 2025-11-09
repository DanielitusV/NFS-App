package aso.nfsapp.view;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;

public class DirectoryListPanel extends JPanel {
    private final DefaultListModel<String> directoryListModel = new DefaultListModel<>();
    private final JList<String> directoryList = new JList<>(directoryListModel);

    public DirectoryListPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Carpetas compartidas:"), BorderLayout.NORTH);
        add(new JScrollPane(directoryList), BorderLayout.CENTER);
    }

    public DefaultListModel<String> getDirectoryListModel() {
        return this.directoryListModel;
    }
    public JList<String> getDirectoryList() {
        return this.directoryList;
    }
}

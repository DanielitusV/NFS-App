package aso.nfsapp.view;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

public class HostRulesTablePanel extends JPanel {
    private final DefaultTableModel rulesTableModel =
        new DefaultTableModel(new Object[]{"Host Wild Card", "Options"}, 0);
    private final JTable rulesTable = new JTable(rulesTableModel);

    private final JButton addHostButton = new JButton("Add Host");
    private final JButton editHostButton = new JButton("Edit");
    private final JButton deleteHostButton = new JButton("Delete");

    public HostRulesTablePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Rules"), BorderLayout.NORTH);
        add(new JScrollPane(rulesTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.add(addHostButton);
        actions.add(editHostButton);
        actions.add(deleteHostButton);
        add(actions, BorderLayout.SOUTH);
    }

    public DefaultTableModel getRulesTableModel() {
        return rulesTableModel;
    }
    public JTable getRulesTable() {
        return rulesTable;
    }
    public JButton getAddHostButton() {
        return addHostButton;
    }
    public JButton getEditHostButton() {
        return editHostButton;
    }
    public JButton getDeleteHostButton() {
        return deleteHostButton;
    }
}

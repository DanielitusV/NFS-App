package aso.nfsapp.view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * Panel que muestra las reglas de host (permisos) para el directorio seleccionado.
 * Contiene tabla de hosts, opciones y botones para agregar, editar y eliminar reglas.
 */
public class HostRulesTablePanel extends JPanel {
    private final DefaultTableModel rulesTableModel =
        new DefaultTableModel(new Object[]{"Host Wild Card", "Opciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editar directamente en la tabla
            }
        };
    private final JTable rulesTable = new JTable(rulesTableModel);

    private final JButton addHostButton = new JButton("Agregar Host");
    private final JButton editHostButton = new JButton("Editar");
    private final JButton deleteHostButton = new JButton("Eliminar");

    public HostRulesTablePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Opciones de Exportación para el Directorio Seleccionado",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));

        // Configurar tabla
        rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesTable.setRowHeight(25);
        rulesTable.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(rulesTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.add(addHostButton);
        actions.add(editHostButton);
        actions.add(deleteHostButton);
        add(actions, BorderLayout.SOUTH);
    }

    public void updateTitle(String directoryPath) {
        if (directoryPath != null && !directoryPath.isEmpty()) {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Opciones de Exportación para: " + directoryPath,
                TitledBorder.LEFT,
                TitledBorder.TOP
            ));
        } else {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Opciones de Exportación para el Directorio Seleccionado",
                TitledBorder.LEFT,
                TitledBorder.TOP
            ));
        }
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

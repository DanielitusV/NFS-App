package aso.nfsapp.controller;

import aso.nfsapp.model.ExportEntry;
import aso.nfsapp.service.ExportFileManager;
import aso.nfsapp.service.SystemPaths;
import aso.nfsapp.view.MainWindow;
import aso.nfsapp.model.HostRule;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;

public class NfsController {
    private final MainWindow ui;
    private final ExportFileManager fileManager = new ExportFileManager();

    /* Estado: cada directorio tiene sus reglas */
    private final List<ExportEntry> directories = new ArrayList<>();

    public NfsController(MainWindow window) {
        this.ui = window;
    }

    public void initialize() {
        /* Botones directorios */
        ui.getAddDirectoryButton().addActionListener(e -> addDirectory());
        ui.getEditDirectoryButton().addActionListener(e -> editDirectory());
        ui.getDeleteDirectoryButton().addActionListener(e -> deleteDirectory());

        /* Selección directorio -> Refrescan las reglas */
        ui.getDirectoryListPanel().getDirectoryList().addListSelectionListener(e -> {
           if (!e.getValueIsAdjusting()) refreshRulesTable();
        });

        /* Botones reglas */
        ui.getHostRulesPanel().getAddHostButton().addActionListener(e -> addRule());
        ui.getHostRulesPanel().getEditHostButton().addActionListener(e -> editRule());
        ui.getHostRulesPanel().getDeleteHostButton().addActionListener(e -> deleteRule());

        /* Guardar y aplicar */
        ui.getSaveApplyButton().addActionListener(e -> saveTempAndShowNextSteps());
    }

    private ExportEntry currentEntry() {
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        return index >= 0 && index < directories.size() ? directories.get(index) : null;
    }

    private void addDirectory() {
        String dir = JOptionPane.showInputDialog(ui, "Directory", "opt/docus");
        if (dir == null || dir.isBlank()) {
            return;
        }
        ExportEntry entry = new ExportEntry(dir.trim());
        directories.add(entry);
        ui.getDirectoryListPanel().getDirectoryListModel().addElement(dir.trim());
        ui.getDirectoryListPanel().getDirectoryList().setSelectedIndex(directories.size() - 1);
        refreshRulesTable();
    }

    private void editDirectory() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }
        String dir = JOptionPane.showInputDialog(ui, "Edit directory:", entry.getDirectoryPath());
        if (dir == null || dir.isBlank()) {
            return;
        }
        entry.setDirectoryPath(dir.trim());
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        ui.getDirectoryListPanel().getDirectoryListModel().set(index, dir.trim());
    }

    private void deleteDirectory() {
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        if (index < 0) {
            return;
        }
        directories.remove(index);
        ui.getDirectoryListPanel().getDirectoryListModel().remove(index);
        clearRulesTable();
    }

    private void refreshRulesTable() {
        clearRulesTable();
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }

        DefaultTableModel table = ui.getHostRulesPanel().getRulesTableModel();
        for (HostRule rule : entry.getHostRules()) {
            table.addRow(new Object[] { rule.getHostWildCard(), rule.getOptions() });
        }
    }


    private void clearRulesTable() {
        ui.getHostRulesPanel().getRulesTableModel().setRowCount(0);
    }

    private void addRule() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }
        String host = JOptionPane.showInputDialog(ui,"Host wildcard:", "*");
        if (host == null || host.isBlank()) {
            return;
        }
        String opts = JOptionPane.showInputDialog(ui, "Options:", "rw");
        if (opts == null || opts.isBlank()) {
            return;
        }
        HostRule rule = new HostRule(host.trim(), opts.trim());
        entry.addHostRule(rule);
        refreshRulesTable();
    }

    private void editRule() {
        addRule();
    }

    private void deleteRule() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }
        if (!entry.getHostRules().isEmpty()) {
            HostRule rule = entry.getHostRules().get(0);  // Eliminamos la primera regla como ejemplo
            entry.removeHostRule(rule);
            refreshRulesTable();
        }
    }

    private void saveTempAndShowNextSteps() {
        try {
            List<String> lines = new ArrayList<>();
            for (ExportEntry e: directories) {
                lines.add(e.toString());
            }
            fileManager.writeFile(lines);

            String msg = "Guardado en: " + fileManager.exportsPathString();
            if (SystemPaths.isLinux()) {
                msg += "\nAplica con:\n"
                        + "pkexec cp " + fileManager.exportsPathString() + "\n"
                        + "pkexec systemctl enable --now nfs-server\n"
                        + "pkexec exportfs -rav";
            } else {
                msg += "\n(Windows: solo prueba. En Linux copiará a /etc/exports).";
            }
            JOptionPane.showMessageDialog(ui, msg);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
package aso.nfsapp.controller;

import aso.nfsapp.model.ExportEntry;
import aso.nfsapp.service.ExportFileManager;
import aso.nfsapp.service.SystemPaths;
import aso.nfsapp.view.MainWindow;
import aso.nfsapp.view.HostRuleDialog;
import aso.nfsapp.model.HostRule;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NfsController {
    private final MainWindow ui;
    private final ExportFileManager fileManager = new ExportFileManager();

    /* Estado: cada directorio tiene sus reglas */
    private final List<ExportEntry> directories = new ArrayList<>();

    public NfsController(MainWindow window) {
        this.ui = window;
    }

    public void initialize() {
        /* Cargar archivo /etc/exports existente */
        loadExistingExports();

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
        ui.getSaveApplyButton().addActionListener(e -> saveAndApplyChanges());
    }

    private ExportEntry currentEntry() {
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        return index >= 0 && index < directories.size() ? directories.get(index) : null;
    }

    private void addDirectory() {
        String dir = JOptionPane.showInputDialog(ui, "Ingrese la ruta del directorio a exportar:", "/opt/docus");
        if (dir == null) {
            return; // Usuario canceló
        }
        dir = dir.trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(ui, 
                "El directorio no puede estar vacío. Por favor ingrese una ruta válida.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Asegurar que empiece con /
        if (!dir.startsWith("/")) {
            dir = "/" + dir;
        }
        
        ExportEntry entry = new ExportEntry(dir);
        // Agregar regla por defecto: 192.168.1.0/24 con rw
        entry.addHostRule(new HostRule("192.168.1.0/24", "rw"));
        
        directories.add(entry);
        ui.getDirectoryListPanel().getDirectoryListModel().addElement(dir);
        ui.getDirectoryListPanel().getDirectoryList().setSelectedIndex(directories.size() - 1);
        refreshRulesTable();
    }

    private void editDirectory() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }
        String dir = JOptionPane.showInputDialog(ui, "Editar directorio:", entry.getDirectoryPath());
        if (dir == null) {
            return; // Usuario canceló
        }
        dir = dir.trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(ui, 
                "El directorio no puede estar vacío. Por favor ingrese una ruta válida.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Asegurar que empiece con /
        if (!dir.startsWith("/")) {
            dir = "/" + dir;
        }
        entry.setDirectoryPath(dir);
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        ui.getDirectoryListPanel().getDirectoryListModel().set(index, dir);
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
            ui.updateHostRulesTitle(null);
            return;
        }

        ui.updateHostRulesTitle(entry.getDirectoryPath());

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
            JOptionPane.showMessageDialog(ui, "Seleccione un directorio primero", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        HostRuleDialog.HostRuleDialogResult result = HostRuleDialog.showDialog(ui, null, null);
        if (result != null) {
            HostRule rule = new HostRule(result.host, result.options);
            entry.addHostRule(rule);
            refreshRulesTable();
        }
    }

    private void editRule() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            JOptionPane.showMessageDialog(ui, "Seleccione un directorio primero", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = ui.getHostRulesPanel().getRulesTable().getSelectedRow();
        if (selectedRow < 0 || selectedRow >= entry.getHostRules().size()) {
            JOptionPane.showMessageDialog(ui, "Seleccione una regla para editar", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        HostRule oldRule = entry.getHostRules().get(selectedRow);
        HostRuleDialog.HostRuleDialogResult result = HostRuleDialog.showDialog(
            ui, oldRule.getHostWildCard(), oldRule.getOptions());
        
        if (result != null) {
            entry.removeHostRule(oldRule);
            HostRule newRule = new HostRule(result.host, result.options);
            entry.addHostRule(newRule);
            refreshRulesTable();
        }
    }

    private void deleteRule() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            JOptionPane.showMessageDialog(ui, "Seleccione un directorio primero", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = ui.getHostRulesPanel().getRulesTable().getSelectedRow();
        if (selectedRow < 0 || selectedRow >= entry.getHostRules().size()) {
            JOptionPane.showMessageDialog(ui, "Seleccione una regla para eliminar", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(ui, 
            "¿Eliminar esta regla?", "Confirmar", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            HostRule rule = entry.getHostRules().get(selectedRow);
            entry.removeHostRule(rule);
            refreshRulesTable();
        }
    }

    private void saveAndApplyChanges() {
        try {
            List<String> lines = new ArrayList<>();
            for (ExportEntry e: directories) {
                lines.add(e.toString());
            }
            // Escribir en archivo temporal (Linux) o directo (Windows)
            fileManager.writeFile(lines);

            if (SystemPaths.isLinux()) {
                // En Linux, copiar el temporal a /etc/exports con privilegios
                applyChangesWithPkexec();
            } else {
                // En Windows, solo guardar el archivo
                String msg = "Guardado en: " + fileManager.exportsPathString() +
                            "\n(Windows: solo prueba. En Linux copiará a /etc/exports).";
                JOptionPane.showMessageDialog(ui, msg);
            }
        } catch (Exception e) {
            String errorMsg = "Error al guardar: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "\nCausa: " + e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(ui, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void applyChangesWithPkexec() {
        try {
            String tempFilePath = fileManager.tempExportsPathString();

            // Verificar si el archivo temporal existe
            File tempFile = new File(tempFilePath);
            if (!tempFile.exists()) {
                JOptionPane.showMessageDialog(ui, "El archivo temporal no se encuentra: " + tempFilePath, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Comando para copiar el archivo temporal a /etc/exports
            String copyCommand = "pkexec cp " + tempFilePath + " /etc/exports";
            int copyResult = executeWithRoot(copyCommand);
            if (copyResult != 0) {
                JOptionPane.showMessageDialog(ui, 
                    "Error al copiar el archivo a /etc/exports. Código: " + copyResult + 
                    "\nAsegúrese de tener instalado pkexec y permisos adecuados.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Comando para reiniciar el servidor NFS
            String restartCommand = "pkexec systemctl restart nfs-server";
            int restartResult = executeWithRoot(restartCommand);
            if (restartResult != 0) {
                JOptionPane.showMessageDialog(ui, 
                    "Advertencia: No se pudo reiniciar nfs-server. Código: " + restartResult + 
                    "\nPuede que el servicio no esté instalado o tenga otro nombre (nfs-kernel-server).", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

            // Comando para aplicar las exportaciones
            String exportfsCommand = "pkexec exportfs -rav";
            executeWithRoot(exportfsCommand);

            JOptionPane.showMessageDialog(ui, "Cambios aplicados con éxito\nArchivo copiado a /etc/exports");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ui, "Error al aplicar cambios: " + e.getMessage() +
                "\n\nVerifique que pkexec esté instalado y funcionando.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(ui, "Operación interrumpida: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private int executeWithRoot(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command.split(" ")).start();
        return process.waitFor();
    }

    private void loadExistingExportsLinux() {
        try {
            // Copiar /etc/exports al archivo temporal para poder leerlo sin privilegios
            String tempPath = fileManager.tempExportsPathString();
            File tempFile = new File(tempPath);
            
            // Crear directorio padre si no existe
            if (tempFile.getParentFile() != null) {
                tempFile.getParentFile().mkdirs();
            }
            
            // Si /etc/exports existe, copiarlo al temporal
            if (Files.exists(Path.of("/etc/exports"))) {
                try {
                    String copyCommand = "pkexec cp /etc/exports " + tempPath;
                    int result = executeWithRoot(copyCommand);
                    if (result != 0) {
                        System.err.println("No se pudo copiar /etc/exports. Se iniciará con configuración vacía.");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Error al copiar /etc/exports: " + e.getMessage());
                    return;
                }
            }
            
            // Leer desde el archivo temporal
            if (!tempFile.exists()) {
                return;
            }
            
            List<String> lines = Files.readAllLines(tempFile.toPath());
            parseExportsLines(lines);
            
        } catch (Exception e) {
            System.err.println("Error al cargar /etc/exports: " + e.getMessage());
        }
    }

    private void parseExportsLines(List<String> lines) {
        Pattern linePattern = Pattern.compile("^(\\S+)\\s+(.+)$");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Ignorar líneas vacías y comentarios
            }
            
            Matcher matcher = linePattern.matcher(line);
            if (matcher.matches()) {
                String directory = matcher.group(1);
                String rulesPart = matcher.group(2);
                
                ExportEntry entry = new ExportEntry(directory);
                
                // Parsear las reglas: host1(opts1) host2(opts2)
                Pattern rulePattern = Pattern.compile("(\\S+)\\(([^)]+)\\)");
                Matcher ruleMatcher = rulePattern.matcher(rulesPart);
                
                while (ruleMatcher.find()) {
                    String host = ruleMatcher.group(1);
                    String options = ruleMatcher.group(2);
                    entry.addHostRule(new HostRule(host, options));
                }
                
                directories.add(entry);
                ui.getDirectoryListPanel().getDirectoryListModel().addElement(directory);
            }
        }
        
        if (!directories.isEmpty()) {
            ui.getDirectoryListPanel().getDirectoryList().setSelectedIndex(0);
            refreshRulesTable();
        }
    }

    private void loadExistingExports() {
        // En Linux, intentar leer /etc/exports real con pkexec
        if (SystemPaths.isLinux()) {
            loadExistingExportsLinux();
            return;
        }
        
        // En Windows, leer el archivo de prueba
        Path exportsPath = SystemPaths.exportsPath();
        if (!Files.exists(exportsPath)) {
            return; // No existe el archivo, empezamos desde cero
        }

        try {
            List<String> lines = Files.readAllLines(exportsPath);
            parseExportsLines(lines);
        } catch (IOException e) {
            // Si no se puede leer, simplemente empezamos desde cero
            System.err.println("No se pudo cargar el archivo exports: " + e.getMessage());
        }
    }
}
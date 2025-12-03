package aso.nfsapp.controller;

import aso.nfsapp.model.ExportEntry;
import aso.nfsapp.service.ExportFileManager;
import aso.nfsapp.service.SystemPaths;
import aso.nfsapp.view.MainWindow;
import aso.nfsapp.view.HostRuleDialog;
import aso.nfsapp.model.HostRule;

import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
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
        // Diálogo para seleccionar directorio: escribir o explorar
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Seleccionar directorio para compartir");
        fileChooser.setCurrentDirectory(new File("/home"));
        
        // Panel personalizado con campo de texto y botón Examinar
        JPanel selectPanel = new JPanel(new BorderLayout(5, 5));
        JTextField pathField = new JTextField("/", 30);
        JButton browseButton = new JButton("Examinar...");
        
        browseButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(ui);
            if (result == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        selectPanel.add(pathField, BorderLayout.CENTER);
        selectPanel.add(browseButton, BorderLayout.EAST);
        
        int result = JOptionPane.showOptionDialog(ui,
            selectPanel,
            "Directorio para exportar",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            new String[]{"Aceptar", "Cancelar"},
            "Aceptar");
        
        if (result != JOptionPane.OK_OPTION) {
            return; // Usuario canceló
        }
        
        String dir = pathField.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(ui, 
                "El directorio no puede estar vacío. Por favor ingrese una ruta válida.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            addDirectory(); // Reintentar
            return;
        }
        
        // Validar que la ruta sea válida
        if (!isValidPath(dir)) {
            JOptionPane.showMessageDialog(ui,
                "La ruta ingresada no es válida. Debe ser una ruta absoluta en Linux (ej: /opt/datos)",
                "Error", JOptionPane.ERROR_MESSAGE);
            addDirectory(); // Reintentar
            return;
        }
        
        // Asegurar que empiece con /
        if (!dir.startsWith("/")) {
            dir = "/" + dir;
        }
        
        // Crear directorio con permisos 777 si no existe
        if (!createDirectoryWithPermissions(dir)) {
            return; // Error al crear el directorio
        }
        
        ExportEntry entry = new ExportEntry(dir);
        
        directories.add(entry);
        ui.getDirectoryListPanel().getDirectoryListModel().addElement(dir);
        ui.getDirectoryListPanel().getDirectoryList().setSelectedIndex(directories.size() - 1);
        refreshRulesTable();
        
        // Marcar que hay cambios sin guardar
        ui.setUnsavedChanges(true);
        
        // Abrir automáticamente el diálogo de permisos para esta carpeta
        addRule();
    }

    private void editDirectory() {
        ExportEntry entry = currentEntry();
        if (entry == null) {
            return;
        }
        
        // Mostrar diálogo de selección
        Object[] options = {"Explorar", "Escribir ruta", "Cancelar"};
        int choice = JOptionPane.showOptionDialog(ui,
            "¿Cómo desea seleccionar el directorio?",
            "Editar Directorio",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        String dir = null;
        
        if (choice == 0) { // Explorar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Seleccionar directorio para compartir");
            fileChooser.setCurrentDirectory(new File(entry.getDirectoryPath()).getParentFile());
            
            int result = fileChooser.showOpenDialog(ui);
            if (result == JFileChooser.APPROVE_OPTION) {
                dir = fileChooser.getSelectedFile().getAbsolutePath();
            }
        } else if (choice == 1) { // Escribir ruta
            dir = JOptionPane.showInputDialog(ui, "Editar directorio:", entry.getDirectoryPath());
        } else {
            return; // Usuario canceló
        }
        
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
        
        // Crear directorio con permisos 777 si no existe
        if (!createDirectoryWithPermissions(dir)) {
            return; // Error al crear el directorio
        }
        
        entry.setDirectoryPath(dir);
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        ui.getDirectoryListPanel().getDirectoryListModel().set(index, dir);
        
        // Marcar que hay cambios sin guardar
        ui.setUnsavedChanges(true);
    }

    private void deleteDirectory() {
        int index = ui.getDirectoryListPanel().getDirectoryList().getSelectedIndex();
        if (index < 0) {
            return;
        }
        directories.remove(index);
        ui.getDirectoryListPanel().getDirectoryListModel().remove(index);
        clearRulesTable();
        
        // Marcar que hay cambios sin guardar
        ui.setUnsavedChanges(true);
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
            
            // Marcar que hay cambios sin guardar
            ui.setUnsavedChanges(true);
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
            
            // Marcar que hay cambios sin guardar
            ui.setUnsavedChanges(true);
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
            
            // Marcar que hay cambios sin guardar
            ui.setUnsavedChanges(true);
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
            String copyCommand = "sudo cp " + tempFilePath + " /etc/exports";
            int copyResult = executeWithRoot(copyCommand);
            if (copyResult != 0) {
                JOptionPane.showMessageDialog(ui, 
                    "Error al copiar el archivo a /etc/exports. Código: " + copyResult + 
                    "\nAsegúrese de tener permisos adecuados.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Comando para reiniciar el servidor NFS
            String restartCommand = "sudo systemctl restart nfs-server";
            int restartResult = executeWithRoot(restartCommand);
            if (restartResult != 0) {
                JOptionPane.showMessageDialog(ui, 
                    "Advertencia: No se pudo reiniciar nfs-server. Código: " + restartResult + 
                    "\nPuede que el servicio no esté instalado o tenga otro nombre (nfs-kernel-server).", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

            // Comando para aplicar las exportaciones
            String exportfsCommand = "sudo exportfs -rav";
            executeWithRoot(exportfsCommand);

            JOptionPane.showMessageDialog(ui, "Cambios aplicados con éxito\nArchivo copiado a /etc/exports");
            
            // Marcar que no hay cambios sin guardar
            ui.setUnsavedChanges(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(ui, "Error al aplicar cambios: " + e.getMessage() +
                "\n\nVerifique que tenga permisos adecuados.", 
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

    private boolean createDirectoryWithPermissions(String dirPath) {
        File dir = new File(dirPath);
        
        // Si el directorio ya existe, verificar que sea accesible
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                JOptionPane.showMessageDialog(ui,
                    "La ruta existe pero no es un directorio: " + dirPath,
                    "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // El directorio existe, preguntar si desea cambiar permisos a 777
            if (SystemPaths.isLinux()) {
                int confirm = JOptionPane.showConfirmDialog(ui,
                    "El directorio existe: " + dirPath + "\n\n" +
                    "¿Desea establecer permisos 777 (lectura/escritura para todos)?\n" +
                    "Esto asegura que otros usuarios puedan acceder sin problemas por NFS.",
                    "Establecer Permisos",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Establecer permisos 777 recursivamente
                        String chmodCommand = "sudo chmod -R 777 " + dirPath;
                        int result = executeWithRoot(chmodCommand);
                        
                        if (result != 0) {
                            JOptionPane.showMessageDialog(ui,
                                "No se pudieron establecer los permisos. Código: " + result + "\n" +
                                "Los usuarios remotos podrían tener problemas de acceso.",
                                "Advertencia", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ui,
                                "Permisos 777 establecidos correctamente en:\n" + dirPath + "\n\n" +
                                "Todos los usuarios podrán acceder por NFS.",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ui,
                            "Error al cambiar permisos: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }
            
            return true;
        }
        
        // El directorio no existe, intentar crearlo
        int confirm = JOptionPane.showConfirmDialog(ui,
            "El directorio no existe: " + dirPath + "\n¿Desea crearlo con permisos 777 (lectura/escritura para todos)?",
            "Crear Directorio",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }
        
        try {
            if (SystemPaths.isLinux()) {
                // Crear directorio con permisos 777 usando sudo
                String mkdirCommand = "sudo mkdir -p " + dirPath;
                int result = executeWithRoot(mkdirCommand);
                
                if (result != 0) {
                    JOptionPane.showMessageDialog(ui,
                        "Error al crear el directorio. Código: " + result,
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                
                // Establecer permisos 777 recursivamente
                String chmodCommand = "sudo chmod -R 777 " + dirPath;
                result = executeWithRoot(chmodCommand);
                
                if (result != 0) {
                    JOptionPane.showMessageDialog(ui,
                        "Directorio creado pero no se pudieron establecer permisos. Código: " + result,
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
                
                JOptionPane.showMessageDialog(ui,
                    "Directorio creado exitosamente con permisos 777\n" + dirPath,
                    "Exito", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                // En Windows, crear directorio normalmente
                if (dir.mkdirs()) {
                    JOptionPane.showMessageDialog(ui,
                        "Directorio creado exitosamente\n" + dirPath,
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(ui,
                        "Error al crear el directorio: " + dirPath,
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui,
                "Error al crear el directorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
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
                    String copyCommand = "sudo cp /etc/exports " + tempPath;
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

    private boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        // Debe empezar con /
        if (!path.startsWith("/")) {
            return false;
        }
        
        // No puede tener caracteres inválidos o ser solo slashes
        String trimmedSlashes = path.replaceAll("/+", "/");
        if (trimmedSlashes.equals("/")) {
            return false; // Solo slashes
        }
        
        // No puede tener caracteres especiales problemáticos
        if (path.contains("..") || path.contains("//")) {
            return false;
        }
        
        return true;
    }
}
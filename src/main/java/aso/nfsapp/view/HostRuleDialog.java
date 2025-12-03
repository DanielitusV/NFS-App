package aso.nfsapp.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para configurar reglas de host (permisos) en NFS.
 * Permite especificar el host destino y seleccionar opciones de NFS (rw, ro, sync, etc.).
 * Valida que siempre haya al menos rw o ro seleccionado.
 */
public class HostRuleDialog extends JDialog {
    private boolean confirmed = false;
    private String hostWildcard = "*";
    private String selectedOptions = "";

    private final JTextField hostField = new JTextField("*", 20);
    private final JTextField anonuidField = new JTextField("", 10);
    private final JTextField anongidField = new JTextField("", 10);
    private JCheckBox anonuidCheckbox;
    private JCheckBox anongidCheckbox;
    
    // Checkboxes para opciones (pueden estar vacíos/deseleccionados)
    private JCheckBox rwCheckbox, roCheckbox, syncCheckbox, asyncCheckbox;
    private JCheckBox rootSquashCheckbox, noRootSquashCheckbox;
    private JCheckBox subtreeCheckCheckbox, noSubtreeCheckCheckbox;
    private JCheckBox secureCheckbox, insecureCheckbox;
    
    // Checkboxes para opciones independientes
    private JCheckBox allSquashCheckbox;

    // Opciones NFS con sus descripciones
    // Nota: rw y ro son mutuamente excluyentes, pero permitimos ambos para que el usuario elija
    private static final String[][] NFS_OPTIONS = {
        {"rw", "Lectura y escritura (Read-Write) - Permite leer y escribir"},
        {"ro", "Solo lectura (Read-Only) - Solo permite leer"},
        {"sync", "Escrituras síncronas - Más seguro pero más lento"},
        {"async", "Escrituras asíncronas - Más rápido pero menos seguro"},
        {"no_root_squash", "Permite acceso root completo - [PELIGROSO]"},
        {"root_squash", "Mapea root a nobody - Recomendado (mas seguro)"},
        {"all_squash", "Mapea todos los usuarios a nobody - Maxima seguridad"},
        {"no_subtree_check", "Desactiva verificacion de subarbol - Mas rapido"},
        {"subtree_check", "Verifica subarbol completo - Mas seguro"},
        {"insecure", "Permite puertos > 1024 - [Menos seguro]"},
        {"secure", "Solo puertos < 1024 - Recomendado (más seguro)"},
        {"anonuid", "UID para usuarios anónimos (requiere valor numérico)"},
        {"anongid", "GID para usuarios anónimos (requiere valor numérico)"}
    };

    public HostRuleDialog(JFrame parent, String currentHost, String currentOptions) {
        super(parent, "Configurar Regla de Host", true);
        this.hostWildcard = currentHost != null && !currentHost.isEmpty() ? currentHost : "*";
        this.selectedOptions = currentOptions != null ? currentOptions : "rw";

        setLayout(new BorderLayout(10, 10));

        // Panel superior: Host Wildcard
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostPanel.add(new JLabel("Host Wild Card:"));
        hostField.setText(hostWildcard);
        hostPanel.add(hostField);
        hostPanel.add(new JLabel("  (ej: *, 192.168.1.0, 192.168.1.10)"));

        // Panel central: Opciones con radio buttons para opciones excluyentes y checkboxes para independientes
        JPanel optionsContainer = new JPanel(new BorderLayout());
        optionsContainer.setBorder(BorderFactory.createTitledBorder("Opciones NFS"));
        
        // Panel principal con scroll
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // --- GRUPO 1: Read/Write vs Read-Only (Opcionales) ---
        JPanel rwPanel = new JPanel();
        rwPanel.setLayout(new BoxLayout(rwPanel, BoxLayout.X_AXIS));
        rwPanel.setBorder(BorderFactory.createTitledBorder("Permisos de acceso (opcional)"));
        rwCheckbox = new JCheckBox("rw (Lectura y escritura)");
        roCheckbox = new JCheckBox("ro (Solo lectura)");
        rwCheckbox.setToolTipText("Lectura y escritura (Read-Write) - Permite leer y escribir");
        roCheckbox.setToolTipText("Solo lectura (Read-Only) - Solo permite leer");
        rwCheckbox.addActionListener(e -> {
            if (rwCheckbox.isSelected()) roCheckbox.setSelected(false);
        });
        roCheckbox.addActionListener(e -> {
            if (roCheckbox.isSelected()) rwCheckbox.setSelected(false);
        });
        rwPanel.add(rwCheckbox);
        rwPanel.add(Box.createHorizontalStrut(20));
        rwPanel.add(roCheckbox);
        rwPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(rwPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 2: Sync vs Async (Opcionales) ---
        JPanel syncPanel = new JPanel();
        syncPanel.setLayout(new BoxLayout(syncPanel, BoxLayout.X_AXIS));
        syncPanel.setBorder(BorderFactory.createTitledBorder("Modo de escritura (opcional)"));
        syncCheckbox = new JCheckBox("sync (Síncrono)");
        asyncCheckbox = new JCheckBox("async (Asíncrono)");
        syncCheckbox.setToolTipText("Escrituras síncronas - Más seguro pero más lento");
        asyncCheckbox.setToolTipText("Escrituras asíncronas - Más rápido pero menos seguro");
        syncCheckbox.addActionListener(e -> {
            if (syncCheckbox.isSelected()) asyncCheckbox.setSelected(false);
        });
        asyncCheckbox.addActionListener(e -> {
            if (asyncCheckbox.isSelected()) syncCheckbox.setSelected(false);
        });
        syncPanel.add(syncCheckbox);
        syncPanel.add(Box.createHorizontalStrut(20));
        syncPanel.add(asyncCheckbox);
        syncPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(syncPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 3: Root Squash (Deshabilitado si all_squash está activo) ---
        JPanel squashPanel = new JPanel();
        squashPanel.setLayout(new BoxLayout(squashPanel, BoxLayout.X_AXIS));
        squashPanel.setBorder(BorderFactory.createTitledBorder("Mapeo de usuario root (opcional)"));
        rootSquashCheckbox = new JCheckBox("root_squash (Recomendado)");
        noRootSquashCheckbox = new JCheckBox("no_root_squash (Peligroso)");
        rootSquashCheckbox.setToolTipText("Mapea root a nobody - Recomendado (más seguro)");
        noRootSquashCheckbox.setToolTipText("Permite acceso root completo - [PELIGROSO]");
        rootSquashCheckbox.addActionListener(e -> {
            if (rootSquashCheckbox.isSelected()) noRootSquashCheckbox.setSelected(false);
        });
        noRootSquashCheckbox.addActionListener(e -> {
            if (noRootSquashCheckbox.isSelected()) rootSquashCheckbox.setSelected(false);
        });
        squashPanel.add(rootSquashCheckbox);
        squashPanel.add(Box.createHorizontalStrut(10));
        squashPanel.add(noRootSquashCheckbox);
        squashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(squashPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 4: Subtree Check (Opcionales) ---
        JPanel subtreePanel = new JPanel();
        subtreePanel.setLayout(new BoxLayout(subtreePanel, BoxLayout.X_AXIS));
        subtreePanel.setBorder(BorderFactory.createTitledBorder("Verificación de subárbol (opcional)"));
        subtreeCheckCheckbox = new JCheckBox("subtree_check (Más seguro)");
        noSubtreeCheckCheckbox = new JCheckBox("no_subtree_check (Más rápido)");
        subtreeCheckCheckbox.setToolTipText("Verifica subárbol completo - Más seguro");
        noSubtreeCheckCheckbox.setToolTipText("Desactiva verificación de subárbol - Más rápido");
        subtreeCheckCheckbox.addActionListener(e -> {
            if (subtreeCheckCheckbox.isSelected()) noSubtreeCheckCheckbox.setSelected(false);
        });
        noSubtreeCheckCheckbox.addActionListener(e -> {
            if (noSubtreeCheckCheckbox.isSelected()) subtreeCheckCheckbox.setSelected(false);
        });
        subtreePanel.add(subtreeCheckCheckbox);
        subtreePanel.add(Box.createHorizontalStrut(10));
        subtreePanel.add(noSubtreeCheckCheckbox);
        subtreePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(subtreePanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 5: Secure vs Insecure (Opcionales) ---
        JPanel securePanel = new JPanel();
        securePanel.setLayout(new BoxLayout(securePanel, BoxLayout.X_AXIS));
        securePanel.setBorder(BorderFactory.createTitledBorder("Puerto de conexión (opcional)"));
        secureCheckbox = new JCheckBox("secure (Recomendado)");
        insecureCheckbox = new JCheckBox("insecure (Menos seguro)");
        secureCheckbox.setToolTipText("Solo puertos < 1024 - Recomendado (más seguro)");
        insecureCheckbox.setToolTipText("Permite puertos > 1024 - [Menos seguro]");
        secureCheckbox.addActionListener(e -> {
            if (secureCheckbox.isSelected()) insecureCheckbox.setSelected(false);
        });
        insecureCheckbox.addActionListener(e -> {
            if (insecureCheckbox.isSelected()) secureCheckbox.setSelected(false);
        });
        securePanel.add(secureCheckbox);
        securePanel.add(Box.createHorizontalStrut(20));
        securePanel.add(insecureCheckbox);
        securePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(securePanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- OPCIONES INDEPENDIENTES (Checkboxes) ---
        JPanel independentPanel = new JPanel();
        independentPanel.setLayout(new BoxLayout(independentPanel, BoxLayout.Y_AXIS));
        independentPanel.setBorder(BorderFactory.createTitledBorder("Opciones adicionales"));
        
        // all_squash checkbox (excluyente con root_squash/no_root_squash)
        allSquashCheckbox = new JCheckBox("all_squash - Mapea TODOS los usuarios a anónimo (Máxima seguridad)");
        allSquashCheckbox.setToolTipText("Mapea todos los usuarios (incluyendo root) a nobody - Desactiva root_squash/no_root_squash");
        allSquashCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        allSquashCheckbox.addActionListener(e -> {
            // Si all_squash está seleccionado, deshabilita root_squash/no_root_squash
            boolean isAllSquashSelected = allSquashCheckbox.isSelected();
            rootSquashCheckbox.setEnabled(!isAllSquashSelected);
            noRootSquashCheckbox.setEnabled(!isAllSquashSelected);
            // Si se habilita all_squash, deselecciona root_squash/no_root_squash
            if (isAllSquashSelected) {
                rootSquashCheckbox.setSelected(false);
                noRootSquashCheckbox.setSelected(false);
            }
        });
        independentPanel.add(allSquashCheckbox);
        independentPanel.add(Box.createVerticalStrut(10));
        
        // anonuid - puede estar vacío
        JPanel anonuidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        anonuidCheckbox = new JCheckBox("anonuid");
        anonuidCheckbox.setToolTipText("UID para usuarios anónimos (dejar vacío usa 65534 por defecto)");
        anonuidField.setEnabled(false);
        anonuidField.setColumns(8);
        anonuidCheckbox.addActionListener(e -> anonuidField.setEnabled(anonuidCheckbox.isSelected()));
        anonuidPanel.add(anonuidCheckbox);
        anonuidPanel.add(new JLabel("UID:"));
        anonuidPanel.add(anonuidField);
        anonuidPanel.add(new JLabel("(opcional, 0-65535)"));
        independentPanel.add(anonuidPanel);
        independentPanel.add(Box.createVerticalStrut(5));
        
        // anongid - puede estar vacío
        JPanel anongidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        anongidCheckbox = new JCheckBox("anongid");
        anongidCheckbox.setToolTipText("GID para usuarios anónimos (dejar vacío usa 65534 por defecto)");
        anongidField.setEnabled(false);
        anongidField.setColumns(8);
        anongidCheckbox.addActionListener(e -> anongidField.setEnabled(anongidCheckbox.isSelected()));
        anongidPanel.add(anongidCheckbox);
        anongidPanel.add(new JLabel("GID:"));
        anongidPanel.add(anongidField);
        anongidPanel.add(new JLabel("(opcional, 0-65535)"));
        independentPanel.add(anongidPanel);
        
        independentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(independentPanel);
        
        // Botón de información
        JButton infoButton = new JButton("Información de Opciones");
        infoButton.addActionListener(e -> showAllOptionsInfo());
        JPanel infoButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoButtonPanel.add(infoButton);
        
        optionsContainer.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        optionsContainer.add(infoButtonPanel, BorderLayout.SOUTH);

        // Restaurar opciones actuales
        if (currentOptions != null && !currentOptions.isEmpty()) {
            String[] opts = currentOptions.split(",");
            for (String opt : opts) {
                opt = opt.trim();
                if (opt.equals("rw")) rwCheckbox.setSelected(true);
                else if (opt.equals("ro")) roCheckbox.setSelected(true);
                else if (opt.equals("sync")) syncCheckbox.setSelected(true);
                else if (opt.equals("async")) asyncCheckbox.setSelected(true);
                else if (opt.equals("root_squash")) rootSquashCheckbox.setSelected(true);
                else if (opt.equals("no_root_squash")) noRootSquashCheckbox.setSelected(true);
                else if (opt.equals("subtree_check")) subtreeCheckCheckbox.setSelected(true);
                else if (opt.equals("no_subtree_check")) noSubtreeCheckCheckbox.setSelected(true);
                else if (opt.equals("secure")) secureCheckbox.setSelected(true);
                else if (opt.equals("insecure")) insecureCheckbox.setSelected(true);
                else if (opt.equals("all_squash")) {
                    allSquashCheckbox.setSelected(true);
                    // Deshabilitar root_squash/no_root_squash cuando all_squash está activo
                    rootSquashCheckbox.setEnabled(false);
                    noRootSquashCheckbox.setEnabled(false);
                }
                else if (opt.startsWith("anonuid=")) {
                    String[] parts = opt.split("=");
                    if (parts.length == 2) {
                        anonuidCheckbox.setSelected(true);
                        anonuidField.setText(parts[1]);
                        anonuidField.setEnabled(true);
                    }
                } else if (opt.startsWith("anongid=")) {
                    String[] parts = opt.split("=");
                    if (parts.length == 2) {
                        anongidCheckbox.setSelected(true);
                        anongidField.setText(parts[1]);
                        anongidField.setEnabled(true);
                    }
                }
            }
        }

        // Panel inferior: Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancelar");

        okButton.addActionListener(e -> {
            hostWildcard = hostField.getText().trim();
            if (hostWildcard.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El host wildcard no puede estar vacío. Por favor ingrese un valor.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                hostField.requestFocus();
                return;
            }
            
            // Validar IP/Host/Wildcard
            if (!isValidHostOrIP(hostWildcard)) {
                JOptionPane.showMessageDialog(this,
                    "Formato inválido. Aceptados: * (comodín), IPv4 (ej: 192.168.1.1), CIDR (ej: 192.168.1.0), rango (*)",
                    "Error", JOptionPane.ERROR_MESSAGE);
                hostField.requestFocus();
                return;
            }
            
            // Validar anonuid y anongid si están seleccionados
            if (anonuidCheckbox.isSelected() && anonuidField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "anonuid requiere un valor numerico (UID)",
                    "Error", JOptionPane.ERROR_MESSAGE);
                anonuidField.requestFocus();
                return;
            }
            if (anongidCheckbox.isSelected() && anongidField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "anongid requiere un valor numerico (GID)",
                    "Error", JOptionPane.ERROR_MESSAGE);
                anongidField.requestFocus();
                return;
            }
            
            buildOptionsString();
            if (confirmed) { // Solo cerrar si buildOptionsString no encontró errores
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(hostPanel, BorderLayout.NORTH);
        add(optionsContainer, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(700, 600);
        setLocationRelativeTo(parent);
    }

    private void buildOptionsString() {
        List<String> selected = new ArrayList<>();
        
        // Agregar solo las opciones seleccionadas (pueden estar vacías)
        if (rwCheckbox.isSelected()) selected.add("rw");
        if (roCheckbox.isSelected()) selected.add("ro");
        
        if (syncCheckbox.isSelected()) selected.add("sync");
        if (asyncCheckbox.isSelected()) selected.add("async");
        
        // Root squash: solo si all_squash NO está seleccionado
        if (allSquashCheckbox.isSelected()) {
            selected.add("all_squash");
        } else {
            if (rootSquashCheckbox.isSelected()) selected.add("root_squash");
            if (noRootSquashCheckbox.isSelected()) selected.add("no_root_squash");
        }
        
        if (subtreeCheckCheckbox.isSelected()) selected.add("subtree_check");
        if (noSubtreeCheckCheckbox.isSelected()) selected.add("no_subtree_check");
        
        if (secureCheckbox.isSelected()) selected.add("secure");
        if (insecureCheckbox.isSelected()) selected.add("insecure");
        
        // anonuid - puede estar vacío (no valida si está vacío)
        if (anonuidCheckbox.isSelected()) {
            String uid = anonuidField.getText().trim();
            if (!uid.isEmpty()) {
                if (uid.matches("\\d+")) {
                    int uidNum = Integer.parseInt(uid);
                    if (uidNum >= 0 && uidNum <= 65535) {
                        selected.add("anonuid=" + uid);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "anonuid debe estar entre 0 y 65535",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        confirmed = false;
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "anonuid debe ser un numero valido",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    confirmed = false;
                    return;
                }
            }
            // Si está vacío, no agrega nada (NFS usa 65534 por defecto)
        }
        
        // anongid - puede estar vacío (no valida si está vacío)
        if (anongidCheckbox.isSelected()) {
            String gid = anongidField.getText().trim();
            if (!gid.isEmpty()) {
                if (gid.matches("\\d+")) {
                    int gidNum = Integer.parseInt(gid);
                    if (gidNum >= 0 && gidNum <= 65535) {
                        selected.add("anongid=" + gid);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "anongid debe estar entre 0 y 65535",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        confirmed = false;
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "anongid debe ser un numero valido",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    confirmed = false;
                    return;
                }
            }
            // Si está vacío, no agrega nada (NFS usa 65534 por defecto)
        }
        
        // VALIDACIÓN: Debe haber AL MENOS UNA opción seleccionada
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar al menos una opción NFS",
                "Error", JOptionPane.ERROR_MESSAGE);
            confirmed = false;
            return;
        }
        
        selectedOptions = String.join(",", selected);
        confirmed = true; // Solo marcar como confirmado si todo está bien
    }

    private boolean isValidHostOrIP(String hostOrIP) {
        // Wildcard "*" is valid
        if ("*".equals(hostOrIP)) {
            return true;
        }
        
        // Check CIDR notation (e.g., 192.168.1.0/24)
        if (hostOrIP.contains("/")) {
            String[] parts = hostOrIP.split("/");
            if (parts.length == 2) {
                String ipPart = parts[0];
                String maskPart = parts[1];
                if (isValidIPv4(ipPart)) {
                    try {
                        int mask = Integer.parseInt(maskPart);
                        return mask >= 0 && mask <= 32;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            return false;
        }
        
        // Check if it's a valid IPv4 address
        if (isValidIPv4(hostOrIP)) {
            return true;
        }
        
        // Check if it's a valid hostname (RFC 1123)
        // Only letters, numbers, hyphens (-) and dots (.)
        // Cannot start/end with hyphen or dot
        if (hostOrIP.matches("^[a-zA-Z0-9]([a-zA-Z0-9.-]*[a-zA-Z0-9])?$")) {
            if (!hostOrIP.contains("--") && !hostOrIP.contains("..") && 
                !hostOrIP.startsWith(".") && !hostOrIP.startsWith("-") &&
                !hostOrIP.endsWith(".") && !hostOrIP.endsWith("-")) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isValidIPv4(String ip) {
        // Validar formato IPv4: xxx.xxx.xxx.xxx donde cada xxx está entre 0-255
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }

    private void showAllOptionsInfo() {
        StringBuilder info = new StringBuilder("<html><body style='width: 400px;'>");
        info.append("<h3>Descripcion de Opciones NFS</h3>");
        info.append("<table cellpadding='5' cellspacing='5'>");
        
        for (String[] option : NFS_OPTIONS) {
            info.append("<tr>");
            info.append("<td><b>").append(option[0]).append("</b></td>");
            info.append("<td>").append(option[1]).append("</td>");
            info.append("</tr>");
        }
        
        info.append("</table>");
        info.append("</body></html>");
        
        JOptionPane.showMessageDialog(
            this,
            info.toString(),
            "Informacion de Opciones NFS",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getHostWildcard() {
        return hostWildcard;
    }

    public String getOptions() {
        return selectedOptions;
    }

    public static HostRuleDialogResult showDialog(JFrame parent, String currentHost, String currentOptions) {
        HostRuleDialog dialog = new HostRuleDialog(parent, currentHost, currentOptions);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return new HostRuleDialogResult(dialog.getHostWildcard(), dialog.getOptions());
        }
        return null;
    }

    public static class HostRuleDialogResult {
        public final String host;
        public final String options;

        public HostRuleDialogResult(String host, String options) {
            this.host = host;
            this.options = options;
        }
    }
}


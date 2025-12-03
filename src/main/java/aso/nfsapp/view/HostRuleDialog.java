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
    
    // Radio buttons para opciones mutuamente excluyentes
    private ButtonGroup rwGroup, syncGroup, squashGroup, subtreeGroup, secureGroup;
    private JRadioButton rwButton, roButton, syncButton, asyncButton;
    private JRadioButton rootSquashButton, noRootSquashButton;
    private JRadioButton subtreeCheckButton, noSubtreeCheckButton;
    private JRadioButton secureButton, insecureButton;
    
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
        setLocationRelativeTo(parent);

        // Panel superior: Host Wildcard
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostPanel.add(new JLabel("Host Wild Card:"));
        hostField.setText(hostWildcard);
        hostPanel.add(hostField);
        hostPanel.add(new JLabel("  (ej: *, 192.168.1.0/24, 192.168.1.10)"));

        // Panel central: Opciones con radio buttons para opciones excluyentes y checkboxes para independientes
        JPanel optionsContainer = new JPanel(new BorderLayout());
        optionsContainer.setBorder(BorderFactory.createTitledBorder("Opciones NFS"));
        
        // Panel principal con scroll
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // --- GRUPO 1: Read/Write vs Read-Only ---
        JPanel rwPanel = new JPanel();
        rwPanel.setLayout(new BoxLayout(rwPanel, BoxLayout.X_AXIS));
        rwPanel.setBorder(BorderFactory.createTitledBorder("Permisos de acceso"));
        rwGroup = new ButtonGroup();
        rwButton = new JRadioButton("rw (Lectura y escritura)");
        roButton = new JRadioButton("ro (Solo lectura)");
        rwButton.setToolTipText("Lectura y escritura (Read-Write) - Permite leer y escribir");
        roButton.setToolTipText("Solo lectura (Read-Only) - Solo permite leer");
        rwGroup.add(rwButton);
        rwGroup.add(roButton);
        rwButton.setSelected(true);
        rwPanel.add(rwButton);
        rwPanel.add(Box.createHorizontalStrut(20));
        rwPanel.add(roButton);
        rwPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(rwPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 2: Sync vs Async ---
        JPanel syncPanel = new JPanel();
        syncPanel.setLayout(new BoxLayout(syncPanel, BoxLayout.X_AXIS));
        syncPanel.setBorder(BorderFactory.createTitledBorder("Modo de escritura"));
        syncGroup = new ButtonGroup();
        syncButton = new JRadioButton("sync (Síncrono)");
        asyncButton = new JRadioButton("async (Asíncrono)");
        syncButton.setToolTipText("Escrituras síncronas - Más seguro pero más lento");
        asyncButton.setToolTipText("Escrituras asíncronas - Más rápido pero menos seguro");
        syncGroup.add(syncButton);
        syncGroup.add(asyncButton);
        syncButton.setSelected(true);
        syncPanel.add(syncButton);
        syncPanel.add(Box.createHorizontalStrut(20));
        syncPanel.add(asyncButton);
        syncPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(syncPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 3: Root Squash ---
        JPanel squashPanel = new JPanel();
        squashPanel.setLayout(new BoxLayout(squashPanel, BoxLayout.X_AXIS));
        squashPanel.setBorder(BorderFactory.createTitledBorder("Mapeo de usuario root"));
        squashGroup = new ButtonGroup();
        rootSquashButton = new JRadioButton("root_squash (Recomendado)");
        noRootSquashButton = new JRadioButton("no_root_squash (Peligroso)");
        rootSquashButton.setToolTipText("Mapea root a nobody - Recomendado (más seguro)");
        noRootSquashButton.setToolTipText("Permite acceso root completo - [PELIGROSO]");
        squashGroup.add(rootSquashButton);
        squashGroup.add(noRootSquashButton);
        rootSquashButton.setSelected(true);
        squashPanel.add(rootSquashButton);
        squashPanel.add(Box.createHorizontalStrut(10));
        squashPanel.add(noRootSquashButton);
        squashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(squashPanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 4: Subtree Check ---
        JPanel subtreePanel = new JPanel();
        subtreePanel.setLayout(new BoxLayout(subtreePanel, BoxLayout.X_AXIS));
        subtreePanel.setBorder(BorderFactory.createTitledBorder("Verificación de subárbol"));
        subtreeGroup = new ButtonGroup();
        subtreeCheckButton = new JRadioButton("subtree_check (Más seguro)");
        noSubtreeCheckButton = new JRadioButton("no_subtree_check (Más rápido)");
        subtreeCheckButton.setToolTipText("Verifica subárbol completo - Más seguro");
        noSubtreeCheckButton.setToolTipText("Desactiva verificación de subárbol - Más rápido");
        subtreeGroup.add(subtreeCheckButton);
        subtreeGroup.add(noSubtreeCheckButton);
        subtreeCheckButton.setSelected(true);
        subtreePanel.add(subtreeCheckButton);
        subtreePanel.add(Box.createHorizontalStrut(10));
        subtreePanel.add(noSubtreeCheckButton);
        subtreePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(subtreePanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- GRUPO 5: Secure vs Insecure ---
        JPanel securePanel = new JPanel();
        securePanel.setLayout(new BoxLayout(securePanel, BoxLayout.X_AXIS));
        securePanel.setBorder(BorderFactory.createTitledBorder("Puerto de conexión"));
        secureGroup = new ButtonGroup();
        secureButton = new JRadioButton("secure (Recomendado)");
        insecureButton = new JRadioButton("insecure (Menos seguro)");
        secureButton.setToolTipText("Solo puertos < 1024 - Recomendado (más seguro)");
        insecureButton.setToolTipText("Permite puertos > 1024 - [Menos seguro]");
        secureGroup.add(secureButton);
        secureGroup.add(insecureButton);
        secureButton.setSelected(true);
        securePanel.add(secureButton);
        securePanel.add(Box.createHorizontalStrut(20));
        securePanel.add(insecureButton);
        securePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.add(securePanel);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        // --- OPCIONES INDEPENDIENTES (Checkboxes) ---
        JPanel independentPanel = new JPanel();
        independentPanel.setLayout(new BoxLayout(independentPanel, BoxLayout.Y_AXIS));
        independentPanel.setBorder(BorderFactory.createTitledBorder("Opciones adicionales"));
        
        allSquashCheckbox = new JCheckBox("all_squash (Máxima seguridad)");
        allSquashCheckbox.setToolTipText("Mapea todos los usuarios a nobody - Máxima seguridad");
        allSquashCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        independentPanel.add(allSquashCheckbox);
        independentPanel.add(Box.createVerticalStrut(5));
        
        // anonuid
        JPanel anonuidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        anonuidCheckbox = new JCheckBox("anonuid (UID para anónimos)");
        anonuidCheckbox.setToolTipText("UID para usuarios anónimos (requiere valor numérico)");
        anonuidField.setEnabled(false);
        anonuidField.setColumns(8);
        anonuidCheckbox.addActionListener(e -> anonuidField.setEnabled(anonuidCheckbox.isSelected()));
        anonuidPanel.add(anonuidCheckbox);
        anonuidPanel.add(new JLabel("UID:"));
        anonuidPanel.add(anonuidField);
        independentPanel.add(anonuidPanel);
        independentPanel.add(Box.createVerticalStrut(5));
        
        // anongid
        JPanel anongidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        anongidCheckbox = new JCheckBox("anongid (GID para anónimos)");
        anongidCheckbox.setToolTipText("GID para usuarios anónimos (requiere valor numérico)");
        anongidField.setEnabled(false);
        anongidField.setColumns(8);
        anongidCheckbox.addActionListener(e -> anongidField.setEnabled(anongidCheckbox.isSelected()));
        anongidPanel.add(anongidCheckbox);
        anongidPanel.add(new JLabel("GID:"));
        anongidPanel.add(anongidField);
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
                if (opt.equals("rw")) rwButton.setSelected(true);
                else if (opt.equals("ro")) roButton.setSelected(true);
                else if (opt.equals("sync")) syncButton.setSelected(true);
                else if (opt.equals("async")) asyncButton.setSelected(true);
                else if (opt.equals("root_squash")) rootSquashButton.setSelected(true);
                else if (opt.equals("no_root_squash")) noRootSquashButton.setSelected(true);
                else if (opt.equals("subtree_check")) subtreeCheckButton.setSelected(true);
                else if (opt.equals("no_subtree_check")) noSubtreeCheckButton.setSelected(true);
                else if (opt.equals("secure")) secureButton.setSelected(true);
                else if (opt.equals("insecure")) insecureButton.setSelected(true);
                else if (opt.equals("all_squash")) allSquashCheckbox.setSelected(true);
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
                    "Formato inválido. Aceptados: * (comodín), IPv4 (ej: 192.168.1.1), CIDR (ej: 192.168.1.0/24), rango (*)",
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
    }

    private void buildOptionsString() {
        List<String> selected = new ArrayList<>();
        
        // Agregar las opciones de los radio buttons (siempre habrá una seleccionada en cada grupo)
        if (rwButton.isSelected()) selected.add("rw");
        else selected.add("ro");
        
        if (syncButton.isSelected()) selected.add("sync");
        else selected.add("async");
        
        if (rootSquashButton.isSelected()) selected.add("root_squash");
        else selected.add("no_root_squash");
        
        if (subtreeCheckButton.isSelected()) selected.add("subtree_check");
        else selected.add("no_subtree_check");
        
        if (secureButton.isSelected()) selected.add("secure");
        else selected.add("insecure");
        
        // Agregar opciones independientes si están seleccionadas
        if (allSquashCheckbox.isSelected()) {
            selected.add("all_squash");
        }
        
        // anonuid
        if (anonuidCheckbox.isSelected()) {
            String uid = anonuidField.getText().trim();
            if (!uid.isEmpty() && uid.matches("\\d+")) {
                selected.add("anonuid=" + uid);
            } else if (!uid.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "anonuid debe ser un numero valido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                confirmed = false;
                return;
            }
        }
        
        // anongid
        if (anongidCheckbox.isSelected()) {
            String gid = anongidField.getText().trim();
            if (!gid.isEmpty() && gid.matches("\\d+")) {
                selected.add("anongid=" + gid);
            } else if (!gid.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "anongid debe ser un numero valido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                confirmed = false;
                return;
            }
        }
        
        selectedOptions = String.join(",", selected);
        confirmed = true; // Solo marcar como confirmado si todo está bien
    }

    private boolean isValidHostOrIP(String hostOrIP) {
        // Comodín "*" es válido
        if ("*".equals(hostOrIP)) {
            return true;
        }
        
        // Verificar si es CIDR (ej: 192.168.1.0/24)
        if (hostOrIP.contains("/")) {
            String[] parts = hostOrIP.split("/");
            if (parts.length == 2) {
                String ipPart = parts[0];
                String maskPart = parts[1];
                // Validar que sea una IP válida
                if (isValidIPv4(ipPart)) {
                    // Validar que la máscara sea numérica y esté entre 0-32
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
        
        // Verificar si es una IPv4 válida
        if (isValidIPv4(hostOrIP)) {
            return true;
        }
        
        // Verificar si es un nombre de host válido (letras, números, guiones, puntos)
        // Ejemplo: server.local, nfs-server, servidor01
        if (hostOrIP.matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*[a-zA-Z0-9]$|^[a-zA-Z0-9]$")) {
            return true;
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


package aso.nfsapp.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HostRuleDialog extends JDialog {
    private boolean confirmed = false;
    private String hostWildcard = "*";
    private String selectedOptions = "";

    private final JTextField hostField = new JTextField("192.168.1.0/24", 20);
    private final List<JCheckBox> optionCheckboxes = new ArrayList<>();
    private final JTextField anonuidField = new JTextField("", 10);
    private final JTextField anongidField = new JTextField("", 10);
    private JCheckBox anonuidCheckbox;
    private JCheckBox anongidCheckbox;

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
        this.hostWildcard = currentHost != null && !currentHost.isEmpty() ? currentHost : "192.168.1.0/24";
        this.selectedOptions = currentOptions != null ? currentOptions : "rw";

        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parent);

        // Panel superior: Host Wildcard
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hostPanel.add(new JLabel("Host Wild Card:"));
        hostField.setText(hostWildcard);
        hostPanel.add(hostField);
        hostPanel.add(new JLabel("  (ej: *, 192.168.1.0/24, 192.168.1.10)"));

        // Panel central: Opciones con checkboxes
        JPanel optionsContainer = new JPanel(new BorderLayout());
        optionsContainer.setBorder(BorderFactory.createTitledBorder("Opciones NFS"));
        
        // Panel para checkboxes
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // Crear checkboxes para cada opción (sin botones individuales)
        for (int i = 0; i < NFS_OPTIONS.length; i++) {
            String[] option = NFS_OPTIONS[i];
            
            // Opciones especiales que requieren valores
            if (option[0].equals("anonuid") || option[0].equals("anongid")) {
                JPanel specialOptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                JCheckBox checkbox = new JCheckBox(option[0]);
                checkbox.setToolTipText(option[1]);
                optionCheckboxes.add(checkbox);
                
                if (option[0].equals("anonuid")) {
                    anonuidCheckbox = checkbox;
                    specialOptionPanel.add(checkbox);
                    specialOptionPanel.add(new JLabel("UID:"));
                    specialOptionPanel.add(anonuidField);
                    anonuidField.setEnabled(false);
                    checkbox.addActionListener(e -> anonuidField.setEnabled(checkbox.isSelected()));
                } else {
                    anongidCheckbox = checkbox;
                    specialOptionPanel.add(checkbox);
                    specialOptionPanel.add(new JLabel("GID:"));
                    specialOptionPanel.add(anongidField);
                    anongidField.setEnabled(false);
                    checkbox.addActionListener(e -> anongidField.setEnabled(checkbox.isSelected()));
                }
                
                specialOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionsPanel.add(specialOptionPanel);
            } else {
                // Checkbox solo con el nombre de la opción
                JCheckBox checkbox = new JCheckBox(option[0]);
                checkbox.setToolTipText(option[1]); // Tooltip al pasar el mouse
                optionCheckboxes.add(checkbox);
                checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionsPanel.add(checkbox);
            }
            optionsPanel.add(Box.createVerticalStrut(2)); // Espacio entre opciones
        }
        
        // Botón de información único que muestra todas las descripciones
        JButton infoButton = new JButton("Informacion de Opciones");
        infoButton.addActionListener(e -> showAllOptionsInfo());
        
        // Panel para el botón de información
        JPanel infoButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoButtonPanel.add(infoButton);
        
        optionsContainer.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        optionsContainer.add(infoButtonPanel, BorderLayout.SOUTH);

        // Marcar las opciones actuales
        if (currentOptions != null && !currentOptions.isEmpty()) {
            String[] opts = currentOptions.split(",");
            for (String opt : opts) {
                opt = opt.trim();
                // Verificar si tiene valor (anonuid=1000, anongid=1000)
                if (opt.startsWith("anonuid=")) {
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
                } else {
                    for (int i = 0; i < NFS_OPTIONS.length; i++) {
                        if (NFS_OPTIONS[i][0].equals(opt)) {
                            optionCheckboxes.get(i).setSelected(true);
                            break;
                        }
                    }
                }
            }
        } else {
            // Por defecto marcar rw y root_squash
            optionCheckboxes.get(0).setSelected(true); // rw
            optionCheckboxes.get(5).setSelected(true); // root_squash
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
        
        setSize(500, 450);
    }

    private void buildOptionsString() {
        List<String> selected = new ArrayList<>();
        boolean hasRw = false;
        boolean hasRo = false;
        
        for (int i = 0; i < optionCheckboxes.size(); i++) {
            if (optionCheckboxes.get(i).isSelected()) {
                String opt = NFS_OPTIONS[i][0];
                
                // Opciones especiales con valores
                if (opt.equals("anonuid") && anonuidCheckbox.isSelected()) {
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
                } else if (opt.equals("anongid") && anongidCheckbox.isSelected()) {
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
                } else {
                    selected.add(opt);
                    if (opt.equals("rw")) hasRw = true;
                    if (opt.equals("ro")) hasRo = true;
                }
            }
        }
        
        // Validación: si no hay rw ni ro, agregar ro por defecto
        if (!hasRw && !hasRo) {
            selected.add("ro");
        }
        
        selectedOptions = String.join(",", selected);
        confirmed = true; // Solo marcar como confirmado si todo está bien
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


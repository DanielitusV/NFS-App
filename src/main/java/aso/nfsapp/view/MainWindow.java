package aso.nfsapp.view;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;

/**
 * Ventana principal de NFS App.
 * Contiene la interfaz gráfica con los paneles de directorios y reglas de host.
 * Mantiene estado de cambios sin guardar para controlar el cierre de la aplicación.
 */
public class MainWindow extends JFrame {
    private final DirectoryListPanel directoryListPanel = new DirectoryListPanel();
    private final HostRulesTablePanel hostRulesPanel = new HostRulesTablePanel();

    private final JButton addDirectoryButton = new JButton("Agregar Directorio");
    private final JButton editDirectoryButton = new JButton("Editar");
    private final JButton deleteDirectoryButton = new JButton("Eliminar");
    private final JButton saveApplyButton = new JButton("Guardar y Aplicar");
    
    private boolean hasUnsavedChanges = false;

    public MainWindow() {
        super ("NFS App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        /* Barra superior de configuración */
        JToolBar topBar = new JToolBar();
        topBar.setFloatable(false);
        topBar.add(addDirectoryButton);
        topBar.add(editDirectoryButton);
        topBar.add(deleteDirectoryButton);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(saveApplyButton);

        /* Split vertical */
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                directoryListPanel, hostRulesPanel);
        split.setResizeWeight(0.5);
        split.setDividerLocation(300);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    public DirectoryListPanel getDirectoryListPanel() {
        return this.directoryListPanel;
    }
    public HostRulesTablePanel getHostRulesPanel() {
        return hostRulesPanel;
    }
    
    public void updateHostRulesTitle(String directoryPath) {
        hostRulesPanel.updateTitle(directoryPath);
    }
    public JButton getAddDirectoryButton() {
        return addDirectoryButton;
    }
    public JButton getEditDirectoryButton() {
        return editDirectoryButton;
    }
    public JButton getDeleteDirectoryButton() {
        return deleteDirectoryButton;
    }
    public JButton getSaveApplyButton() {
        return saveApplyButton;
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public void setUnsavedChanges(boolean unsaved) {
        this.hasUnsavedChanges = unsaved;
    }

}

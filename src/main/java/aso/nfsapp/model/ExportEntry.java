package aso.nfsapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una entrada de directorio en /etc/exports.
 * Almacena la ruta del directorio, las reglas de host asociadas y comentarios opcionales.
 * Se serializa como: /ruta host1(opciones1) host2(opciones2)
 */
public class ExportEntry {
    private String directoryPath;
    private List<HostRule> hostRules;
    private String comment; // Comentario asociado (opcional)

    public ExportEntry(String directoryPath) {
        this.directoryPath = directoryPath;
        this.hostRules = new ArrayList<>();
        this.comment = null;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public List<HostRule> getHostRules() {
        return hostRules;
    }
    public void addHostRule(HostRule rule) {
        this.hostRules.add(rule);
    }
    public void removeHostRule(HostRule rule) {
        this.hostRules.remove(rule);
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(directoryPath);
        for (HostRule rule : hostRules) {
            sb.append(" ").append(rule);
        }
        return sb.toString();
    }
}

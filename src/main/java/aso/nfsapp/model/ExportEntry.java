package aso.nfsapp.model;

import java.util.ArrayList;
import java.util.List;

public class ExportEntry {
    private String directoryPath;
    private List<HostRule> hostRules;

    public ExportEntry(String directoryPath) {
        this.directoryPath = directoryPath;
        this.hostRules = new ArrayList<>();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(directoryPath);
        for (HostRule rule : hostRules) {
            sb.append(" ").append(rule);
        }
        return sb.toString();
    }
}

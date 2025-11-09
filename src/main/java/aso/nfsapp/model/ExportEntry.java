package aso.nfsapp.model;

public class ExportEntry {
    private String directoryPath;
    private String clientHost;
    private String exportOptions;

    public ExportEntry(String directoryPath, String clientHost, String exportOptions) {
        this.directoryPath = directoryPath;
        this.clientHost = clientHost;
        this.exportOptions = exportOptions;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getClientHost() {
        return clientHost;
    }
    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public String getExportOptions() {
        return exportOptions;
    }
    public void setExportOptions(String exportOptions) {
        this.exportOptions = exportOptions;
    }

    @Override
    public String toString() {
        return directoryPath + " " + clientHost + "(" + exportOptions + ")";
    }
}

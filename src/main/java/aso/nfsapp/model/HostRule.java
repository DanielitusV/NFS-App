package aso.nfsapp.model;

/**
 * Representa una regla de host en una entrada de /etc/exports.
 * Define el wildcard del host destino y las opciones NFS (rw, ro, sync, etc.).
 * Se serializa como: host(opciones)
 */
public class HostRule {
    private String hostWildCard;    // "*", "192.168.100.0/24"
    private String options;         // "rw", "ro,sync", ...

    public HostRule(String hostWildCard, String options) {
        this.hostWildCard = hostWildCard;
        this.options = options;
    }

    public String getHostWildCard() {
        return hostWildCard;
    }
    public void setHostWildCard(String hostWildCard) {
        this.hostWildCard = hostWildCard;
    }
    public String getOptions() {
        return options;
    }
    public void setOptions(String options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return hostWildCard + "(" + options + ")";
    }
}

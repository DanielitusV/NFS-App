package aso.nfsapp.model;

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

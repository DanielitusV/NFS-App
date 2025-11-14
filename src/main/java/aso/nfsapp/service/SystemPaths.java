package aso.nfsapp.service;
import java.nio.file.Path;

public final class SystemPaths {
    private SystemPaths() {}

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static Path exportsPath() {
        if (isLinux()) {
            return Path.of("/etc/exports");
        }
        return Path.of("C:\\tmp\\exports.txt");
    }

    public static Path tempExportsPath() {
        String userHome = System.getProperty("user.home");
        if (isLinux()) {
            return Path.of(userHome, ".config", "nfs-app", "exports.tmp");
        }
        return Path.of("C:\\tmp\\exports.txt");
    }
}

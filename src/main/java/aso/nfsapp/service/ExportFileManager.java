package aso.nfsapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ExportFileManager {

    public void writeFile(List<String> lines) throws IOException {
        Path path = SystemPaths.exportsPath();
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public String exportsPathString() {
        return SystemPaths.exportsPath().toString();
    }
}

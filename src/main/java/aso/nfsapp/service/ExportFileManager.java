package aso.nfsapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

/**
 * Gestiona la lectura y escritura del archivo /etc/exports.
 * En Linux escribe en archivo temporal para luego copiarlo con pkexec.
 * Preserva comentarios del archivo original al guardar.
 */
public class ExportFileManager {

    public void writeFile(List<String> lines) throws IOException {
        Path path = SystemPaths.isLinux() ? SystemPaths.tempExportsPath() : SystemPaths.exportsPath();
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        
        // Preservar comentarios del archivo original si existe
        List<String> finalLines = new ArrayList<>();
        Path originalPath = SystemPaths.exportsPath();
        
        if (Files.exists(originalPath)) {
            List<String> originalLines = Files.readAllLines(originalPath);
            // Agregar comentarios del encabezado
            for (String line : originalLines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    finalLines.add(line);
                } else {
                    break; // Dejar de agregar cuando encontremos la primera línea no comentada
                }
            }
            // Agregar línea en blanco si no hay
            if (!finalLines.isEmpty() && !finalLines.get(finalLines.size() - 1).trim().isEmpty()) {
                finalLines.add("");
            }
        }
        
        // Agregar las líneas de exportación
        finalLines.addAll(lines);
        
        Files.write(path, finalLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public String exportsPathString() {
        return SystemPaths.exportsPath().toString();
    }

    public String tempExportsPathString() {
        return SystemPaths.tempExportsPath().toString();
    }
}

package dev.fxe.recaf_forge;

import javafx.stage.DirectoryChooser;
import me.coley.recaf.ui.controls.ExceptionAlert;
import org.apache.commons.io.IOUtils;
import org.jline.utils.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Filip
 */
public class Extractor {
    private static final DirectoryChooser dirChooser = new DirectoryChooser();

    public static Path getResourcePath(String name, String resourcePath) {
        Path resource = null;
        try (InputStream in = Extractor.class.getResourceAsStream(resourcePath)) {
            String[] strings = resourcePath.split("\\.");
            String suffix = strings[strings.length - 1];
            if (in == null) {
                Log.info("Failed to find resource");
                return null;
            }
            String fileName = name == null ? String.valueOf(in.hashCode()) : name;
            File tempFile = File.createTempFile(fileName, suffix);
            tempFile.deleteOnExit();
            saveFile(tempFile.toPath(), IOUtils.toByteArray(in));
            resource = tempFile.toPath();
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to read resources");
        }
        return resource;
    }

    public static Path extractMDK(Path mdkPath) {
        Path exportDir = getExportDir();
        if (exportDir == null) return null;
        String destDirectory = exportDir.toString();
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(mdkPath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    saveFile(Paths.get(filePath), IOUtils.toByteArray(zipIn));
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to extract mdk zip");
        }
        return exportDir;
    }

    public static void saveFile(Path path, byte[] data) {
        try {
            Files.createDirectories(path.getParent());
            try (
                final BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND))
            ) {
                out.write(data);
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to write file " + path);
        }
    }

    private static Path getExportDir() {
        File file = dirChooser.showDialog(null);
        if (file == null) return null;
        return Paths.get(file.getPath() + "/");
    }
}

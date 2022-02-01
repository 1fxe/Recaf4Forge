package dev.fxe.recaf4forge.utils;

import dev.fxe.recaf4forge.Recaf4Forge;
import javafx.stage.DirectoryChooser;
import me.coley.recaf.ui.controls.ExceptionAlert;
import org.apache.commons.io.IOUtils;

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

    /**
     * Creates a temporary resource file from the jar
     *
     * @param name         file name
     * @param resourcePath path to the resource in the plugin
     * @return the path of the temp file
     */
    public static Path getResourcePath(String name, String resourcePath) {
        Path resource = null;
        try (InputStream in = Extractor.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                Recaf4Forge.info("Failed to find resource");
                return null;
            }

            String[] strings = resourcePath.split("\\.");
            String suffix = strings[strings.length - 1];
            String fileName = name == null ? String.valueOf(in.hashCode()) : name;
            File tempFile = File.createTempFile(fileName, suffix);
            tempFile.deleteOnExit();
            Extractor.saveFile(tempFile.toPath(), IOUtils.toByteArray(in));
            resource = tempFile.toPath();
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to read resources");
        }
        return resource;
    }

    /**
     * Extracts the mdk from this plugin
     *
     * @param mdkPath the mdk (e.g 1_8_9)
     * @return the path of the extracted mdk
     */
    public static Path extractMDK(Path mdkPath) {
        Path exportDir = Extractor.getExportDir();
        if (exportDir == null) return null;
        String destDirectory = exportDir.toString();
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(mdkPath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    Extractor.saveFile(Paths.get(filePath), IOUtils.toByteArray(zipIn));
                } else {
                    File dir = new File(filePath);
                    if (dir.mkdirs()) System.out.println("Success.");
                    else System.out.println("Something went wrong.");
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to extract mdk zip");
        }
        return exportDir;
    }

    /**
     * Saves a file to a specified location
     *
     * @param path location to save the file
     * @param data the file data
     */
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
        File file = Extractor.dirChooser.showDialog(null);
        if (file == null) return null;
        return Paths.get(file.getPath() + "/");
    }
}

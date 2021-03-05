package dev.fxe.recaf_forge;

import me.coley.recaf.ui.controls.ExceptionAlert;
import org.apache.commons.io.IOUtils;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Filip
 */
public class ExtractMappings {

    public static Path getMappingsPath(String resourcePath) {
        try (InputStream in = ExtractMappings.class.getResourceAsStream(resourcePath)) {
            String[] strings = resourcePath.split("\\.");
            String suffix = strings[strings.length - 1];
            if (in != null) {
                File tempFile = File.createTempFile(String.valueOf(in.hashCode()), suffix);
                tempFile.deleteOnExit();
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    byte[] array = IOUtils.toByteArray(in);
                    out.write(array);
                    return tempFile.toPath();
                } catch (Exception ex) {
                    ExceptionAlert.show(ex, "Failed to create mappings file");
                }
            } else {
                Log.info("InputStream is null");
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to read resources");
        }
        return null;
    }
}

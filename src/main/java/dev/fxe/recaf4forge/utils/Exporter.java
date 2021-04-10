package dev.fxe.recaf4forge.utils;

import dev.fxe.recaf4forge.Recaf4Forge;
import dev.fxe.recaf4forge.gui.Notification;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.decompile.DecompileImpl;
import me.coley.recaf.ui.MainWindow;
import me.coley.recaf.workspace.Workspace;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Exporter {

    private final Recaf4Forge recaf4Forge;

    public Exporter(Recaf4Forge recaf4Forge) {
        this.recaf4Forge = recaf4Forge;
    }

    public void exportMDK() {
        final Workspace workspace = this.recaf4Forge.getCurrentWorkspace();
        final String version = this.recaf4Forge.getCurrentVersion();
        if (!version.equalsIgnoreCase("") & workspace != null) {
            String path = this.getPluginResource(version, "/mdk.zip");
            String jarName = workspace.getPrimary().getShortName().toString();
            Path mdkPath = Extractor.getResourcePath(jarName, path);
            if (mdkPath == null) {
                Recaf4Forge.info("Failed to get MDK path");
                return;
            }
            Path extractedMDK = Extractor.extractMDK(mdkPath);
            if (extractedMDK == null) {
                Recaf4Forge.info("Failed to extract mdk");
                return;
            }
            CompletableFuture.runAsync(() -> {
                this.exportResources(extractedMDK);
                this.exportSrc(extractedMDK);
            }).thenRunAsync(() -> {
                String msg = "Successfully exported the source code to " + extractedMDK;
                Recaf4Forge.info(msg);
                if (this.recaf4Forge.getController() instanceof GuiController) {
                    MainWindow window = MainWindow.get((GuiController) this.recaf4Forge.getController());
                    Notification.create(BuildInfo.name + " MDK export", msg).show(window.getRoot());
                }
            });
        }
    }


    /**
     * Exports resource files to the mdk
     *
     * @param extractedMDK the path of the mdk
     */
    private void exportResources(Path extractedMDK) {
        for (Map.Entry<String, byte[]> resources : this.recaf4Forge.getCurrentWorkspace().getPrimary().getFiles().entrySet()) {
            String resourcePath =
                    extractedMDK.toString() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + resources.getKey();
            Extractor.saveFile(Paths.get(resourcePath), resources.getValue());
        }
        Recaf4Forge.info("Finished exporting resources");
    }

    /**
     * Exports decompiled source code to the mdk
     *
     * @param extractedMDK the path of the mdk
     */
    private void exportSrc(Path extractedMDK) {
        for (Map.Entry<String, byte[]> resources : this.recaf4Forge.getCurrentWorkspace().getPrimary().getClasses().entrySet()) {
            final String className = resources.getKey();
            String resourcePath =
                    extractedMDK.toString() + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + className + ".java";
            byte[] code = DecompileImpl.CFR.create(this.recaf4Forge.getController()).decompile(className).getBytes(StandardCharsets.UTF_8);
            Extractor.saveFile(Paths.get(resourcePath), code);
        }
        Recaf4Forge.info("Finished exporting source code");
    }

    public String getPluginResource(String version, String resource) {
        return "/mdks/" + version.replaceAll("\\.", "_") + resource;
    }
}

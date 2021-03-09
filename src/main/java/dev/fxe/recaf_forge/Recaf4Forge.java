package dev.fxe.recaf_forge;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import me.coley.recaf.config.Conf;
import me.coley.recaf.control.Controller;
import me.coley.recaf.decompile.DecompileImpl;
import me.coley.recaf.mapping.MappingImpl;
import me.coley.recaf.mapping.Mappings;
import me.coley.recaf.plugin.api.ConfigurablePlugin;
import me.coley.recaf.plugin.api.MenuProviderPlugin;
import me.coley.recaf.plugin.api.StartupPlugin;
import me.coley.recaf.plugin.api.WorkspacePlugin;
import me.coley.recaf.ui.controls.ActionMenuItem;
import me.coley.recaf.ui.controls.ExceptionAlert;
import me.coley.recaf.util.struct.ListeningMap;
import me.coley.recaf.workspace.Workspace;
import org.jline.utils.Log;
import org.plugface.core.annotations.Plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Filip
 */
@Plugin(name = "Recaf4Forge")
public class Recaf4Forge implements ConfigurablePlugin, MenuProviderPlugin, WorkspacePlugin, StartupPlugin {

    private final String[] versions = {"1.8", "1.8.9", "1.9.4"};
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Workspace currentWorkspace = null;
    private Controller controller;
    private String currentVersion = "";

    @Conf(value = "Automatically apply mappings", noTranslate = true)
    private boolean autoApply = false;

    @Override
    public String getName() {
        return "Recaf4Forge";
    }

    @Override
    public String getVersion() {
        return "1.0.5";
    }

    @Override
    public String getDescription() {
        return "Recaf4Forge will automatically try apply the correct mappings for a forge mod";
    }

    @Override
    public void onClosed(Workspace workspace) {
        currentWorkspace = null;
        currentVersion = "";
    }

    @Override
    public void onOpened(Workspace workspace) {
        currentWorkspace = workspace;
        if (this.autoApply) {
            CompletableFuture.runAsync(this::detectVersion, executor);
        }
    }

    private void applyMapping() {
        String path = getResourcesPath("/mappings.srg");
        Path mappingPath = Extractor.getResourcePath(null, path);
        if (mappingPath == null) {
            Log.info(getName() + " Could not find mappings");
            return;
        }
        try {
            Mappings mappings = MappingImpl.SRG.create(mappingPath, currentWorkspace);
            mappings.setCheckFieldHierarchy(true);
            mappings.setCheckMethodHierarchy(true);
            mappings.accept(currentWorkspace.getPrimary());
        } catch (Exception ex) {
            ExceptionAlert.show(ex, getName() + " Failed to apply mappings");
        }
    }

    private void exportMDK() {
        if (!currentVersion.equalsIgnoreCase("") && currentWorkspace != null) {
            String path = getResourcesPath("/mdk.zip");
            String jarName = currentWorkspace.getPrimary().getShortName().toString();
            Path mdkPath = Extractor.getResourcePath(jarName, path);
            if (mdkPath == null) return;
            Path extractedMDK = Extractor.extractMDK(mdkPath);
            if (extractedMDK == null) return;
            CompletableFuture.runAsync(() -> {
                exportResources(extractedMDK);
                exportSrc(extractedMDK);
            }, executor);
        }
    }


    private void exportResources(Path extractedMDK) {
        for (Map.Entry<String, byte[]> resources : currentWorkspace.getPrimary().getFiles().entrySet()) {
            String resourcePath =
                extractedMDK.toString() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + resources.getKey();
            Extractor.saveFile(Paths.get(resourcePath), resources.getValue());
        }
    }

    private void exportSrc(Path extractedMDK) {
        for (Map.Entry<String, byte[]> resources : currentWorkspace.getPrimary().getClasses().entrySet()) {
            final String className = resources.getKey();
            String resourcePath =
                extractedMDK.toString() + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + className + ".java";
            byte[] code = DecompileImpl.CFR.create(this.controller).decompile(className).getBytes(StandardCharsets.UTF_8);
            Extractor.saveFile(Paths.get(resourcePath), code);
        }
    }

    private String getResourcesPath(String resource) {
        return "/mdks/" + currentVersion.replaceAll("\\.", "_") + resource;
    }

    private void detectVersion() {
        ListeningMap<String, byte[]> data = currentWorkspace.getPrimary().getFiles();
        byte[] mcModInfo = data.get("mcmod.info");
        JsonValue jsonValue = Json.parse(new String(mcModInfo));
        String version = jsonValue.asArray().get(0).asObject().get("mcversion").asString();
        for (String s : versions) {
            if (s.equalsIgnoreCase(version)) {
                currentVersion = version;
                applyMapping();
                return;
            }
        }
        Log.info(getName() + " Failed to find mod version");
        currentVersion = "";
    }

    @Override
    public Menu createMenu() {
        MenuItem[] itemList = new MenuItem[versions.length + 1];
        int index = 0;
        for (String version : versions) {
            itemList[index] = new ActionMenuItem(version, () -> {
                currentVersion = version;
                CompletableFuture.runAsync(this::applyMapping, executor);
            });
            index++;
        }
        itemList[index] = new ActionMenuItem("Export MDK", this::exportMDK);
        return new Menu(getName(), null, itemList);
    }

    @Override
    public String getConfigTabTitle() {
        return getName();
    }

    @Override
    public void onStart(Controller controller) {
        this.controller = controller;
    }
}

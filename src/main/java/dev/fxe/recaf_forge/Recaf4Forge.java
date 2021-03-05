package dev.fxe.recaf_forge;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import me.coley.recaf.config.Conf;
import me.coley.recaf.mapping.MappingImpl;
import me.coley.recaf.mapping.Mappings;
import me.coley.recaf.plugin.api.ConfigurablePlugin;
import me.coley.recaf.plugin.api.MenuProviderPlugin;
import me.coley.recaf.plugin.api.WorkspacePlugin;
import me.coley.recaf.ui.controls.ActionMenuItem;
import me.coley.recaf.ui.controls.ExceptionAlert;
import me.coley.recaf.util.struct.ListeningMap;
import me.coley.recaf.workspace.Workspace;
import org.jline.utils.Log;
import org.plugface.core.annotations.Plugin;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Filip
 */
@Plugin(name = "Recaf4Forge")
public class Recaf4Forge implements ConfigurablePlugin, MenuProviderPlugin, WorkspacePlugin {

    private final String[] versions = {"1.8", "1.8.9", "1.9.4"};
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Workspace currentWorkspace = null;
    private String currentVersion = "None";

    @Conf(value = "Automatically apply mappings", noTranslate = true)
    private boolean autoApply = false;

    @Override
    public String getName() {
        return "Recaf4Forge";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Recaf4Forge will automatically try apply the correct mappings for a forge mod";
    }

    @Override
    public Menu createMenu() {
        MenuItem[] itemList = new MenuItem[versions.length];
        int index = 0;
        for (String version : versions) {
            MenuItem item = new ActionMenuItem(version, () -> {
                currentVersion = version;
                applyMapping();
            });
            itemList[index] = item;
            index++;
        }
        return new Menu(getName(), null, itemList);
    }

    @Override
    public void onClosed(Workspace workspace) {
        currentWorkspace = null;
    }


    @Override
    public void onOpened(Workspace workspace) {
        currentWorkspace = workspace;
        if (this.autoApply) {
            CompletableFuture.runAsync(this::detectVersion, executor);
        }
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

    private void applyMapping() {
        String path = "/mappings/" + currentVersion.replaceAll("\\.", "_") + "/mappings.srg";
        Path mappingPath = ExtractMappings.getMappingsPath(path);
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

    @Override
    public String getConfigTabTitle() {
        return getName();
    }
}

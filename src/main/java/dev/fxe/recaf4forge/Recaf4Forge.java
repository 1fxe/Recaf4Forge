package dev.fxe.recaf4forge;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import dev.fxe.recaf4forge.gui.Notification;
import dev.fxe.recaf4forge.utils.Exporter;
import dev.fxe.recaf4forge.utils.Extractor;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import me.coley.recaf.config.Conf;
import me.coley.recaf.control.Controller;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.mapping.MappingImpl;
import me.coley.recaf.mapping.Mappings;
import me.coley.recaf.plugin.api.ConfigurablePlugin;
import me.coley.recaf.plugin.api.MenuProviderPlugin;
import me.coley.recaf.plugin.api.StartupPlugin;
import me.coley.recaf.plugin.api.WorkspacePlugin;
import me.coley.recaf.ui.MainWindow;
import me.coley.recaf.ui.controls.ActionMenuItem;
import me.coley.recaf.ui.controls.ExceptionAlert;
import me.coley.recaf.ui.controls.node.ClassNodeEditorPane;
import me.coley.recaf.ui.controls.view.ClassViewport;
import me.coley.recaf.util.Log;
import me.coley.recaf.util.ThreadUtil;
import me.coley.recaf.util.struct.ListeningMap;
import me.coley.recaf.workspace.Workspace;
import org.plugface.core.annotations.Plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.fxe.recaf4forge.utils.BuildInfo.*;

/**
 * @author Filip
 */
@SuppressWarnings("unused")
@Plugin(name = name)
public class Recaf4Forge implements ConfigurablePlugin, MenuProviderPlugin, WorkspacePlugin, StartupPlugin {

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Exporter exporter = new Exporter(this);

	private Workspace currentWorkspace = null;
	private String currentVersion = "";
	private Controller controller;

	@Conf(value = "Automatically apply mappings", noTranslate = true)
	private boolean autoApply = false;

	@Conf(value = "Notifications", noTranslate = true)
	private boolean notify = false;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void onClosed(Workspace workspace) {
		this.currentWorkspace = null;
		this.currentVersion = "";
	}

	@Override
	public void onOpened(Workspace workspace) {
		this.currentWorkspace = workspace;
		if (this.autoApply) {
			CompletableFuture.runAsync(this::detectVersion, this.executor);
		}
	}

	@Override
	public String getConfigTabTitle() {
		return name;
	}

	@Override
	public void onStart(Controller controller) {
		this.controller = controller;
	}

	@Override
	public Menu createMenu() {
		MenuItem[] itemList = new MenuItem[Versions.MAP.size() + 1];
		int index = 0;
		for (String version : Versions.MAP.keySet()) {
			itemList[index] = new ActionMenuItem(version, () -> {
				this.currentVersion = version;
				CompletableFuture.runAsync(this::applyMapping, this.executor);
			});
			index++;
		}
		itemList[index] = new ActionMenuItem("Export MDK", this.exporter::exportMDK);
		return new Menu(this.getName(), null, itemList);
	}

	private void applyMapping() {
		if (this.currentWorkspace == null) return;
		MappingImpl mappingIml = Versions.MAP.get(this.currentVersion);
		String path = this.exporter.getPluginResource(this.currentVersion, ("/mappings." + mappingIml).toLowerCase());
		Path mappingPath = Extractor.getResourcePath(null, path);
		if (mappingPath == null) {
			Recaf4Forge.info("Could not find mappings, is the resource null?");
			return;
		}
		try {
			Mappings mappings = mappingIml.create(mappingPath, this.currentWorkspace);
			mappings.setCheckFieldHierarchy(true);
			mappings.setCheckMethodHierarchy(true);
			mappings.accept(this.currentWorkspace.getPrimary());
			Recaf4Forge.info("Finished applying mappings");
			if (this.getController() instanceof GuiController) {
				MainWindow window = MainWindow.get((GuiController) this.getController());
				ThreadUtil.checkJfxAndEnqueue(() -> {
					for (Tab tab : window.getTabs().getTabs()) {
						if (tab.getContent() instanceof ClassViewport) {
							ClassViewport classViewport = (ClassViewport) tab.getContent();
							classViewport.updateView();
						}
					}
				});

				if (this.notify) {
					Notification.create(this.getName() + " Mappings", "Successfully applied mappings!").show(window.getRoot());
				}
			}
		} catch (Exception ex) {
			ExceptionAlert.show(ex, this.getName() + " Failed to apply mappings");
		}
	}

	private void detectVersion() {
		ListeningMap<String, byte[]> data = this.currentWorkspace.getPrimary().getFiles();
		byte[] mcModInfo = data.get("mcmod.info");
		JsonValue jsonValue = Json.parse(new String(mcModInfo));
		String version = jsonValue.asArray().get(0).asObject().get("mcversion").asString();
		for (String s : Versions.MAP.keySet()) {
			if (s.equalsIgnoreCase(version)) {
				this.currentVersion = version;
				this.applyMapping();
				return;
			}
		}
		Recaf4Forge.info("Failed to find mod version");
		this.currentVersion = "";
	}

	public String getCurrentVersion() {
		return this.currentVersion;
	}

	public Workspace getCurrentWorkspace() {
		return this.currentWorkspace;
	}

	public Controller getController() {
		return this.controller;
	}

	public static void info(String msg) {
		Log.info(name + " " + msg);
	}
}

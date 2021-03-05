# Recaf plugin workspace

This is a sample maven workspace for creating plugins for Recaf `2.X`.

## Plugin documentation

The official documentation can be found here: [Recaf Docs:Plugins](https://col-e.github.io/Recaf/doc-advanced-plugin.html).

The source and javadoc artifacts are also available and can be fetched in your IDE workspace.

## Building & modification

Once you've downloaded or cloned the repository, you can compile with `mvn clean package`. 
This will generate the file `target/plugin-{VERSION}.jar`. To add your plugin to Recaf:

1. Navigate to the `plugins` folder.
    - Windows: `%HOMEPATH%/Recaf/plugins`
	- Linux: `$HOME/Recaf/plugins`
2. Copy your plugin jar into this folder
3. Run Recaf to verify your plugin loads.
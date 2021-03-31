<div align="center">

# Recaf4Forge

Inspired by [Luyten4Forge](https://github.com/KevinPriv/Luyten4Forge)

![VERSION](https://img.shields.io/github/v/release/1fxe/Recaf4Forge?style=flat-square)
![ISSUES](https://img.shields.io/github/issues/1fxe/Recaf4Forge?style=flat-square)

</div>

## What is this?

This is a plugin for Recaf which can automatically detects a Minecraft Forge Mods version using the mcmod.info and
applies the correct mapping. It also allows you to export the Forge MDK for the version.

The mappings and mdks are stored in this jar file, so you don't have to manually find them.

<hr>

**Supported Versions**

Version | Mappings | MDK
 --- | --- | --- 
**1.8** | ✔ | ❌
**1.8.9** | ✔ | ✔
**1.9.4** | ✔ | ✔

### Config

Apply Mappings automatically - off by default, parses mcmod.info to try read the version

Notifications - Off by default, Creates a popup when mappings are applied and/or source is extracted

### Example Usage

<details>
  <summary>Click here to see an example of automatic mappings</summary>

![Automatic Example](.github/automatic.gif)

</details>

<details>
  <summary>Click here to see an example of manual mappings</summary>

![Manual Example](.github/manual.gif)

</details>

#### TODO

- Add support for other versions
- Add translations?
- Fix notifications disappearing too fast

<hr>

## Contributing

Contributors are welcome 👍🏽

Thanks [Recaf-development](https://github.com/videogame-player/recaf-development) for the gradle plugin

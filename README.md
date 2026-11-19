# Item Pickup Filter

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Loader](https://img.shields.io/badge/Loader-Fabric-blue)
![Environment](https://img.shields.io/badge/Environment-Client--Side-green)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21%20→%2026.3-yellow)

**Item Pickup Filter** is a client-side Fabric mod that keeps junk out of your inventory. Pick a blocklist (never keep these items) or a whitelist (only keep these items) and the mod throws unwanted items straight back out, with the pickup sound and animation hidden.

It is purely client-side: it works on vanilla servers and in singleplayer, and the server does not need the mod.

## Features

* **Blocklist or whitelist** mode.
* **In-game config screen** (Cloth Config), also reachable from Mod Menu.
* **Chat commands** for quick changes: `!ipf`.
* **Hold sneak** to keep everything while you hold the key.
* Works with items from other mods (use the full `modid:item` id).
* Feedback: `silent`, an action-bar `message`, or a `full` chat message for every dropped item.

## Requirements

* Fabric Loader and [Fabric API](https://modrinth.com/mod/fabric-api)
* [Cloth Config](https://modrinth.com/mod/cloth-config) (required)
* [Mod Menu](https://modrinth.com/mod/modmenu) (optional, adds a config button)

## Supported versions

| Minecraft | Jar |
|-----------|-----|
| 1.21 - 1.21.1 | `item-pickup-filter-<version>+1.21.jar` |
| 1.21.2 - 1.21.8 | `item-pickup-filter-<version>+1.21.2.jar` |
| 1.21.9 - 1.21.11 | `item-pickup-filter-<version>+1.21.9.jar` |
| 26.1.x | `item-pickup-filter-<version>+26.1.jar` |
| 26.2 - 26.3 | `item-pickup-filter-<version>+26.2.jar` |

Pick the jar that matches your game version. Minecraft 26.1 and newer need Java 25, older versions Java 21.

## Usage

Press **N** (rebindable under Controls) to open the config screen, or use chat commands:

| Command | What it does |
|---------|--------------|
| `!ipf` / `!ipf help` | Show the command list |
| `!ipf toggle` | Turn the filter on or off |
| `!ipf status` | Show on/off, mode and item count |
| `!ipf add <item> [item...]` | Add items (`dirt` means `minecraft:dirt`) |
| `!ipf remove <item> [item...]` | Remove items |
| `!ipf list` / `!ipf clear` | Show or empty the list |
| `!ipf mode whitelist\|blocklist` | Choose the mode |
| `!ipf feedback silent\|message\|full` | Choose the feedback |

Notes:

* An **empty whitelist does nothing**. Otherwise switching to whitelist mode would throw away your whole inventory.
* The filter only acts on your own inventory and pauses while a chest or other container is open.
* Items you throw away land at your feet and can be picked up again after about 2 seconds. Step away or hold sneak.
* Config file: `config/ipf.json`.

## Building

Needs JDK 25 to run Gradle (the 1.21.x jars still target Java 21).

```
./gradlew build                      # 1.21.8 jar, the default target
./build-all.ps1                      # every supported version, jars end up in dist
```

`gradle.properties` holds the default target. `build-all.ps1` lists the exact Minecraft, mapping, Fabric API, Cloth Config and Mod Menu versions used for each jar. Source layout:

* `src/shared`: version independent code (config, command parsing, drop decision)
* `src/yarn`: Minecraft 1.21 - 1.21.11 (Yarn names)
* `src/official`: Minecraft 26.1+ (official names)
* `src/compat/<name>`: the few calls that differ between Minecraft versions

## Contributing

Fork, branch, commit, open a pull request. Issues are welcome in the [Issues tab](../../issues); please include your game version, mod version and the log.

## License

MIT, see `LICENSE.md`.

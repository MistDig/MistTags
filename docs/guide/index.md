# Installation

MistTags is a Bukkit-family plugin for temporary and permanent player prefixes/suffixes.

## Requirements

- Java 17 or newer
- Bukkit, Spigot, Paper, Purpur, or Folia
- Minecraft versions tested/targeted by the beta line: `1.19.x`, `1.20.x`, `1.21.x`, `26.1.x`, and `26.2.x`

## Optional Plugins

MistTags works by itself, but can integrate with:

- PlaceholderAPI
- TAB
- LPC
- LuckPerms
- NametagEdit
- Essentials
- DeluxeTags
- UltraPrefixes
- TitleManager

## Setup

1. Download the latest beta jar from the [GitHub Releases](https://github.com/MistDig/MistTags/releases).
2. Put the jar in your server's `plugins` folder.
3. Start the server once.
4. Edit `plugins/MistTags/config.yml` if needed.
5. Restart, or run `/mt reload`.

## First Test

Give yourself a test prefix:

```text
/mt addprefix YourName 10m <green>[VIP]</green>
```

Preview a tag above your hotbar:

```text
/mt preview <gold>[STAFF]</gold>
```

Remove the test prefix:

```text
/mt removeprefix YourName
```

## Storage

By default, player tag data is stored in `plugins/MistTags/data.yml`.

If `database.enabled: true`, MistTags ignores `data.yml` and uses SQLite/MySQL through JDBC instead.

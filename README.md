# MistTags 2.2 Beta

MistTags is a lightweight prefix/suffix plugin for staff-controlled temporary and permanent tags.

Documentation: https://mistdig.github.io/MistTags/

## Compatibility

This compatibility build targets Java 17 and the Spigot/Bukkit 1.19 API, while using reflection for Paper/Folia scheduler features when they exist.

Expected loaders:
- Bukkit
- Spigot
- Paper
- Purpur
- Folia

Expected game versions:
- 1.19.x
- 1.20.x
- 1.21.x
- 26.1.x / 26.2.x, pending live testing

This is a beta build. Test on your exact server jar before marking it stable.

## Features

- Temporary and permanent prefixes/suffixes
- `/mt addprefix`, `/mt addsuffix`, `/mt removeprefix`, `/mt removesuffix`
- `/mt list`, `/mt check`, `/mt stats`, `/mt preview`, `/mt reload`
- Staff manage menu with delete, edit-command, and time-left buttons
- Animated tags from `animations.yml`
- TAB/LPC/LuckPerms-safe display behavior
- PlaceholderAPI expansion with GUI-friendly placeholders
- Editable messages in `msg.yml`
- YAML storage by default
- Optional SQLite/MySQL storage
- Any Unicode emoji/symbol in tags, if the client/server/display plugin can render it

## Display Modes

- `auto`: if TAB or LPC is installed with PlaceholderAPI, MistTags auto-wires smart placeholders into those configs and hands display/chat to them. If no supported display/chat plugin is found, MistTags uses standalone mode.
- `tab`: pushes tags into TAB. Normal TAB/LuckPerms prefixes stay until a player has an active MistTags tag; then MistTags wins for that player.
- `standalone`: MistTags formats chat/nametags/tablist itself.
- `placeholder-only`: MistTags only exposes PlaceholderAPI values for another plugin to render.

Smart placeholders:
- `%misttags_display_prefix%`: MistTags prefix if active, otherwise LuckPerms prefix.
- `%misttags_display_suffix%`: MistTags suffix if active, otherwise LuckPerms suffix.

## Notes

If using MySQL and emojis, use an `utf8mb4` database/table/connection setup.

For Modrinth, upload the jar as beta until tested on the exact versions/loaders you select.

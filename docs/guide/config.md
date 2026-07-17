# Configuration

MistTags creates its files in `plugins/MistTags`.

| File | Purpose |
| --- | --- |
| `config.yml` | Main settings, display mode, integrations, storage, and custom tag limits. |
| `msg.yml` | Editable messages sent by MistTags. |
| `commands.yml` | Editable extra command aliases. |
| `animations.yml` | Animation IDs and frames. |
| `placeholders.txt` | Plain text list of PlaceholderAPI placeholders. |
| `active-tags.txt` | Clean text list of active player tags, kept for recent active users. |
| `misttags.db` | Default SQLite player tag database. |

## Display Modes

| Mode | Behavior |
| --- | --- |
| `auto` | Recommended. Auto-configures TAB/LPC when PlaceholderAPI is installed; otherwise uses standalone display. |
| `standalone` | MistTags formats chat, tab list, and nametags itself. |
| `tab` | MistTags pushes tags directly into TAB's API. |
| `placeholder-only` | MistTags only exposes placeholders for another plugin to render. |

## Integrations

```yaml
integrations:
  auto-configure: true
  auto-reload: true
  tab:
    enabled: true
  lpc:
    enabled: true
```

When enabled, MistTags makes a one-time `.misttags-backup` beside the TAB/LPC config before editing it.

## Storage

```yaml
storage:
  type: sqlite
  sqlite:
    file: misttags.db
  mysql:
    host: 127.0.0.1
    port: 3306
    database: misttags
    username: ""
    password: ""
    use-ssl: false
```

SQLite is the default and stores player tag data in `plugins/MistTags/misttags.db`.

If an old `data.yml` exists and the database is empty, MistTags imports it once and then leaves it as a backup.

## Command Aliases

`commands.yml` lets you add extra command names without changing what the commands do.

```yaml
commands:
  misttags:
    target: "misttags"
    aliases:
      - "misttags"
      - "mt"
      - "tags"

  addprefix:
    target: "addprefix"
    aliases:
      - "addprefix"
      - "setprefix"
```

Built-in commands still exist. Custom aliases are rewritten to the same internal MistTags actions.

## Custom Tags

The `custom` section controls self-service tags for players with `misttags.custom`.

Staff permissions such as `misttags.manage.addprefix` bypass these limits.

Key options:

| Option | Purpose |
| --- | --- |
| `cooldown-seconds` | Time a player must wait between changing their own prefix/suffix. |
| `min-length` | Minimum visible tag length. |
| `max-length` | Maximum visible tag length. |
| `banned-words` | Case-insensitive text blacklist. |
| `banned-patterns` | Optional regex blacklist. |
| `default-max-duration` | Default self-service duration cap. |
| `duration-tiers` | Permission-based duration caps. |

## Emojis

MistTags does not whitelist emojis. Any Unicode emoji or symbol can be used if the Minecraft client, server, font, and display plugin can render it.

For MySQL/MariaDB emoji storage, use an `utf8mb4` database/table/connection setup.

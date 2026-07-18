# Commands

All main commands use `/misttags` with alias `/mt`.

## Staff Commands

| Command | Description |
| --- | --- |
| `/mt addprefix <player> <duration> <tag/anim:id>` | Add or replace a player's prefix. |
| `/mt addsuffix <player> <duration> <tag/anim:id>` | Add or replace a player's suffix. |
| `/mt removeprefix <player>` | Remove a player's active MistTags prefix. |
| `/mt removesuffix <player>` | Remove a player's active MistTags suffix. |
| `/mt list` | List all active tags. |
| `/mt list <player>` | View one player's active tags. |
| `/mt check <player>` | Open the staff manage menu for a player. |
| `/mt stats` | Show counts for active tags, animations, cache, storage, and display mode. |
| `/mt preview <tag>` | Show a tag above your hotbar for 5 seconds. |
| `/mt reload` | Reload config, messages, animations, and integrations. |

Legacy command aliases are also available:

| Command | Description |
| --- | --- |
| `/addprefix <player> <duration> <tag/anim:id>` | Same as `/mt addprefix`. |
| `/addsuffix <player> <duration> <tag/anim:id>` | Same as `/mt addsuffix`. |
| `/removeprefix <player>` | Same as `/mt removeprefix`. |
| `/removesuffix <player>` | Same as `/mt removesuffix`. |

## Duration Examples

| Value | Meaning |
| --- | --- |
| `10m` | 10 minutes |
| `2h` | 2 hours |
| `7d` | 7 days |
| `perm` | Permanent |
| `permanent` | Permanent |
| `forever` | Permanent |
| `never` | Permanent |

## Animation Examples

Use an animation from `animations.yml` by writing `anim:<id>`.

```text
/mt addprefix Steve 30m anim:rainbow
/mt addsuffix Alex perm anim:pulse
```

Template animations can use `{text}` in `animations.yml`, then the command supplies the text:

```text
/mt addprefix MistDig 1m anim:gradient_chroma VIP
/mt addsuffix MistDig 1m anim:gradient_chroma IMMORTAL
```

## Manage Menu

`/mt check <player>` opens the staff manage menu.

It shows:

- rendered prefix and suffix
- delete prefix/suffix buttons
- edit prefix/suffix buttons
- prefix/suffix time buttons

On Paper builds with Dialog UI support, MistTags opens a native dialog. Edit dialogs include both the tag text and duration, so edits do not silently become permanent.

Rows in `/mt list` are clickable for players and open the same menu.

## Custom Command Aliases

`plugins/MistTags/commands.yml` can add extra command names while keeping the same MistTags behavior.

```yaml
commands:
  misttags:
    target: "misttags"
    aliases:
      - "misttags"
      - "mt"
      - "tags"
```

With that example, `/tags list` acts like `/mt list`.

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `misttags.manage` | Full access to add/remove prefixes and suffixes for any player. | `op` |
| `misttags.manage.addprefix` | Add or edit a prefix for any player. | `op` |
| `misttags.manage.addsuffix` | Add or edit a suffix for any player. | `op` |
| `misttags.manage.removeprefix` | Remove any player's prefix. | `op` |
| `misttags.manage.removesuffix` | Remove any player's suffix. | `op` |
| `misttags.custom` | Let a player manage only their own tag within config limits. | `false` |
| `misttags.reload` | Use `/mt reload`. | `op` |
| `misttags.list` | Use `/mt list`. | `op` |
| `misttags.check` | Use `/mt check`. | `op` |
| `misttags.stats` | Use `/mt stats`. | `op` |
| `misttags.preview` | Use `/mt preview`. | `true` |
| `misttags.custom.duration.1d` | Example self-service duration tier. | `false` |
| `misttags.custom.duration.10d` | Example self-service duration tier. | `false` |
| `misttags.custom.duration.month` | Example self-service duration tier. | `false` |
| `misttags.custom.duration.year` | Example self-service duration tier. | `false` |

# Groups

`groups.yml` gives every LuckPerms group a default prefix and/or suffix, without needing a
player to run `/mt addprefix` first. It's the fallback that sits between an active MistTags
tag and the raw LuckPerms prefix -- see the order below.

Introduced in 2.4.0-beta.

## Why Not Just Use A LuckPerms Prefix?

You can -- for plain colored text, a LuckPerms prefix works fine everywhere already.

`groups.yml` exists specifically for **animated** group tags. TAB has its own `%animation:name%`
syntax, but that syntax only gets re-parsed correctly wherever TAB itself builds the text
(tablist, nametags). If you paste `%animation:sovereign%` straight into a LuckPerms prefix meta
value, it renders fine in the tablist but prints out as literal, unresolved text everywhere else
-- chat, staff-chat, `/msg`, any plugin that does a single PlaceholderAPI pass instead of TAB's
own animation-aware renderer.

`groups.yml` values are rendered by MistTags itself, the same `anim:<name>` resolution
`/mt addprefix` uses. That means `%misttags_display_prefix%` always returns finished,
already-animated text -- safe for any consumer, chat included.

## Setup

Define the animation once in `animations.yml` (see [Animation Presets](/guide/animations) for
ready-made ones), then reference it by name in `groups.yml`:

```yaml
# plugins/MistTags/animations.yml
animations:
  sovereign:
    update-ticks: 50
    frames:
      - "<b><gradient:#F2B4FF:#A226AD>SOVEREIGN</gradient></b>"
      - "<b><gradient:#F2B4FF:#A428B0>SOVEREIGN</gradient></b>"
```

```yaml
# plugins/MistTags/groups.yml
groups:
  sovereign:
    prefix: "anim:sovereign"
    suffix: ""

  owner:
    prefix: "<gold><bold>OWNER"
    suffix: ""
```

Values don't have to be animated -- a plain MiniMessage string works too, as the `owner`
example above shows.

The key (`sovereign`, `owner`, ...) is matched against the player's LuckPerms primary group via
`%luckperms_primary_group_name%`, case-insensitively. Reload with `/mt reload` or restart to
pick up changes.

## Fallback Order

For a player with no group entry configured, or no PlaceholderAPI/LuckPerms installed,
`%misttags_display_prefix%`/`%misttags_display_suffix%` fall through in this order:

1. **Active MistTags tag** -- from `/mt addprefix`/`/mt addsuffix`. Always wins if present.
2. **Group tag** -- looked up in `groups.yml` by the player's LuckPerms primary group.
3. **Raw LuckPerms prefix/suffix** -- via `%luckperms_prefix%`/`%luckperms_suffix%`, exactly as
   before `groups.yml` existed.

Staff running `/mt addprefix <player> <duration> <tag>` overrides both the group tag and the
LuckPerms rank prefix for that player -- in chat too, since `ChatListener` runs at `HIGHEST`
priority specifically so an active tag wins that race against other chat-formatting plugins.
Removing the MistTags prefix restores the group tag (or LuckPerms prefix, if no group entry
exists).

## TAB Setup

Point TAB's `groups.yml` at the same smart placeholders as always -- nothing changes here,
MistTags' own `groups.yml` sits earlier in the fallback chain, so TAB doesn't need to know it
exists:

```yaml
_DEFAULT_:
  tabprefix: "%misttags_display_prefix%"
  tagprefix: "%misttags_display_prefix%"
  customtabname: "%player%"
  tabsuffix: "%misttags_display_suffix%"
  tagsuffix: "%misttags_display_suffix%"
```

See [Integrations](/guide/integrations) for the full TAB/LuckPerms/LPC setup.

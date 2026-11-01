# Groups

`groups.yml` gives every LuckPerms group a default prefix and/or suffix, without needing a
player to run `/mt addprefix` first. It's the fallback that sits between an active MistTags
tag and the raw LuckPerms prefix -- see the order below.

Introduced in 2.4.0-beta.

## Why Not Just Use A LuckPerms Prefix?

You can -- for plain colored text, a LuckPerms prefix works fine everywhere already.

`groups.yml` exists specifically for **animated** group tags. TAB has its own `%animation:name%`
syntax, but that syntax only gets re-parsed correctly wherever TAB itself builds the text
(tablist, nametags). If you paste `%animation:<name>%` straight into a LuckPerms prefix meta
value, it renders fine in the tablist but prints out as literal, unresolved text everywhere else
-- chat, staff-chat, `/msg`, any plugin that does a single PlaceholderAPI pass instead of TAB's
own animation-aware renderer.

`groups.yml` values are rendered by MistTags itself, the same `anim:<name>` resolution
`/mt addprefix` uses. That means `%misttags_display_prefix%` always returns finished,
already-animated text -- safe for any consumer, chat included.

## Setup

Reference any animation already in `animations.yml` by name (see [Animation Presets](/guide/animations)
for the full ready-made list this plugin ships with), or define your own:

```yaml
# plugins/MistTags/groups.yml
groups:
  vip:               # <- your LuckPerms group's NAME, e.g. `/lp creategroup vip`
    prefix: "anim:gradient_vip"
    suffix: ""

  owner:
    prefix: "<gold><bold>OWNER"
    suffix: ""
```

Values don't have to be animated -- a plain MiniMessage string works too, as the `owner`
example above shows.

The key (`vip`, `owner`, ...) is matched against the player's LuckPerms **primary group name**
via `%luckperms_primary_group_name%`, case-insensitively -- use whatever group names your own
LuckPerms setup actually has (check with `/lp listgroups` or `/lp user <player> info`).

> **This is not the same thing as a LuckPerms prefix.** You are not setting anything on the
> group in LuckPerms -- no `/lp group <name> meta setprefix ...`, nothing in LuckPerms at all.
> The `vip` in `groups.yml` only has to match the *group's name*. A player just needs to be a
> member of that group (`/lp user <player> parent add vip`); whatever that group's own
> LuckPerms prefix meta says is irrelevant here, since `groups.yml` is a separate, earlier
> fallback layer that overrides it (see the order below).

Reload with `/mt reload` or restart to pick up changes.

### Adding A Placeholder Badge (e.g. a Discord-link checkmark)

A value can lead with arbitrary text -- including a PlaceholderAPI placeholder -- before an
`anim:<name>` reference, and both parts get resolved:

```yaml
groups:
  vip:
    prefix: "%tick_linked%anim:gradient_vip"
    suffix: ""
```

`anim:<name>` still has to be a literal, unbroken token starting the animation portion (`anim:`
can't appear earlier by coincidence in your badge text) -- everything before the first `anim:`
is treated as the badge and resolved via PlaceholderAPI once the animation frame is rendered.
This also works with no animation at all -- a group's `prefix`/`suffix` can just be
`"%some_placeholder%"` on its own.

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

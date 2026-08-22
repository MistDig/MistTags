# Integrations

MistTags can run alone or hand display/chat to other plugins.

## PlaceholderAPI

PlaceholderAPI is required for external plugins to read MistTags placeholders.

MistTags registers:

```text
%misttags_prefix%
%misttags_suffix%
%misttags_display_prefix%
%misttags_display_suffix%
```

## TAB

When TAB and PlaceholderAPI are installed, `display.mode: auto` can write these into `plugins/TAB/groups.yml`:

```yaml
_DEFAULT_:
  tabprefix: "%misttags_display_prefix%"
  tagprefix: "%misttags_display_prefix%"
  customtabname: "%player%"
  tabsuffix: "%misttags_display_suffix%"
  tagsuffix: "%misttags_display_suffix%"
```

This means:

- A player with a MistTags prefix shows the MistTags prefix.
- A player without a MistTags prefix falls back to LuckPerms.
- Removing the MistTags prefix restores the normal LuckPerms display.
- Prefix placeholders include one trailing display space when not empty. Suffix placeholders include one leading display space when not empty.

Do not use TAB's global `placeholder-output-replacements` to turn an empty `%misttags_display_suffix%` into `None` unless you want `None` to appear everywhere, including nametags. For scoreboards or player info menus, use:

```text
%misttags_suffix_or_none%
```

Keep TAB nametags on:

```text
%misttags_display_suffix%
```

## LPC

When LPC and PlaceholderAPI are installed, MistTags can write smart placeholders into `plugins/LPC/config.yml`:

```yaml
chat-format: "%misttags_display_prefix%{name}%misttags_display_suffix%<dark_gray> »<reset> {message}"
```

Chat lines do not live-update after being sent. Animated prefixes can show the current frame when a message is sent, but old chat messages stay the same.

## LuckPerms

MistTags does not edit LuckPerms data.

The smart display placeholders use LuckPerms as fallback when no MistTags tag is active:

```text
%misttags_display_prefix%
%misttags_display_suffix%
```

### Animated Rank Prefixes: Use `groups.yml`, Not A Raw LuckPerms Meta Value

Earlier versions of this page recommended putting TAB's own animation syntax straight into a
LuckPerms prefix meta value, e.g. `/lp user <player> meta setprefix 100 "%animation:sovereign%"`.
**Don't do that.** `%animation:name%` is TAB-internal syntax: it only gets re-parsed correctly
wherever TAB itself builds the text (tablist, nametags). Any other consumer of a LuckPerms
prefix -- chat plugins, staff-chat, `/msg` formats, anything doing a single PlaceholderAPI
pass -- prints it back out as literal unresolved text, because `%animation:sovereign%` was
never a real PlaceholderAPI placeholder in the first place. This is a common way to end up
with `%animation:sovereign%MistDig » hi` showing up in chat while the tablist looks fine.

Use `plugins/MistTags/groups.yml` instead. Group prefixes/suffixes defined there are rendered
by MistTags itself -- the same `anim:<name>` animation resolution `/mt addprefix` uses -- so
`%misttags_display_prefix%`/`%misttags_display_suffix%` always return finished, already-
animated text no matter who's asking for it:

```yaml
# plugins/MistTags/groups.yml
groups:
  sovereign:
    prefix: "anim:sovereign"
    suffix: ""
```

Matched against the player's LuckPerms primary group (`%luckperms_primary_group_name%`),
case-insensitively. Recommended TAB group setup (unchanged -- TAB still just points at the
smart placeholders):

```yaml
_DEFAULT_:
  tabprefix: "%misttags_display_prefix%"
  tagprefix: "%misttags_display_prefix%"
  customtabname: "%player%"
  tabsuffix: "%misttags_display_suffix%"
  tagsuffix: "%misttags_display_suffix%"
```

Behavior:

- If the player has no MistTags prefix, `%misttags_display_prefix%` checks `groups.yml` for
  their primary group before falling back further to the raw LuckPerms prefix.
- If staff run `/mt addprefix <player> <duration> <tag>`, the MistTags prefix overrides both
  the group tag and the LuckPerms/TAB rank prefix for that player -- in chat too, since
  `ChatListener` runs at `HIGHEST` priority specifically so an active tag wins that race.
- Removing the MistTags prefix restores the normal group/LuckPerms prefix.

You still only need to define the animation once, in `animations.yml`:

```text
/mt addprefix <player> 30d anim:sovereign SOVEREIGN
```

## Manual PlaceholderAPI Compatibility

These plugins are not auto-configured by MistTags, but can usually use MistTags placeholders manually if they support PlaceholderAPI:

- NametagEdit
- Essentials
- DeluxeTags
- UltraPrefixes
- TitleManager

Use:

```text
%misttags_display_prefix%
%misttags_display_suffix%
```

## Standalone Mode

If TAB/LPC are not installed, MistTags can format chat, tab list, and nametags itself.

```yaml
display:
  mode: standalone
```

Standalone mode is useful for small servers that do not want a separate chat or tab plugin.

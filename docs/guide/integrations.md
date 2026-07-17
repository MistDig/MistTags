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

# Placeholders

MistTags registers a PlaceholderAPI expansion with the identifier `misttags`.

## Player Tag Placeholders

| Placeholder | Description |
| --- | --- |
| `%misttags_prefix%` | Current rendered prefix, including animation frame if animated. |
| `%misttags_suffix%` | Current rendered suffix, including animation frame if animated. |
| `%misttags_display_prefix%` | MistTags prefix if active; otherwise LuckPerms prefix if available through PlaceholderAPI. |
| `%misttags_display_suffix%` | MistTags suffix if active; otherwise LuckPerms suffix if available through PlaceholderAPI. |
| `%misttags_prefix_raw%` | Stored prefix value, such as `anim:rainbow`. |
| `%misttags_suffix_raw%` | Stored suffix value, such as `anim:pulse`. |
| `%misttags_prefix_plain%` | Prefix with formatting removed. |
| `%misttags_suffix_plain%` | Suffix with formatting removed. |
| `%misttags_has_prefix%` | `true` or `false`. |
| `%misttags_has_suffix%` | `true` or `false`. |
| `%misttags_has_any%` | `true` or `false`. |
| `%misttags_prefix_expires_at%` | Prefix expiry in Unix milliseconds, or `0`. |
| `%misttags_suffix_expires_at%` | Suffix expiry in Unix milliseconds, or `0`. |
| `%misttags_prefix_remaining_seconds%` | Seconds until prefix expires, or `0`. |
| `%misttags_suffix_remaining_seconds%` | Seconds until suffix expires, or `0`. |
| `%misttags_prefix_remaining%` | Human-readable time until prefix expiry. |
| `%misttags_suffix_remaining%` | Human-readable time until suffix expiry. |
| `%misttags_prefix_permanent%` | `true` or `false`. |
| `%misttags_suffix_permanent%` | `true` or `false`. |
| `%misttags_name_color%` | Color derived from the last color in the prefix. |
| `%misttags_last_seen%` | Last seen time in Unix milliseconds, or `0`. |
| `%misttags_last_seen_iso%` | Last seen time as ISO-8601, or blank. |
| `%misttags_custom_prefix_cooldown_seconds%` | Remaining self-service prefix cooldown seconds. |
| `%misttags_custom_suffix_cooldown_seconds%` | Remaining self-service suffix cooldown seconds. |

## Server and Stats Placeholders

| Placeholder | Description |
| --- | --- |
| `%misttags_active_prefixes%` | Count of active prefixes. |
| `%misttags_active_suffixes%` | Count of active suffixes. |
| `%misttags_active_players%` | Count of players with a prefix or suffix. |
| `%misttags_animated_players%` | Count of players using an animated prefix or suffix. |
| `%misttags_animations_loaded%` | Count of loaded animations. |
| `%misttags_animations_running%` | Count of animations with more than one frame. |
| `%misttags_cached_players%` | Count of cached player records. |
| `%misttags_display_mode%` | `standalone`, `tab`, or `placeholder-only`. |
| `%misttags_database_enabled%` | `true` or `false`. |

## Smart Display Placeholders

Use these in TAB, LPC, chat plugins, menus, and GUIs:

```text
%misttags_display_prefix%%player_name%%misttags_display_suffix%
```

They show MistTags first. If the player has no MistTags prefix or suffix, they fall back to LuckPerms.

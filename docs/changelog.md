# Changelog

## 2.2-beta

- Added `/mt check <player>` staff manage menu.
- Made `/mt list` show rendered colored prefixes/suffixes instead of raw MiniMessage.
- Made `/mt list` rows clickable for players, opening the manage menu.
- Added manage buttons for delete, edit-command suggestion, and time-left checks.
- Fixed `/mt preview` to last 5 seconds.
- Updated plugin website metadata to the MistTags docs site.
- Added default-message fallback so new messages still work on old `msg.yml` files.

## 2.1.2-beta

- Added plugin description, website, and author metadata so `/version MistTags` shows useful in-game information.

## 2.1.1-beta

- Fixed legacy color handling by using safe Unicode escapes for section-color conversion.
- Fixed the default `rainbow` animation's purple frame to use valid MiniMessage color syntax.
- Kept TAB/LPC smart-placeholder auto setup from `2.1-beta`.

## 2.1-beta

- Initial public beta release.
- Added temporary and permanent prefixes/suffixes.
- Added `/mt list`, `/mt stats`, `/mt preview`, and `/mt reload`.
- Added PlaceholderAPI placeholders, editable `msg.yml`, animations, YAML/database storage, and TAB/LPC smart integration.

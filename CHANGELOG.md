# Changelog

## 2.3-beta

- Added Paper Dialog UI support for `/mt check <player>` on supported Paper/26.x servers.
- Added dialog editing with separate tag and duration inputs.
- Added Back buttons for time dialogs.
- Added `commands.yml` for custom command aliases that route to the same MistTags actions.
- Switched normal game storage to SQLite by default and stopped writing `data.yml`.
- Added display-only spacing so prefixes do not merge into names and suffixes do not attach to names.
- Improved clickable `/mt list` rows and Adventure color handling.
- Fixed preview cleanup to last 5 seconds and clear reliably.
- Added safer scheduler cancellation for Paper/Folia task handles.

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

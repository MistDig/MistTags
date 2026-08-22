# Changelog

## 2.4.0-beta

- Added `groups.yml`: per-LuckPerms-group default prefixes/suffixes, resolved by MistTags itself (including `anim:<name>` animation references) instead of left as raw placeholder text. Fixes animated group tags showing up as literal unresolved placeholder syntax (e.g. `%animation:<name>%`) in chat, staff-chat, or any other plugin that does a single PlaceholderAPI pass -- they only ever rendered correctly in TAB's own tablist/nametag output, which re-parses TAB's animation syntax itself.
- `%misttags_display_prefix%`/`%misttags_display_suffix%` now check `groups.yml` before falling back to the raw `%luckperms_prefix%`/`%luckperms_suffix%` passthrough.
- Fixed `/mt addprefix`/`/mt addsuffix` tags not reliably overriding other chat-formatting plugins: `ChatListener` now runs at `HIGHEST` priority instead of the default, so an active MistTags tag wins the chat format instead of racing whichever plugin's listener happened to run last.
- Corrected the LuckPerms/TAB integration docs, which previously recommended pasting `%animation:<name>%` directly into a LuckPerms prefix meta value -- that only works for TAB's own rendering and silently breaks chat. Docs now recommend `groups.yml` for that use case.

## 2.3.4-beta

- Fixed broad staff permissions so `misttags.manage`, `misttags.admin`, and `misttags.*` work across commands, tab completion, and the manage GUI.
- Added `misttags.admin` and `misttags.*` permission declarations.
- Added `or_none` placeholders for scoreboards/menus so TAB nametags can stay empty while sidebars show red `None`.
- Added docs for using TAB animation placeholders, such as `%animation:sovereign%`, through LuckPerms fallback prefixes.

## 2.3.3-beta

- Added the Paper Dialog animation picker after staff edit a prefix or suffix.
- Made every animation preset in the docs use `{text}` so copied examples work as templates.
- Updated version metadata for the next beta build.
- Kept public release naming focused on `MistTags` for Modrinth compliance.

## 2.3.2-beta

- Added template animation support using `{text}` in `animations.yml` frames.
- Added command syntax such as `/mt addprefix MistDig 1m anim:gradient_chroma VIP`.
- Added default `gradient_chroma` template animation.

## 2.3.1-beta

- Fixed `/mt` help and tab completion so players only see commands they have permission to use.
- Fixed standalone command tab completion so self-service players only see their own name.

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

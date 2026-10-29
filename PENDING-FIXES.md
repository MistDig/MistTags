# Pending Fixes

## Automatic Standalone Display Without TAB

Status: Reported; cause not yet confirmed on the affected server.

- A user reported that tags only displayed after installing TAB. With `display.mode: auto` and no display/chat integration, MistTags should render tags itself without TAB or PlaceholderAPI.
- Investigate auto-detection: an installed plugin must not cause display to be handed off unless the relevant integration can actually render MistTags. Mere plugin presence is not sufficient evidence of a configured renderer.
- Investigate standalone nametags when another plugin assigns a separate scoreboard to viewers.
- Preserve explicit `standalone`, `tab`, and `placeholder-only` settings.
- Preserve working TAB/LPC integrations and LuckPerms fallback behavior. Do not overwrite unrelated configuration, prefixes, suffixes, scoreboard teams, objectives, or chat formatting.
- Keep changes scoped to the affected display surfaces; avoid enabling a second chat or nametag renderer over one already in use.
- Verify before release: plain server without TAB/PAPI; LuckPerms-only; working TAB/PAPI; LPC/PAPI; another scoreboard plugin; explicit display modes; tag removal/expiry; reload; animations; Paper/Folia scheduling where supported.
- Obtain the affected server's plugin list, display configuration, startup logs, and which surfaces failed (chat, tab list, or nametags) before declaring the cause fixed.

No runtime changes or release are included in this tracking item.

## Prefix Editing Through The Dialog

Status: Reported; source investigation requested.

- User reports bugs when typing a prefix in the editor opened through `/mt check`.
- Inspect text submission, formatting preservation, duration handling, animation selection (including None), and reopening the menu.
- Preserve permissions, expiry, suffix behavior, and fallback inventory handling.
- Exact input, expected output, and observed failure are still needed for a complete in-game reproduction.
- Source fixes prepared: None preserves formatting; editor validates animation IDs and normalizes input; permanent defaults stay permanent and remaining duration includes seconds; Save Permanent bypasses the duration field. Live dialog round-trip testing is pending. Preserving the exact original deadline while an editor remains open and duration overflow validation remain open items.

## MiniMessage In Preview

Status: Reported; source investigation requested.

- User reports that `/mt preview` does not render MiniMessage correctly.
- Trace sanitization and action-bar serialization; check named colors, hex colors, gradients, bold, spaces, and animation templates.
- Preserve the five-second preview duration and cleanup; do not change stored tags during preview.
- Verify supported formatting and reject interactive/non-cosmetic tags consistently with normal tags.
- Source fix prepared: Paper action bars use native Adventure components, and the sanitizer accepts cosmetic color aliases. Live gradient/formatting tests on 26.3 remain pending.

## Minecraft/Paper 26.3 Compatibility

- Local candidate version: 2.3.5-beta. Keep older server compatibility and the existing minimum api-version rather than making 26.3 mandatory.
- Paper 26.3 requires Java 25 for the server runtime. Java 17 bytecode remains the plugin build target.
- Official references: https://papermc.io/news/26-3/ and https://docs.papermc.io/paper/getting-started/
- A live 26.3 server test is required before claiming verified support. Server downloads are blocked by this session's network restrictions.
- After relocating the workspace to D:, Java 17 compilation and jar packaging passed. RenderingSmokeTest passed against cached Adventure 4.26.1 for gradients, color aliases, closed formatting scope, legacy decoration reset, and RGB serialization. This does not replace live server validation.
- Docs build remains blocked by unresolved vue/server-renderer in the relocated dependency tree. GitHub upload requires reauthentication of both the connected app and CLI.

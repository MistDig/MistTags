package com.mistdig.misttags;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unified front door for the plugin: /misttags (alias /mt) with subcommands, so staff don't
 * have to remember four separate command names. The standalone /addprefix, /addsuffix,
 * /removeprefix and /removesuffix commands still work exactly as before -- this just wraps
 * the same TagCommand logic behind one root command, plus adds /misttags reload, which has
 * no standalone equivalent.
 */
public class MistTagsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ADD_REMOVE_SUBCOMMANDS =
            List.of("addprefix", "addsuffix", "removeprefix", "removesuffix");
    private static final List<String> ROOT_SUBCOMMANDS =
            List.of("addprefix", "addsuffix", "removeprefix", "removesuffix", "list", "check", "stats", "preview", "reload");

    private final MistTags plugin;
    private final TagCommand tagCommand;

    public MistTagsCommand(MistTags plugin, TagCommand tagCommand) {
        this.plugin = plugin;
        this.tagCommand = tagCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] rest = Arrays.copyOfRange(args, 1, args.length);

        if (sub.equals("reload")) {
            return handleReload(sender);
        }
        if (sub.equals("list")) {
            return handleList(sender, rest);
        }
        if (sub.equals("check")) {
            return handleCheck(sender, rest);
        }
        if (sub.equals("stats")) {
            return handleStats(sender);
        }
        if (sub.equals("preview")) {
            return handlePreview(sender, rest);
        }
        if (ADD_REMOVE_SUBCOMMANDS.contains(sub)) {
            // Delegate straight into TagCommand with the subcommand as the synthetic label --
            // it's exactly what onCommand would have received had /addprefix etc. been used.
            return tagCommand.handle(sender, sub, rest);
        }

        sendHelp(sender);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("misttags.reload")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        try {
            plugin.reloadAll();
        } catch (Exception e) {
            plugin.messages().send(sender, "reload-failed", Map.of("error", String.valueOf(e.getMessage())));
            plugin.getLogger().severe("Error during /misttags reload: " + e);
            return true;
        }
        plugin.messages().send(sender, "reloaded");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        plugin.messages().send(sender, "help-title");
        if (sender.hasPermission("misttags.manage") || sender.hasPermission("misttags.custom")) {
            plugin.messages().send(sender, "help-addprefix");
            plugin.messages().send(sender, "help-addsuffix");
            plugin.messages().send(sender, "help-removeprefix");
            plugin.messages().send(sender, "help-removesuffix");
        }
        if (sender.hasPermission("misttags.reload")) {
            plugin.messages().send(sender, "help-reload");
        }
        plugin.messages().send(sender, "help-list");
        plugin.messages().send(sender, "help-check");
        plugin.messages().send(sender, "help-stats");
        plugin.messages().send(sender, "help-preview");
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("misttags.list")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length > 0) {
            String target = args[0];
            for (PlayerTagData data : plugin.getAllPlayerData()) {
                if (data.getName().equalsIgnoreCase(target)) {
                    sendListRow(sender, data, true);
                    return true;
                }
            }
            plugin.messages().send(sender, "list-missing", Map.of("player", target));
            return true;
        }

        int shown = 0;
        plugin.messages().send(sender, "list-header");
        for (PlayerTagData data : plugin.getAllPlayerData()) {
            if (data.isEmpty()) continue;
            shown++;
            sendListRow(sender, data, false);
            if (shown >= 25) {
                plugin.messages().send(sender, "list-more", Map.of("limit", "25"));
                break;
            }
        }
        if (shown == 0) plugin.messages().send(sender, "list-empty");
        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("misttags.check")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "check-player-only");
            return true;
        }
        if (args.length < 1) {
            plugin.messages().send(sender, "usage-check");
            return true;
        }
        PlayerTagData data = findData(args[0]);
        if (data == null) {
            plugin.messages().send(sender, "list-missing", Map.of("player", args[0]));
            return true;
        }
        plugin.getTagManageMenu().open(player, data);
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("misttags.stats")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        int activePrefixes = 0;
        int activeSuffixes = 0;
        int animatedPlayers = 0;
        for (PlayerTagData data : plugin.getAllPlayerData()) {
            boolean animated = false;
            if (data.getPrefix() != null) {
                activePrefixes++;
                animated |= data.getPrefix().toLowerCase().startsWith("anim:");
            }
            if (data.getSuffix() != null) {
                activeSuffixes++;
                animated |= data.getSuffix().toLowerCase().startsWith("anim:");
            }
            if (animated) animatedPlayers++;
        }
        long runningAnimations = plugin.getAnimations().values().stream()
                .filter(anim -> anim.getFrameCount() > 1)
                .count();
        plugin.messages().send(sender, "stats", Map.of(
                "active_prefixes", String.valueOf(activePrefixes),
                "active_suffixes", String.valueOf(activeSuffixes),
                "animations_loaded", String.valueOf(plugin.getAnimations().size()),
                "animations_running", String.valueOf(runningAnimations),
                "animated_players", String.valueOf(animatedPlayers)
        ));
        return true;
    }

    private boolean handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "preview-console-only");
            return true;
        }
        if (!sender.hasPermission("misttags.preview")) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }
        if (args.length < 1) {
            plugin.messages().send(sender, "usage-preview");
            return true;
        }
        String stored = MiniMessageSanitizer.toSafeString(String.join(" ", args));
        if (stored.toLowerCase().startsWith("anim:")) {
            String animId = stored.substring(5).toLowerCase();
            if (!plugin.getAnimations().containsKey(animId)) {
                plugin.messages().send(sender, "animation-missing", Map.of("animation", animId));
                return true;
            }
        }
        final String preview = stored;
        // Per-player action bar loop, so this must run on whichever region owns this
        // player rather than a bare main-thread timer -- see MistTagsScheduler.
        var task = plugin.getScheduler().runForPlayerTimer(player, () ->
                ActionBarUtil.send(player, plugin.renderStoredRaw(preview)), 0L, 20L);
        plugin.getScheduler().runGlobalDelayed(() -> {
            plugin.getScheduler().cancel(task);
            plugin.getScheduler().runForPlayer(player, () -> ActionBarUtil.send(player, ""));
        }, 20L * 5L);
        plugin.getLogger().info(sender.getName() + " previewed MistTag '" + stored + "'.");
        plugin.messages().send(sender, "preview-started");
        return true;
    }

    private PlayerTagData findData(String name) {
        for (PlayerTagData data : plugin.getAllPlayerData()) {
            if (data.getName() != null && data.getName().equalsIgnoreCase(name)) return data;
        }
        return null;
    }

    private void sendListRow(CommandSender sender, PlayerTagData data, boolean detailed) {
        String line = (detailed ? plugin.messages().prefix() + " " : "")
                + ChatColor.YELLOW + data.getName()
                + ChatColor.GRAY + " prefix=" + rendered(data.getPrefix())
                + ChatColor.GRAY + " suffix=" + rendered(data.getSuffix());

        if (!(sender instanceof Player player)) {
            sender.sendMessage(line);
            return;
        }

        ClickableMessageUtil.sendRunCommand(player, line, "/mt check " + data.getName(),
                ChatColor.YELLOW + "Click to manage " + data.getName() + "'s MistTags");
    }

    private String rendered(String value) {
        return value == null ? ChatColor.GRAY + "(none)" : MiniMessageSanitizer.toLegacy(plugin.renderStoredRaw(value));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(ROOT_SUBCOMMANDS);
            if (!sender.hasPermission("misttags.reload")) options.remove("reload");
            List<String> matches = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], options, matches);
            return matches;
        }

        String sub = args[0].toLowerCase();
        if ((sub.equals("list") || sub.equals("check")) && args.length == 2) return tagCommand.playerNameSuggestions(args[1]);
        if (sub.equals("preview") && args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("<Raw MiniMessage Text>");
            for (String animKey : plugin.getAnimations().keySet()) suggestions.add("anim:" + animKey);
            return suggestions;
        }
        if (!ADD_REMOVE_SUBCOMMANDS.contains(sub)) return List.of();

        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        return tagCommand.complete(sender, sub, rest);
    }
}

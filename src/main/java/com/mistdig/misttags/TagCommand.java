package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TagCommand implements CommandExecutor, TabCompleter {

    private final MistTags plugin;

    public TagCommand(MistTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return handle(sender, label, args);
    }

    /**
     * Actual command logic, split out from onCommand so that MistTagsCommand (the /misttags
     * and /mt parent command) can dispatch straight into it with a synthetic label
     * ("addprefix", "removesuffix", etc.) taken from its first argument, rather than
     * duplicating all of this behind a second code path.
     */
    public boolean handle(CommandSender sender, String label, String[] args) {
        String lower = label.toLowerCase();

        if (args.length < 1) {
            plugin.messages().send(sender, "usage-player", Map.of("label", label));
            return true;
        }
        if (!isAuthorized(sender, lower, args[0])) {
            plugin.messages().send(sender, "no-permission");
            return true;
        }

        if (lower.equals("removeprefix") || lower.equals("removesuffix")) {
            return handleRemove(sender, lower, args);
        }
        return handleAdd(sender, lower, args);
    }

    /**
     * Two ways to be authorized for a given command against a given target:
     *  - hold the matching misttags.manage.* node, which works against any target, or
     *  - hold misttags.custom and be targeting your own name (self-service only).
     */
    private boolean isAuthorized(CommandSender sender, String label, String targetName) {
        if (sender.hasPermission(managePermissionFor(label))) return true;

        return sender.hasPermission("misttags.custom")
                && sender instanceof Player player
                && player.getName().equalsIgnoreCase(targetName);
    }

    private String managePermissionFor(String label) {
        return switch (label) {
            case "addprefix" -> "misttags.manage.addprefix";
            case "addsuffix" -> "misttags.manage.addsuffix";
            case "removeprefix" -> "misttags.manage.removeprefix";
            case "removesuffix" -> "misttags.manage.removesuffix";
            default -> "misttags.manage";
        };
    }

    private boolean handleAdd(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            plugin.messages().send(sender, "usage-add", Map.of("label", label));
            return true;
        }

        String targetName = args[0];
        String durationStr = args[1];
        String tagValue = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        boolean isAnimation = tagValue.toLowerCase().startsWith("anim:");
        String type = label.equalsIgnoreCase("addprefix") ? "prefix" : "suffix";

        // Self-service: a player using misttags.custom on themselves rather than staff acting
        // through a misttags.manage.* node. isAuthorized() already confirmed one of these two
        // paths applies before handleAdd was ever called, so "not manage" here means "custom".
        boolean selfService = sender instanceof Player
                && !sender.hasPermission(managePermissionFor(label));
        Player selfPlayer = selfService ? (Player) sender : null;

        if (isAnimation) {
            if (selfService) {
                plugin.messages().send(sender, "self-animated-denied");
                return true;
            }
            String animId = tagValue.substring(5).toLowerCase();
            if (!plugin.getAnimations().containsKey(animId)) {
                plugin.messages().send(sender, "animation-missing", Map.of("animation", animId));
                return true;
            }
        } else {
            // Strip any non-cosmetic MiniMessage tags (click/hover/insertion, etc.) up front
            // so the sender gets immediate feedback rather than a silently-modified value later.
            tagValue = MiniMessageSanitizer.toSafeString(tagValue);
        }

        CustomTagPolicy policy = plugin.getCustomTagPolicy();

        if (selfService) {
            PlayerTagData existing = plugin.getData(selfPlayer.getUniqueId());
            long lastChange = existing == null ? 0
                    : (type.equals("prefix") ? existing.getLastCustomPrefixChange() : existing.getLastCustomSuffixChange());
            long remainingMs = lastChange + (policy.getCooldownSeconds() * 1000L) - System.currentTimeMillis();
            if (remainingMs > 0) {
                plugin.messages().send(sender, "cooldown", Map.of("type", type, "time", formatRemaining(remainingMs)));
                return true;
            }

            String rejection = policy.validate(tagValue);
            if (rejection != null) {
                sender.sendMessage(rejection);
                return true;
            }
        }

        boolean permanent = isPermanentDuration(durationStr);
        if (permanent && selfService) {
            plugin.messages().send(sender, "self-permanent-denied");
            return true;
        }

        Duration duration = permanent ? Duration.ZERO : DurationParser.parse(durationStr);
        if (!permanent && (duration == null || duration.isZero() || duration.isNegative())) {
            plugin.messages().send(sender, "invalid-duration");
            return true;
        }

        String cappedNotice = null;
        if (selfService && !permanent) {
            Duration maxAllowed = policy.resolveMaxDuration(selfPlayer);
            if (duration.compareTo(maxAllowed) > 0) {
                duration = maxAllowed;
                cappedNotice = "Capped to the maximum duration you're allowed to set yourself: "
                        + DurationParser.describe(maxAllowed) + ".";
            }
        }

        long expireAt = permanent ? 0 : System.currentTimeMillis() + duration.toMillis();
        String finalTagValue = tagValue;
        Duration finalDuration = duration;
        String finalCappedNotice = cappedNotice;
        String durationText = permanent ? "permanent" : DurationParser.describe(finalDuration);

        resolveTarget(sender, targetName, target -> {
            PlayerTagData data = plugin.getOrCreate(target.getUniqueId(), targetName);
            if (data.getLastSeen() == 0) data.setLastSeen(System.currentTimeMillis());
            if (type.equals("prefix")) {
                data.setPrefix(finalTagValue, expireAt);
                if (selfService) data.setLastCustomPrefixChange(System.currentTimeMillis());
            } else {
                data.setSuffix(finalTagValue, expireAt);
                if (selfService) data.setLastCustomSuffixChange(System.currentTimeMillis());
            }
            plugin.markDirty();
            if (plugin.isStandaloneDisplayActive()) {
                plugin.getDisplayManager().refresh(target.getUniqueId());
            } else if (plugin.getDisplayManager() != null) {
                plugin.getDisplayManager().refresh(target.getUniqueId());
            }
            plugin.getLogger().info(sender.getName() + " set " + type + " for " + targetName
                    + " to '" + finalTagValue + "' for " + durationText + ".");
            if (finalCappedNotice != null) {
                plugin.messages().send(sender, "capped-duration", Map.of("duration", finalCappedNotice));
            }
            plugin.messages().send(sender, "set-tag", Map.of(
                    "type", type,
                    "player", targetName,
                    "expiry", permanent ? "permanent" : "expires in " + durationText
            ));
        });

        return true;
    }

    /** Short "Xm Ys" style formatting for cooldown remainders, which are usually well under a day. */
    private String formatRemaining(long millis) {
        long totalSeconds = Math.max(1, (millis + 999) / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes > 0 ? (minutes + "m " + seconds + "s") : (seconds + "s");
    }

    private boolean isPermanentDuration(String value) {
        return value.equalsIgnoreCase("perm")
                || value.equalsIgnoreCase("permanent")
                || value.equalsIgnoreCase("forever")
                || value.equalsIgnoreCase("never");
    }

    private boolean handleRemove(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            plugin.messages().send(sender, "usage-remove", Map.of("label", label));
            return true;
        }
        String targetName = args[0];
        String type = label.equalsIgnoreCase("removeprefix") ? "prefix" : "suffix";

        resolveTarget(sender, targetName, target -> {
            PlayerTagData data = plugin.getData(target.getUniqueId());
            boolean hasTag = data != null && (type.equals("prefix") ? data.getPrefix() : data.getSuffix()) != null;
            if (!hasTag) {
                plugin.messages().send(sender, "no-active-tag", Map.of("player", targetName, "type", type));
                return;
            }
            if (type.equals("prefix")) data.clearPrefix(); else data.clearSuffix();
            plugin.markDirty();
            if (plugin.getDisplayManager() != null) {
                plugin.getDisplayManager().refresh(target.getUniqueId());
            }
            plugin.getLogger().info(sender.getName() + " removed " + type + " from " + targetName + ".");
            plugin.messages().send(sender, "removed-tag", Map.of("player", targetName, "type", type));
        });
        return true;
    }

    /**
     * Resolves a target without ever blocking the calling thread on a Mojang lookup.
     * Online players resolve instantly; offline players are resolved on an async task,
     * which is the only thread it's safe to call the name-based OfflinePlayer lookup from.
     */
    private void resolveTarget(CommandSender sender, String targetName, Consumer<OfflinePlayer> onResolved) {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            onResolved.accept(online);
            return;
        }

        plugin.messages().send(sender, "offline-lookup");
        plugin.getScheduler().runAsyncNow(() -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            boolean known = target.hasPlayedBefore() || target.getName() != null;
            // Resume on whichever region is safe for this sender (their own player-region,
            // or the global region for console) instead of a bare main-thread runTask --
            // see MistTagsScheduler for why that distinction matters on Folia.
            plugin.getScheduler().runForSender(sender, () -> {
                if (!known) {
                    plugin.messages().send(sender, "unknown-player", Map.of("player", targetName));
                    return;
                }
                onResolved.accept(target);
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return complete(sender, alias, args);
    }

    /** Split out for the same reason as handle() above -- lets MistTagsCommand reuse this. */
    public List<String> complete(CommandSender sender, String alias, String[] args) {
        String lower = alias.toLowerCase();
        if (!canUseCommand(sender, lower)) return List.of();

        if (lower.startsWith("remove")) {
            if (args.length == 1) {
                return targetSuggestions(sender, lower, args[0]);
            }
            return List.of();
        }

        if (args.length == 1) return targetSuggestions(sender, lower, args[0]);
        if (args.length == 2) return Arrays.asList("30m", "1h", "12h", "1d", "7d", "permanent");
        if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("<Raw MiniMessage Text>");
            for (String animKey : plugin.getAnimations().keySet()) {
                suggestions.add("anim:" + animKey);
            }
            return suggestions;
        }
        return List.of();
    }

    private boolean canUseCommand(CommandSender sender, String label) {
        return sender.hasPermission(managePermissionFor(label)) || sender.hasPermission("misttags.custom");
    }

    private List<String> targetSuggestions(CommandSender sender, String label, String prefix) {
        if (sender.hasPermission(managePermissionFor(label))) return playerNameSuggestions(prefix);
        if (sender instanceof Player player && sender.hasPermission("misttags.custom")
                && player.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
            return List.of(player.getName());
        }
        return List.of();
    }

    List<String> playerNameSuggestions(String prefix) {
        List<String> names = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(lower)) names.add(p.getName());
        }
        for (PlayerTagData data : plugin.getAllPlayerData()) {
            String name = data.getName();
            if (name != null && name.toLowerCase().startsWith(lower) && !names.contains(name)) names.add(name);
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String name = player.getName();
            if (name != null && name.toLowerCase().startsWith(lower) && !names.contains(name)) names.add(name);
            if (names.size() >= 80) break;
        }
        return names;
    }
}

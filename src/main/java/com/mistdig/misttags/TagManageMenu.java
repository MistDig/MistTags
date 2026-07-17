package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TagManageMenu implements Listener {

    private final MistTags plugin;
    private final Map<UUID, MenuTarget> openMenus = new ConcurrentHashMap<>();

    public TagManageMenu(MistTags plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, PlayerTagData targetData) {
        if (PaperDialogUtil.showManageDialog(plugin, viewer, targetData)) return;

        MenuTarget target = new MenuTarget(targetData.getUuid(), targetData.getName());
        Inventory inventory = Bukkit.createInventory(new Holder(target), 27,
                ChatColor.DARK_AQUA + "MistTags: " + target.name());

        fill(inventory, targetData);
        openMenus.put(viewer.getUniqueId(), target);
        viewer.openInventory(inventory);
    }

    private void fill(Inventory inventory, PlayerTagData data) {
        inventory.setItem(4, item(Material.NAME_TAG, ChatColor.AQUA + data.getName(),
                List.of(ChatColor.GRAY + "Manage this player's active MistTags.")));

        inventory.setItem(10, tagInfo(Material.PAPER, ChatColor.GREEN + "Prefix", data.getPrefix(), data.getPrefixExpire()));
        inventory.setItem(11, button(Material.BARRIER, ChatColor.RED + "Delete Prefix",
                "Remove this player's active MistTags prefix."));
        inventory.setItem(12, button(Material.WRITABLE_BOOK, ChatColor.YELLOW + "Edit Prefix",
                "Click to paste the edit command into chat."));
        inventory.setItem(13, button(Material.CLOCK, ChatColor.GOLD + "Prefix Time Left",
                timeLeft(data.getPrefix(), data.getPrefixExpire())));

        inventory.setItem(15, tagInfo(Material.PAPER, ChatColor.GREEN + "Suffix", data.getSuffix(), data.getSuffixExpire()));
        inventory.setItem(16, button(Material.BARRIER, ChatColor.RED + "Delete Suffix",
                "Remove this player's active MistTags suffix."));
        inventory.setItem(17, button(Material.WRITABLE_BOOK, ChatColor.YELLOW + "Edit Suffix",
                "Click to paste the edit command into chat."));
        inventory.setItem(22, button(Material.CLOCK, ChatColor.GOLD + "Suffix Time Left",
                timeLeft(data.getSuffix(), data.getSuffixExpire())));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Holder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("misttags.manage")) {
            plugin.messages().send(player, "no-permission");
            return;
        }

        PlayerTagData data = plugin.getData(holder.target.uuid());
        if (data == null) {
            plugin.messages().send(player, "list-missing", Map.of("player", holder.target.name()));
            player.closeInventory();
            return;
        }

        switch (event.getRawSlot()) {
            case 11 -> deleteTag(player, data, "prefix");
            case 12 -> suggestEdit(player, data, "prefix");
            case 13 -> showTime(player, data, "prefix");
            case 16 -> deleteTag(player, data, "suffix");
            case 17 -> suggestEdit(player, data, "suffix");
            case 22 -> showTime(player, data, "suffix");
            default -> {
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openMenus.remove(event.getPlayer().getUniqueId());
    }

    private void deleteTag(Player player, PlayerTagData data, String type) {
        boolean hadTag = "prefix".equals(type) ? data.getPrefix() != null : data.getSuffix() != null;
        if (!hadTag) {
            plugin.messages().send(player, "no-active-tag", Map.of("player", data.getName(), "type", type));
            return;
        }
        if ("prefix".equals(type)) data.clearPrefix(); else data.clearSuffix();
        plugin.markDirty();
        refreshDisplay(data);
        plugin.messages().send(player, "removed-tag", Map.of("player", data.getName(), "type", type));
        player.closeInventory();
    }

    private void suggestEdit(Player player, PlayerTagData data, String type) {
        String command = "/mt add" + type + " " + data.getName() + " 30m ";
        player.closeInventory();
        ClickableMessageUtil.sendSuggestCommand(player, plugin.messages().prefix() + ChatColor.YELLOW
                + "Click here to edit " + data.getName() + "'s " + type + ".", command);
    }

    private void showTime(Player player, PlayerTagData data, String type) {
        String stored = "prefix".equals(type) ? data.getPrefix() : data.getSuffix();
        long expireAt = "prefix".equals(type) ? data.getPrefixExpire() : data.getSuffixExpire();
        plugin.messages().send(player, "check-time", Map.of(
                "player", data.getName(),
                "type", type,
                "time", timeLeft(stored, expireAt)
        ));
    }

    private ItemStack tagInfo(Material material, String name, String stored, long expireAt) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Rendered: " + renderLegacy(stored));
        lore.add(ChatColor.GRAY + "Raw: " + (stored == null ? "(none)" : stored));
        lore.add(ChatColor.GRAY + "Time: " + timeLeft(stored, expireAt));
        return item(material, name, lore);
    }

    private ItemStack button(Material material, String name, String description) {
        return item(material, name, List.of(ChatColor.GRAY + description));
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String renderLegacy(String stored) {
        return stored == null ? ChatColor.GRAY + "(none)" : MiniMessageSanitizer.toLegacy(plugin.renderStoredRaw(stored));
    }

    private String timeLeft(String stored, long expireAt) {
        if (stored == null) return "no active tag";
        if (expireAt <= 0) return "permanent";
        long millis = expireAt - System.currentTimeMillis();
        if (millis <= 0) return "expired";
        return DurationParser.describe(Duration.ofMillis(millis));
    }

    private void refreshDisplay(PlayerTagData data) {
        if (plugin.getDisplayManager() != null) {
            plugin.getDisplayManager().refresh(data.getUuid());
        }
    }

    private record MenuTarget(UUID uuid, String name) {
    }

    private record Holder(MenuTarget target) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}

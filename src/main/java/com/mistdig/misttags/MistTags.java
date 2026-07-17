package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MistTags extends JavaPlugin implements Listener {

    private final Map<String, AnimationData> animations = new ConcurrentHashMap<>();

    // Single source of truth for prefix/suffix state. PlaceholderAPI/TAB read from this;
    // nothing but MistTags' own main-thread expiry sweep ever mutates it based on time,
    // which is what makes it safe for other plugins to query from arbitrary threads.
    private final Map<UUID, PlayerTagData> playerCache = new ConcurrentHashMap<>();

    private File dataFile;
    private File activityFile;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final Object storageLock = new Object();
    private boolean databaseEnabled;
    private Connection databaseConnection;

    private DisplayManager displayManager;
    private DisplayMode displayMode = DisplayMode.STANDALONE;
    private CustomTagPolicy customTagPolicy;
    private MistTagsScheduler scheduler;
    private MessageManager messageManager;
    private TagManageMenu tagManageMenu;

    // Plugins that count as "already owning" prefix/suffix display when display.mode is
    // "auto". Overridable via display.known-display-plugins in config.yml.
    private static final List<String> DEFAULT_KNOWN_DISPLAY_PLUGINS = List.of(
            "TAB", "LuckPerms", "NametagEdit", "Essentials", "DeluxeTags", "UltraPrefixes", "TitleManager");

    private enum DisplayMode { STANDALONE, TAB, PLACEHOLDER_ONLY }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createAnimationConfig();
        createPlaceholderList();
        messageManager = new MessageManager(this);
        validateStartupConfigs();
        loadAnimations();
        loadDataConfig();
        customTagPolicy = new CustomTagPolicy(this);
        scheduler = new MistTagsScheduler(this);
        tagManageMenu = new TagManageMenu(this);
        activityFile = new File(getDataFolder(), "active-tags.txt");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(tagManageMenu, this);

        boolean tabPresent = Bukkit.getPluginManager().getPlugin("TAB") != null;
        boolean placeholderApiPresent = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        displayMode = resolveDisplayMode(tabPresent);

        if (placeholderApiPresent) {
            new TagExpansion(this).register();
            new IntegrationAutoConfigurator(this).configureDetectedIntegrations();
        } else if (displayMode == DisplayMode.PLACEHOLDER_ONLY) {
            getLogger().warning("PlaceholderAPI isn't installed and display mode resolved to "
                    + "'placeholder-only'. Nothing will render prefixes/suffixes until you either "
                    + "install PlaceholderAPI or set display.mode: standalone in config.yml.");
        }

        if (displayMode == DisplayMode.STANDALONE || displayMode == DisplayMode.TAB) {
            displayManager = new DisplayManager(this);
            displayManager.enable();
            getLogger().info(displayMode == DisplayMode.TAB
                    ? "TAB display active: MistTags will push tags into TAB automatically."
                    : "Standalone display active: MistTags is rendering tags itself (chat + tab list/nametags).");
        } else {
            getLogger().info("Placeholder-only mode active: expose %misttags_prefix%/"
                    + "%misttags_suffix% for TAB or another display plugin to render.");
        }

        TagCommand commandHandler = new TagCommand(this);
        for (String cmd : List.of("addprefix", "addsuffix", "removeprefix", "removesuffix")) {
            getCommand(cmd).setExecutor(commandHandler);
            getCommand(cmd).setTabCompleter(commandHandler);
        }

        // Unified /misttags (alias /mt) front door: same TagCommand logic underneath,
        // reached via a synthetic label instead of the standalone commands above, plus
        // /misttags reload which the standalone commands don't have an equivalent of.
        MistTagsCommand mistTagsCommand = new MistTagsCommand(this, commandHandler);
        getCommand("misttags").setExecutor(mistTagsCommand);
        getCommand("misttags").setTabCompleter(mistTagsCommand);

        // Central animation clock: ticks every server tick (50ms) so update-ticks in
        // animations.yml stays accurate regardless of how many animations are registered.
        // Uses the global region scheduler (MistTagsScheduler), not a bare
        // Bukkit.getScheduler() timer, so this works correctly on Folia as well as on
        // regular Paper/Purpur -- see MistTagsScheduler's class doc for why that matters.
        scheduler.runGlobalTimer(() -> {
            boolean anyChanged = false;
            for (AnimationData anim : animations.values()) {
                if (anim.tick()) anyChanged = true;
            }
            if (anyChanged && displayManager != null) {
                displayManager.refreshAnimatedPlayers();
            }
        }, 1L, 1L);

        // Active expiry sweep instead of the old lazy "only clears when someone requests
        // that specific placeholder" approach. Runs on the main thread only, once a second,
        // so it never races with async placeholder reads.
        scheduler.runGlobalTimer(this::sweepExpiredTags, 20L, 20L);

        // Debounced save: batches writes instead of hitting disk on every single command.
        int saveIntervalTicks = Math.max(20, getConfig().getInt("save-interval-seconds", 10) * 20);
        scheduler.runAsyncTimer(this::flushIfDirty, saveIntervalTicks, saveIntervalTicks);
        scheduler.runAsyncTimer(this::writeActivityFile, 40L, 20L * 60L * 30L);
        getLogger().info("MistTags enabled with " + playerCache.size() + " cached player records.");
    }

    @Override
    public void onDisable() {
        if (displayManager != null) displayManager.disable();
        flushIfDirty(); // synchronous final save so nothing is lost on shutdown/restart
        writeActivityFile();
        closeDatabase();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerTagData data = getOrCreate(player.getUniqueId(), player.getName());
        data.setLastSeen(System.currentTimeMillis());
        markDirty();
    }

    // ----- Animation registry -----

    public Map<String, AnimationData> getAnimations() { return animations; }

    private void createAnimationConfig() {
        File file = new File(getDataFolder(), "animations.yml");
        if (!file.exists()) saveResource("animations.yml", false);
    }

    private void createPlaceholderList() {
        File file = new File(getDataFolder(), "placeholders.txt");
        if (!file.exists()) saveResource("placeholders.txt", false);
    }

    public void loadAnimations() {
        animations.clear();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "animations.yml"));
        if (!cfg.contains("animations")) return;
        for (String key : cfg.getConfigurationSection("animations").getKeys(false)) {
            int ticks = Math.max(1, cfg.getInt("animations." + key + ".update-ticks", 10));
            List<String> frames = cfg.getStringList("animations." + key + ".frames");
            if (frames.isEmpty()) {
                getLogger().warning("animations.yml animation '" + key + "' has no frames and was skipped.");
                continue;
            }
            animations.put(key.toLowerCase(), new AnimationData(frames, ticks));
        }
        getLogger().info("Loaded " + animations.size() + " animations.");
    }

    // ----- Player tag cache -----

    public PlayerTagData getOrCreate(UUID uuid, String name) {
        return playerCache.compute(uuid, (id, existing) -> {
            if (existing == null) return new PlayerTagData(id, name);
            existing.setName(name);
            return existing;
        });
    }

    public PlayerTagData getData(UUID uuid) {
        return playerCache.get(uuid);
    }

    public void markDirty() {
        dirty.set(true);
    }

    public List<PlayerTagData> getAllPlayerData() {
        return List.copyOf(playerCache.values());
    }

    public String renderStoredRaw(String stored) {
        if (stored == null) return "";
        if (stored.toLowerCase().startsWith("anim:")) {
            AnimationData anim = animations.get(stored.substring(5).toLowerCase());
            return anim == null ? "" : anim.getCurrentFrame();
        }
        return stored;
    }

    private void sweepExpiredTags() {
        boolean changed = false;
        for (PlayerTagData data : playerCache.values()) {
            if (data.isPrefixExpired()) { data.clearPrefix(); changed = true; }
            if (data.isSuffixExpired()) { data.clearSuffix(); changed = true; }
        }
        if (changed) {
            markDirty();
            if (displayManager != null) displayManager.refreshAll();
        }
    }

    public DisplayManager getDisplayManager() { return displayManager; }
    public boolean isStandaloneDisplayActive() { return displayMode == DisplayMode.STANDALONE; }
    public boolean isTabDisplayActive() { return displayMode == DisplayMode.TAB; }
    public String getDisplayModeName() { return displayMode.name().toLowerCase().replace('_', '-'); }
    public boolean isDatabaseEnabled() { return databaseEnabled; }
    public CustomTagPolicy getCustomTagPolicy() { return customTagPolicy; }
    public MistTagsScheduler getScheduler() { return scheduler; }
    public MessageManager messages() { return messageManager; }
    public TagManageMenu getTagManageMenu() { return tagManageMenu; }

    /**
     * Backs /misttags reload. Reloads config.yml and animations.yml and re-derives
     * everything sourced from them (duration tiers/cooldown/policy, display mode) without
     * a server restart. Deliberately does NOT touch playerCache/data.yml -- active tags
     * should survive a reload untouched.
     *
     * save-interval-seconds is the one config value this can't apply live, since the async
     * flush task's period was already baked in as a fixed scheduler interval back in
     * onEnable(); changing it here would require cancelling and rescheduling that task,
     * which isn't worth the complexity for a value nobody tweaks at runtime. A log line
     * below tells the reloading admin as much instead of silently ignoring it.
     */
    public void reloadAll() {
        int oldSaveInterval = getConfig().getInt("save-interval-seconds", 10);
        reloadConfig();
        messageManager.reload();
        createAnimationConfig();
        createPlaceholderList();
        loadAnimations();
        customTagPolicy = new CustomTagPolicy(this);

        boolean tabPresent = Bukkit.getPluginManager().getPlugin("TAB") != null;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new IntegrationAutoConfigurator(this).configureDetectedIntegrations();
        }
        DisplayMode newDisplayMode = resolveDisplayMode(tabPresent);

        if (newDisplayMode != displayMode) {
            if (newDisplayMode == DisplayMode.STANDALONE || newDisplayMode == DisplayMode.TAB) {
                displayManager = new DisplayManager(this);
                displayManager.enable();
                getLogger().info("Reload: display.mode now resolves to " + newDisplayMode.name().toLowerCase() + ".");
            } else if (displayManager != null) {
                displayManager.disable();
                displayManager = null;
                getLogger().info("Reload: display.mode now resolves to placeholder-only -- handing rendering off.");
            }
            displayMode = newDisplayMode;
        } else if (displayManager != null) {
            // Same mode, but re-render everyone in case colors/format-relevant config changed.
            displayManager.refreshAll();
        }

        int newSaveInterval = getConfig().getInt("save-interval-seconds", 10);
        if (newSaveInterval != oldSaveInterval) {
            getLogger().warning("save-interval-seconds changed in config.yml, but this requires " +
                    "a full server restart to take effect (the save task's schedule is fixed at startup).");
        }
    }

    // ----- Persistence -----

    private void loadDataConfig() {
        databaseEnabled = getConfig().getBoolean("database.enabled", false);
        if (databaseEnabled) {
            loadDatabase();
            return;
        }
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to create data.yml: " + e.getMessage());
            }
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (!cfg.contains("players")) return;
        for (String uuidStr : cfg.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String path = "players." + uuidStr + ".";
                String name = cfg.getString(path + "name", "Unknown");
                PlayerTagData data = new PlayerTagData(uuid, name);
                if (cfg.contains(path + "prefix")) {
                    data.setPrefix(cfg.getString(path + "prefix"), cfg.getLong(path + "prefix-expire", 0));
                }
                if (cfg.contains(path + "suffix")) {
                    data.setSuffix(cfg.getString(path + "suffix"), cfg.getLong(path + "suffix-expire", 0));
                }
                data.setLastCustomPrefixChange(cfg.getLong(path + "last-custom-prefix-change", 0));
                data.setLastCustomSuffixChange(cfg.getLong(path + "last-custom-suffix-change", 0));
                data.setLastSeen(cfg.getLong(path + "last-seen", 0));
                playerCache.put(uuid, data);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Skipping malformed UUID key in data.yml: " + uuidStr);
            }
        }
    }

    /**
     * Builds a fresh YamlConfiguration from the in-memory cache and writes it to disk.
     * Building the snapshot only touches plain data (no Bukkit API calls), which is what
     * makes it safe to run from the async scheduler thread as well as from onDisable().
     */
    private void flushIfDirty() {
        if (!dirty.compareAndSet(true, false)) return;
        if (databaseEnabled) {
            saveDatabase();
            writeActivityFile();
            return;
        }

        YamlConfiguration cfg = new YamlConfiguration();
        for (PlayerTagData data : playerCache.values()) {
            boolean hasCooldownToTrack = data.getLastCustomPrefixChange() > 0 || data.getLastCustomSuffixChange() > 0;
            // Still skip players with no active tags AND no self-service cooldown to remember,
            // to avoid bloating the file -- but a self-service cooldown must survive even
            // after the tag it came from expires/gets removed, or a restart would let a
            // player bypass custom.cooldown-seconds by waiting for expiry then re-logging.
            if (data.isEmpty() && !hasCooldownToTrack) continue;
            String path = "players." + data.getUuid() + ".";
            cfg.set(path + "name", data.getName());
            if (data.getPrefix() != null) {
                cfg.set(path + "prefix", data.getPrefix());
                cfg.set(path + "prefix-expire", data.getPrefixExpire());
            }
            if (data.getSuffix() != null) {
                cfg.set(path + "suffix", data.getSuffix());
                cfg.set(path + "suffix-expire", data.getSuffixExpire());
            }
            if (data.getLastCustomPrefixChange() > 0) {
                cfg.set(path + "last-custom-prefix-change", data.getLastCustomPrefixChange());
            }
            if (data.getLastCustomSuffixChange() > 0) {
                cfg.set(path + "last-custom-suffix-change", data.getLastCustomSuffixChange());
            }
            if (data.getLastSeen() > 0) {
                cfg.set(path + "last-seen", data.getLastSeen());
            }
        }

        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data.yml: " + e.getMessage());
            dirty.set(true); // retry on the next cycle instead of silently losing the write
        }
        writeActivityFile();
    }

    /**
     * Auto prefers smart PlaceholderAPI handoff when TAB or LPC is installed, because the
     * auto-configurator can wire %misttags_display_prefix%/%misttags_display_suffix% into
     * those plugins. The smart placeholders show MistTags first and fall back to LuckPerms
     * when a player has no MistTags tag. If no supported renderer is found, MistTags uses
     * standalone mode so chat/tab/nametags still work on a plain Bukkit-family server.
     */
    private DisplayMode resolveDisplayMode(boolean tabPresent) {
        String mode = getConfig().getString("display.mode", "auto").toLowerCase();
        return switch (mode) {
            case "standalone" -> DisplayMode.STANDALONE;
            case "tab" -> tabPresent ? DisplayMode.TAB : DisplayMode.STANDALONE;
            case "placeholder-only" -> DisplayMode.PLACEHOLDER_ONLY;
            default -> shouldUseSmartPlaceholderHandoff(tabPresent)
                    || (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && anyKnownDisplayPluginPresent())
                    ? DisplayMode.PLACEHOLDER_ONLY : DisplayMode.STANDALONE;
        };
    }

    private boolean shouldUseSmartPlaceholderHandoff(boolean tabPresent) {
        if (!getConfig().getBoolean("integrations.auto-configure", true)) return false;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return false;
        return tabPresent || Bukkit.getPluginManager().getPlugin("LPC") != null;
    }

    /**
     * True if any plugin MistTags recognizes as already owning prefix/suffix display is
     * installed (TAB, LuckPerms, NametagEdit, EssentialsX, etc. -- see
     * display.known-display-plugins in config.yml to customize the list). Used only by
     * "auto" display.mode resolution above; explicit modes always do exactly what the admin
     * asked for regardless of what else is installed.
     */
    private boolean anyKnownDisplayPluginPresent() {
        List<String> known = getConfig().getStringList("display.known-display-plugins");
        if (known.isEmpty()) known = DEFAULT_KNOWN_DISPLAY_PLUGINS;
        for (String name : known) {
            if ("LuckPerms".equalsIgnoreCase(name)) continue;
            if (Bukkit.getPluginManager().getPlugin(name) != null) return true;
        }
        return false;
    }

    private void validateStartupConfigs() {
        validateSection("config.yml", getConfig(), List.of("display", "custom"));
        File animationsFile = new File(getDataFolder(), "animations.yml");
        FileConfiguration animationsCfg = YamlConfiguration.loadConfiguration(animationsFile);
        validateSection("animations.yml", animationsCfg, List.of("animations"));
    }

    private void validateSection(String fileName, FileConfiguration cfg, List<String> requiredSections) {
        for (String section : requiredSections) {
            if (!cfg.isConfigurationSection(section)) {
                getLogger().warning(fileName + " is missing section '" + section + "'. Defaults may be used, but check formatting.");
            }
        }
    }

    private void loadDatabase() {
        try {
            getDataFolder().mkdirs();
            String defaultUrl = "jdbc:sqlite:" + new File(getDataFolder(), "misttags.db").getAbsolutePath();
            String url = getConfig().getString("database.url", defaultUrl);
            if (url == null || url.isBlank()) url = defaultUrl;
            String user = getConfig().getString("database.username", "");
            String password = getConfig().getString("database.password", "");
            loadJdbcDriver(url);
            databaseConnection = user.isEmpty() ? DriverManager.getConnection(url) : DriverManager.getConnection(url, user, password);
            try (Statement st = databaseConnection.createStatement()) {
                st.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS misttags_players (
                          uuid VARCHAR(36) PRIMARY KEY,
                          name VARCHAR(64) NOT NULL,
                          prefix TEXT,
                          prefix_expire BIGINT NOT NULL DEFAULT 0,
                          suffix TEXT,
                          suffix_expire BIGINT NOT NULL DEFAULT 0,
                          last_custom_prefix_change BIGINT NOT NULL DEFAULT 0,
                          last_custom_suffix_change BIGINT NOT NULL DEFAULT 0,
                          last_seen BIGINT NOT NULL DEFAULT 0
                        )
                        """);
            }
            try (Statement st = databaseConnection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM misttags_players")) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    PlayerTagData data = new PlayerTagData(uuid, rs.getString("name"));
                    String prefix = rs.getString("prefix");
                    if (prefix != null) data.setPrefix(prefix, rs.getLong("prefix_expire"));
                    String suffix = rs.getString("suffix");
                    if (suffix != null) data.setSuffix(suffix, rs.getLong("suffix_expire"));
                    data.setLastCustomPrefixChange(rs.getLong("last_custom_prefix_change"));
                    data.setLastCustomSuffixChange(rs.getLong("last_custom_suffix_change"));
                    data.setLastSeen(rs.getLong("last_seen"));
                    playerCache.put(uuid, data);
                }
            }
            getLogger().info("Database storage enabled. data.yml will be ignored.");
        } catch (SQLException | IllegalArgumentException e) {
            getLogger().severe("Database startup failed: " + e.getMessage());
            getLogger().severe("MistTags will keep running in memory for this session, but data.yml is still ignored because database.enabled is true.");
        }
    }

    private void loadJdbcDriver(String url) {
        try {
            if (url.startsWith("jdbc:sqlite:")) {
                Class.forName("org.sqlite.JDBC");
            } else if (url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mariadb:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            getLogger().warning("Could not load JDBC driver for " + url + ": " + e.getMessage());
        }
    }

    private void saveDatabase() {
        if (databaseConnection == null) return;
        synchronized (storageLock) {
            try {
                databaseConnection.setAutoCommit(false);
                try (Statement delete = databaseConnection.createStatement()) {
                    delete.executeUpdate("DELETE FROM misttags_players");
                }
                try (PreparedStatement ps = databaseConnection.prepareStatement("""
                        INSERT INTO misttags_players
                        (uuid, name, prefix, prefix_expire, suffix, suffix_expire, last_custom_prefix_change, last_custom_suffix_change, last_seen)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)) {
                    for (PlayerTagData data : playerCache.values()) {
                        boolean hasCooldownToTrack = data.getLastCustomPrefixChange() > 0 || data.getLastCustomSuffixChange() > 0;
                        if (data.isEmpty() && !hasCooldownToTrack) continue;
                        ps.setString(1, data.getUuid().toString());
                        ps.setString(2, data.getName());
                        ps.setString(3, data.getPrefix());
                        ps.setLong(4, data.getPrefixExpire());
                        ps.setString(5, data.getSuffix());
                        ps.setLong(6, data.getSuffixExpire());
                        ps.setLong(7, data.getLastCustomPrefixChange());
                        ps.setLong(8, data.getLastCustomSuffixChange());
                        ps.setLong(9, data.getLastSeen());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                databaseConnection.commit();
            } catch (SQLException e) {
                dirty.set(true);
                getLogger().severe("Could not save database: " + e.getMessage());
                try {
                    databaseConnection.rollback();
                } catch (SQLException ignored) {
                }
            } finally {
                try {
                    databaseConnection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void closeDatabase() {
        if (databaseConnection == null) return;
        try {
            databaseConnection.close();
        } catch (SQLException ignored) {
        }
    }

    private void writeActivityFile() {
        if (activityFile == null) return;
        long cutoff = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L);
        try {
            getDataFolder().mkdirs();
            try (FileWriter writer = new FileWriter(activityFile, false)) {
                writer.write("username\tprefix\tsuffix\tlast_seen\n");
                for (PlayerTagData data : playerCache.values()) {
                    if (data.isEmpty()) continue;
                    long lastSeen = data.getLastSeen();
                    if (lastSeen > 0 && lastSeen < cutoff) continue;
                    writer.write(data.getName() + "\t"
                            + renderStoredRaw(data.getPrefix()) + "\t"
                            + renderStoredRaw(data.getSuffix()) + "\t"
                            + (lastSeen > 0 ? Instant.ofEpochMilli(lastSeen) : "unknown") + "\n");
                }
            }
        } catch (IOException e) {
            getLogger().warning("Could not write active-tags.txt: " + e.getMessage());
        }
    }
}

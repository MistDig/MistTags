package com.mistdig.misttags;

import java.util.UUID;

/**
 * Holds the prefix/suffix state for one player. Deliberately contains only plain fields
 * and no Bukkit API calls, so it can safely be read from the in-memory cache by
 * PlaceholderAPI/TAB on whatever thread they happen to call from, without touching the
 * server's main-thread-only YamlConfiguration objects.
 */
public class PlayerTagData {

    private final UUID uuid;
    private volatile String name;

    private volatile String prefix;
    private volatile long prefixExpire;

    private volatile String suffix;
    private volatile long suffixExpire;

    // Timestamps of the player's own last self-service (misttags.custom) change, tracked
    // separately per type so a cooldown on prefixes doesn't also block suffixes. Never
    // touched by staff using misttags.manage.* -- see TagCommand#handleAdd.
    private volatile long lastCustomPrefixChange;
    private volatile long lastCustomSuffixChange;
    private volatile long lastSeen;

    public PlayerTagData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() { return uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrefix() { return prefix; }
    public long getPrefixExpire() { return prefixExpire; }
    public void setPrefix(String prefix, long expireAtEpochMs) {
        this.prefix = prefix;
        this.prefixExpire = expireAtEpochMs;
    }
    public void clearPrefix() {
        this.prefix = null;
        this.prefixExpire = 0;
    }
    public boolean isPrefixExpired() {
        return prefix != null && prefixExpire > 0 && System.currentTimeMillis() > prefixExpire;
    }

    public String getSuffix() { return suffix; }
    public long getSuffixExpire() { return suffixExpire; }
    public void setSuffix(String suffix, long expireAtEpochMs) {
        this.suffix = suffix;
        this.suffixExpire = expireAtEpochMs;
    }
    public void clearSuffix() {
        this.suffix = null;
        this.suffixExpire = 0;
    }
    public boolean isSuffixExpired() {
        return suffix != null && suffixExpire > 0 && System.currentTimeMillis() > suffixExpire;
    }

    public long getLastCustomPrefixChange() { return lastCustomPrefixChange; }
    public void setLastCustomPrefixChange(long epochMs) { this.lastCustomPrefixChange = epochMs; }

    public long getLastCustomSuffixChange() { return lastCustomSuffixChange; }
    public void setLastCustomSuffixChange(long epochMs) { this.lastCustomSuffixChange = epochMs; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long epochMs) { this.lastSeen = epochMs; }

    /** Used to skip writing empty entries to disk and to skip chat/display work. */
    public boolean isEmpty() {
        return prefix == null && suffix == null;
    }
}

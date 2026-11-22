package com.mistdig.misttags;

import java.util.UUID;

/** Contract checks for optional TAB integration; run without a Minecraft server. */
public final class TabListResenderTest {
    public static final class Player {
        boolean loaded = true;
        public boolean isLoaded() { return loaded; }
    }
    public static final class Manager {
        String prefix = "%misttags_display_prefix_legacy%&r&f";
        String suffix = "&r%misttags_display_suffix_legacy%&r";
        String override;
        int packets;
        public String getOriginalRawPrefix(Player p) { return prefix; }
        public String getOriginalRawSuffix(Player p) { return suffix; }
        public String getCustomName(Player p) { return override; }
        public void setName(Player p, String name) { override = name; packets++; }
    }
    public static final class Api {
        Player player = new Player();
        Manager manager = new Manager();
        public Player getPlayer(UUID id) { return player; }
        public Manager getTabListFormatManager() { return manager; }
    }
    public static void main(String[] args) throws Exception {
        Api api = new Api();
        UUID id = UUID.randomUUID();
        require(TabListResender.resend(api, id, Player.class), "unchanged configured tags must be resent");
        require(api.manager.packets == 1 && api.manager.override == null, "must preserve absence of an API override");
        api.manager.override = "%some_nickname%";
        TabListResender.resend(api, id, Player.class);
        require(api.manager.packets == 2 && api.manager.override.equals("%some_nickname%"), "must preserve another plugin's nickname override");
        require(api.manager.prefix.equals("%misttags_display_prefix_legacy%&r&f"), "must preserve prefix configuration");
        api.player.loaded = false;
        require(!TabListResender.resend(api, id, Player.class), "must wait for TAB player loading");
        api.player = null;
        require(!TabListResender.resend(api, id, Player.class), "must tolerate missing TAB player");
        api.player = new Player();
        api.manager.prefix = "%luckperms_prefix%";
        api.manager.suffix = "";
        require(!TabListResender.resend(api, id, Player.class), "must leave unrelated TAB formats alone");
        api.manager = null;
        require(!TabListResender.resend(api, id, Player.class), "must tolerate disabled list formatting");
        System.out.println("TAB resend contract checks passed");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

package com.guflimc.clans.spigot.chat;

import com.guflimc.brick.chat.spigot.api.SpigotChatAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanChat {

    private ClanChat() {}

    public static void init(JavaPlugin plugin) {
        SpigotChatAPI.get().channelByName("clan").ifPresent(ch -> {
            ClanChatChannel replacement = new ClanChatChannel(ch.name(), ch.activator(), ch.format());
            SpigotChatAPI.get().unregisterChatChannel(ch);
            SpigotChatAPI.get().registerChatChannel(replacement);
            plugin.getLogger().info("Injected custom chat channel for clans.");
        });

        plugin.getServer().getPluginManager().registerEvents(new PlayerChatListener(), plugin);
    }

}

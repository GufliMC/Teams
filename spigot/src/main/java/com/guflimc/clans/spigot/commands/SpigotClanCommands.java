package com.guflimc.clans.spigot.commands;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.guflimc.clans.spigot.SpigotClans;
import com.guflimc.clans.spigot.menu.ClanMenu;
import org.bukkit.entity.Player;

//@CommandContainer
public class SpigotClanCommands {

    private final SpigotClans plugin;

    public SpigotClanCommands(SpigotClans plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("clans menu")
    @CommandPermission("clans.menu")
    public void menu(Player sender) {
        ClanMenu.open(sender);
    }

    // TODO
//    @CommandMethod("clans info <input>")
//    @CommandPermission("lavaclans.clans.menu")
//    public void menu(Player sender, @Argument("input") String input) {
//        Clan clan = SpigotClanAPI.get().findClan(input).orElse(null);
//        if (clan != null) {
//            ClanMenu.clan(sender, clan);
//            return;
//        }
//
//        SpigotClanAPI.get().findProfile(input).thenAccept(profile -> {
//            ClanMenu.profile(sender, profile);
//        }).exceptionally(v -> {
//            SpigotI18nAPI.get(this).send(sender, "cmd.clans.info.not-found");
//            return null;
//        });
//    }

}

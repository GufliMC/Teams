package com.guflimc.clans.spigot.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.guflimc.brick.i18n.spigot.api.SpigotI18nAPI;
import com.guflimc.clans.spigot.SpigotClans;
import com.guflimc.clans.spigot.api.SpigotClanAPI;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

//@CommandContainer
public class SpigotCrestCommands {

    private final SpigotClans plugin;

    public SpigotCrestCommands(SpigotClans plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("crests list")
    @CommandPermission("clans.crests.list")
    public void list(Player player) {
        // TODO open menu with list of sigils with option to remove them or toggle restricted mode
    }

    // TODO
//    @CommandMethod("crests add <name> <restricted>")
//    @CommandPermission("clans.crests.add")
//    public void add(Player player, @Argument("name") String name, @Argument("restricted") boolean restricted) {
//        ItemStack hand = player.getInventory().getItemInMainHand();
//        if ( hand.getType().toString().contains("_BANNER") ) {
//            SpigotI18nAPI.get(this).send(player, "cmd.clans.sigils.add.error.banner");
//            return;
//        }
//
//        BannerMeta meta = (BannerMeta) hand.getItemMeta();
//        if ( meta == null ) {
//            SpigotI18nAPI.get(this).send(player, "cmd.clans.sigils.add.error.banner");
//            return;
//        }
//
//        SpigotClanAPI.get().addCrestTemplate(name, meta.getPatterns().stream().map(Pattern::getPattern).toList(), restricted);
//        SpigotI18nAPI.get(this).send(player, "cmd.clans.sigils.add", name);
//    }

}

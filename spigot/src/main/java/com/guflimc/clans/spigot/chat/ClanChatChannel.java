package com.guflimc.clans.spigot.chat;

import com.guflimc.brick.chat.api.channel.AbstractChatChannel;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ClanChatChannel extends AbstractChatChannel<Player> {

    public ClanChatChannel(String name, String activator, Component format) {
        super(name, activator, format, false);
    }

    @Override
    public boolean canRead(Player player) {
        return TeamAPI.get().findCachedProfile(player.getUniqueId()).flatMap(Profile::clanProfile).isPresent();
    }

    @Override
    public boolean canTalk(Player player) {
        return TeamAPI.get().findCachedProfile(player.getUniqueId()).flatMap(Profile::clanProfile).isPresent();
    }
}

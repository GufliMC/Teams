package com.guflimc.clans.spigot.chat;

import com.guflimc.brick.chat.spigot.api.event.SpigotPlayerChannelChatEvent;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void onChat(SpigotPlayerChannelChatEvent event) {
        if ( !(event.chatChannel() instanceof ClanChatChannel) ) {
            return;
        }

        Team team = TeamAPI.get().findCachedProfile(event.player().getUniqueId())
                .flatMap(Profile::clanProfile).orElseThrow().clan();

        event.recipients().removeIf(p ->
                !TeamAPI.get().findCachedProfile(p.getUniqueId())
                .flatMap(Profile::clanProfile).orElseThrow()
                .clan().equals(team));
    }

}

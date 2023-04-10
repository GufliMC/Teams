package com.guflimc.clans.spigot.placeholders;

import com.guflimc.brick.placeholders.api.module.BasePlaceholderModule;
import com.guflimc.brick.placeholders.api.resolver.PlaceholderResolver;
import com.guflimc.brick.placeholders.spigot.api.SpigotPlaceholderAPI;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class ClanPlaceholders {

    private static <T> PlaceholderResolver<Player, T> profile(Function<Profile, T> map) {
        return PlaceholderResolver.requireEntity((placeholder, context) ->
                TeamAPI.get().findCachedProfile(context.entity().getUniqueId())
                        .map(map).orElse(null));
    }

    private static <T> PlaceholderResolver<Player, T> clanProfile(Function<Membership, T> map) {
        return profile(profile -> profile.clanProfile().map(map).orElse(null));
    }

    public static void init() {
        BasePlaceholderModule<Player> module = new BasePlaceholderModule<>("clan");
        module.register("name", clanProfile(cp -> Component.text(cp.clan().name())));
        module.register("display_name", clanProfile(cp -> cp.clan().displayName()));
        module.register("tag", clanProfile(cp -> Component.text(cp.clan().tag())));
        module.register("display_tag", clanProfile(cp -> cp.clan().displayTag()));
        module.register("member_count", clanProfile(cp -> cp.clan().memberCount()));
        module.register("member_limit", clanProfile(cp -> cp.clan().memberLimit()));
        module.register("color", clanProfile(cp -> cp.clan().namedTextColor()));
        SpigotPlaceholderAPI.get().register(module);
    }

}

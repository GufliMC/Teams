package com.guflimc.clans.spigot.util;

import com.guflimc.brick.gui.spigot.item.ItemStackBuilder;
import com.guflimc.brick.gui.spigot.item.specific.BannerBuilder;
import com.guflimc.teams.api.crest.CrestConfig;
import com.guflimc.teams.api.crest.CrestType;
import com.guflimc.teams.api.domain.Team;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClanTools {

    private static final Map<DyeColor, DyeColor> secondary = new HashMap<>();

    static {
        secondary.put(DyeColor.WHITE, DyeColor.LIGHT_GRAY);
        secondary.put(DyeColor.LIGHT_GRAY, DyeColor.WHITE);
        secondary.put(DyeColor.GRAY, DyeColor.BLACK);
        secondary.put(DyeColor.BLACK, DyeColor.GRAY);
        secondary.put(DyeColor.RED, DyeColor.BROWN);
        secondary.put(DyeColor.BROWN, DyeColor.RED);
        secondary.put(DyeColor.ORANGE, DyeColor.YELLOW);
        secondary.put(DyeColor.YELLOW, DyeColor.ORANGE);
        secondary.put(DyeColor.LIME, DyeColor.GREEN);
        secondary.put(DyeColor.GREEN, DyeColor.LIME);
        secondary.put(DyeColor.CYAN, DyeColor.LIGHT_BLUE);
        secondary.put(DyeColor.LIGHT_BLUE, DyeColor.CYAN);
        secondary.put(DyeColor.PURPLE, DyeColor.MAGENTA);
        secondary.put(DyeColor.MAGENTA, DyeColor.PURPLE);
        secondary.put(DyeColor.PINK, DyeColor.MAGENTA);
        secondary.put(DyeColor.BLUE, DyeColor.CYAN);
    }

    private static DyeColor select(CrestType.Color color, DyeColor background, DyeColor foreground) {
        if (color == CrestType.Color.BACKGROUND) {
            return background;
        } else if (color == CrestType.Color.FOREGROUND) {
            return foreground;
        } else if (color == CrestType.Color.ACCENT) {
            return secondary.get(foreground);
        } else {
            return DyeColor.valueOf(color.name());
        }
    }

    public static ItemStack crest(@NotNull CrestType type, @NotNull CrestConfig config, @NotNull DyeColor main) {
        DyeColor background;
        DyeColor foreground;

        if (config.target() == CrestConfig.ColorTarget.BACKGROUND) {
            background = DyeColor.valueOf(config.color().name());
            foreground = main;
        } else {
            background = main;
            foreground = DyeColor.valueOf(config.color().name());
        }

//        DyeColor secondary = ClanTools.secondary.get(foreground);

        BannerBuilder b = ItemStackBuilder.banner(select(type.background(), background, foreground));

        for (CrestType.CrestLayer layer : type.layers()) {
            DyeColor color = select(layer.color(), background, foreground);
            layer.patterns().forEach(p -> b.withBannerPattern(color, PatternType.valueOf(p.name())));
        }

        return b.build();
    }

    public static DyeColor dyeColor(@NotNull Team team) {
        return dyeColor(team.color());
    }

    public static DyeColor dyeColor(int ahex) {
        DyeColor closestMatch = null;
        int minMSE = Integer.MAX_VALUE;

        for (DyeColor color : DyeColor.values()) {
            int bhex = color.getColor().asRGB();
            int mse = mse(ahex, bhex);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = color;
            }
        }
        return closestMatch;
    }

    private static int mse(int ahex, int bhex) {
        int ar = ahex >> 16 & 0xff;
        int ag = ahex >> 8 & 0xff;
        int ab = ahex & 0xff;

        int br = bhex >> 16 & 0xff;
        int bg = bhex >> 8 & 0xff;
        int bb = bhex & 0xff;

        return mse(ar, ag, ab, br, bg, bb);
    }

    private static int mse(int ar, int ag, int ab, int br, int bg, int bb) {
        return ((ar - br) * (ar - br)
                + (ag - bg) * (ag - bg)
                + (ab - bb) * (ab - bb)) / 3;
    }

}

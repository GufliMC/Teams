package com.guflimc.teams.api.domain.traits;

import com.guflimc.teams.api.domain.TeamTrait;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public interface TeamColorTrait extends TeamTrait {

    int rgbColor();

    void setRgbColor(int rgb);


    default TextColor textColor() {
        return TextColor.color(rgbColor());
    }

    default NamedTextColor namedTextColor() {
        return NamedTextColor.nearestTo(textColor());
    }

}

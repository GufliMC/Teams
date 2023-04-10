package com.guflimc.teams.common;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;
import com.guflimc.config.common.ConfigComment;

public class ClansConfig {

    @ConfigComment("DO NOT TOUCH THIS! ADVANCED USAGE ONLY!")
    public EbeanConfig database = new EbeanConfig();

    @Deprecated
    public transient String clanChatPrefix = "<insert:/clans info {clan_name}><hover:show_text:'{clan_display_name}'><gray>[{clan_display_tag}]</gray></hover></insert>";

    @Deprecated
    public transient String clanNametagPrefix = "<gray>[{clan_display_tag}]</gray> ";

    @Deprecated
    public transient String noClanDisplayName = "";

    public ClansConfig() {
        database.dsn = "jdbc:h2:file:./plugins/Clans/data/database.h2;MODE=MySQL";
        database.driver = "org.h2.Driver";
        database.username = "user";
        database.password = "";
    }

}

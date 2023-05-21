package com.guflimc.teams.common.config;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;
import com.guflimc.config.common.ConfigComment;

import java.util.ArrayList;
import java.util.List;

public class TeamsConfig {

    @ConfigComment("DO NOT TOUCH THIS! ADVANCED USAGE ONLY!")
    public EbeanConfig database = new EbeanConfig();

    public List<TeamTypeConfig> teamTypes;

    public static class TeamTypeConfig {

        public String name;

        public boolean colorTrait;
        public boolean inviteTrait;
        public boolean memberLimitTrait;
        public boolean tagTrait;


        public int maxNameLength;
        public int maxTagLength;
        public int defaultMemberLimit;

    }

    public TeamsConfig() {
        database.dsn = "jdbc:h2:file:./plugins/Teams/data/database.h2;MODE=MySQL";
        database.driver = "org.h2.Driver";
        database.username = "user";
        database.password = "";

        TeamTypeConfig example = new TeamTypeConfig();
        example.name = "guild";
        example.colorTrait = true;
        example.inviteTrait = true;
        example.memberLimitTrait = true;
        example.tagTrait = true;

        example.maxNameLength = 20;
        example.maxTagLength = 3;
        example.defaultMemberLimit = 10;

        teamTypes = List.of(example);
    }

}

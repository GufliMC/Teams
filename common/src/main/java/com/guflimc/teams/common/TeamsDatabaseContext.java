package com.guflimc.teams.common;

import com.guflimc.brick.orm.ebean.database.EbeanConfig;
import com.guflimc.brick.orm.ebean.database.EbeanDatabaseContext;
import com.guflimc.brick.orm.ebean.database.EbeanMigrations;
import com.guflimc.teams.common.domain.*;
import io.ebean.annotation.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

public class TeamsDatabaseContext extends EbeanDatabaseContext {

    private final static String DATASOURCE_NAME = "Teams";

    public TeamsDatabaseContext(EbeanConfig config) {
        super(config, DATASOURCE_NAME);
    }

    public TeamsDatabaseContext(EbeanConfig config, int poolSize) {
        super(config, DATASOURCE_NAME, poolSize);
    }

    @Override
    protected Class<?>[] applicableClasses() {
        return APPLICABLE_CLASSES;
    }

    private static final Class<?>[] APPLICABLE_CLASSES = new Class[]{
            DTeam.class,
            DMembership.class,
            DMembershipAttribute.class,
            DProfile.class,
            DProfileAttribute.class,
            DTeamInvite.class,
            DTeamAttribute.class,
            DTeamInvite.class
    };

    public static void main(String[] args) throws IOException, SQLException {
        EbeanMigrations generator = new EbeanMigrations(
                DATASOURCE_NAME,
                Path.of("Teams/common/src/main/resources"),
                Platform.H2, Platform.MYSQL
        );
        Arrays.stream(APPLICABLE_CLASSES).forEach(generator::addClass);
        generator.generate();
    }

}

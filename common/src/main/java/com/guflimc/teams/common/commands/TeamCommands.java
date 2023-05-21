package com.guflimc.teams.common.commands;

import cloud.commandframework.annotations.Argument;
import com.guflimc.brick.i18n.api.I18nAPI;
import com.guflimc.colonel.common.Colonel;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamType;
import com.guflimc.teams.api.domain.traits.TeamInviteTrait;
import com.guflimc.teams.api.domain.traits.TeamMemberLimitTrait;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

//@CommandContainer
public class TeamCommands {

    private final AudienceProvider adventure;

    public TeamCommands(AudienceProvider adventure, List<TeamType> types, Colonel<Audience> colonel) {
        this.adventure = adventure;
        types.forEach(type -> register(type, colonel));
    }

    private void register(TeamType type, Colonel<Audience> colonel) {
        colonel.builder()
                .path("t " + type.name() + " list")
                .executor(ctx -> list(type, ctx.source()))
                .register();

        colonel.builder()
                .path("t " + type.name() + " invite")
                .parameter("username").type(String.class).completer(Audience.class).done()
                .source(Profile.class)
                .executor(ctx -> invite(type, ctx.source(), ctx.source(0), ctx.argument("username")))
                .register();
    }

    private void list(TeamType type, Audience sender) {
        I18nAPI.get(this).send(sender, "cmd.teams.list",
                type.name(), TeamAPI.get().teams(type).stream().map(Team::name).toList());
    }

    private void invite(TeamType type, Audience sender, Profile senderp, String username) {
        if ( senderp.membership(type).isEmpty() ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.team");
            return;
        }

        Team team = senderp.membership(type).orElseThrow().team();

        TeamInviteTrait invTrait = team.trait(TeamInviteTrait.class).orElse(null);
        if ( invTrait == null ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        // TODO invite permission with trait
//        if (!team.trait(TeamPermissionTrait.class)
//                .map(trait -> trait.hasPermission(senderp, "invite"))
//                .orElse(false)) {
//            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
//            return;
//        }

        if ( team.trait(TeamMemberLimitTrait.class).map(trait -> trait.memberLimit() <= team.members()).orElse(false) ) {
            I18nAPI.get(this).send(sender, "cmd.teams.invite.error.max.members");
            return;
        }

        TeamAPI.get().profile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            if (target.membership(type).isPresent()) {
                I18nAPI.get(this).send(sender, "cmd.teams.invite.error.already.in.team.type");
                return;
            }

            TeamInviteTrait.TeamInvite invite = invTrait.invite(target).orElse(null);
            if ( invite != null && !invite.isExpired() && !invite.isAnswered()) {
                I18nAPI.get(this).send(sender, "cmd.teams.invite.error.already.invited");
                return;
            }

            invTrait.invite(senderp, target);
            TeamAPI.get().update(team);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.teams.invite.sender", target.name());

            Audience targetAudience = adventure.player(target.id());

            // to target
            Component accept = I18nAPI.get(this).hoverable(targetAudience, "chat.button.accept", "chat.button.accept.hover")
                    .clickEvent(ClickEvent.runCommand("/t " + type.name() + " join " + team.name()));
            Component decline = I18nAPI.get(this).hoverable(targetAudience, "chat.button.decline", "chat.button.decline.hover")
                    .clickEvent(ClickEvent.runCommand("/t " + type.name() + " reject " + team.name()));

            Component message = I18nAPI.get(this).translate(targetAudience, "cmd.teams.invite.target", senderp.name(), team.name());

            int width = I18nAPI.get(this).width(message);
            Component buttons = I18nAPI.get(this).paddingAround(width, accept, decline);

            I18nAPI.get(this).menu(targetAudience, message, Component.text(""), buttons);

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void uninvite(TeamType type, Audience sender, Profile senderp, String username) {
        if ( senderp.membership(type).isEmpty() ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.team");
            return;
        }

        Team team = senderp.membership(type).orElseThrow().team();

        TeamInviteTrait invTrait = team.trait(TeamInviteTrait.class).orElse(null);
        if ( invTrait == null ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        // TODO invite permission with trait
//        if (!team.trait(TeamPermissionTrait.class)
//                .map(trait -> trait.hasPermission(senderp, "invite"))
//                .orElse(false)) {
//            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
//            return;
//        }

        TeamAPI.get().profile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            TeamInviteTrait.TeamInvite invite = invTrait.invite(target).orElse(null);
            if ( invite == null || !invite.isActive() ) {
                I18nAPI.get(this).send(sender, "cmd.teams.uninvite.error.not.invited");
                return;
            }

            invite.cancel();
            TeamAPI.get().update(team);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.teams.uninvite.sender", target.name());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void kick(TeamType type, Audience sender, Profile senderp, String username) {
        if ( senderp.membership(type).isEmpty() ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.team");
            return;
        }

        Team team = senderp.membership(type).orElseThrow().team();

        // TODO kick permission with trait
//        if (!team.trait(TeamPermissionTrait.class)
//                .map(trait -> trait.hasPermission(senderp, "kick"))
//                .orElse(false)) {
//            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
//            return;
//        }

        TeamAPI.get().profile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            if ( target.membership(team).isEmpty() ) {
                I18nAPI.get(this).send(sender, "cmd.teams.kick.error.not.in.clan");
                return;
            }

            target.membership(team).get().quit();
            TeamAPI.get().update(target);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.teams.kick.sender", target.name());

            Audience targetAudience = adventure.player(target.id());
            I18nAPI.get(this).send(targetAudience, "cmd.teams.kick.target");
        });
    }

    public void join(TeamType type, Audience sender, Profile senderp, Team team) {
        if ( senderp.membership(type).isPresent()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.already.in.team.type");
            return;
        }

        if ( team.trait(TeamMemberLimitTrait.class).map(trait -> trait.memberLimit() <= team.members()).orElse(false) ) {
            I18nAPI.get(this).send(sender, "cmd.teams.join.error.max.members");
            return;
        }

        TeamInviteTrait invTrait = team.trait(TeamInviteTrait.class).orElse(null);
        if ( invTrait != null ) {
            TeamInviteTrait.TeamInvite invite = invTrait.invite(senderp).orElse(null);
            if ( invite == null || !invite.isActive() ) {
                I18nAPI.get(this).send(sender, "cmd.teamsjoin.error.missing");
                return;
            }

            invite.accept();
        } else {
            senderp.join(team);
        }

        TeamAPI.get().update(senderp);

        I18nAPI.get(this).send(sender, "cmd.teams.join", team.name());
    }

    public void reject(TeamType type, Audience sender, Profile senderp, Team team) {
        TeamInviteTrait invTrait = team.trait(TeamInviteTrait.class).orElse(null);
        if ( invTrait == null ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        TeamInviteTrait.TeamInvite invite = invTrait.invite(target).orElse(null);
        if ( invite == null || !invite.isActive() ) {
            I18nAPI.get(this).send(sender, "cmd.teams.join.error.missing");
            return;
        }

        invite.decline();
        TeamAPI.get().update(senderp);

        I18nAPI.get(this).send(sender, "cmd.teams.decline", team.name());

        Audience invsender = adventure.player(invite.sender().id());
        I18nAPI.get(this).send(invsender, "cmd.teams.decline.sender", senderp.name());
    }

    public void quit(TeamType type, Audience sender, Profile senderp) {
        if ( senderp.membership(type).isEmpty() ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.team");
            return;
        }

        senderp.membership(type).get().quit();
        TeamAPI.get().update(senderp);

        I18nAPI.get(this).send(sender, "cmd.teams.quit", type);
    }

    public void disband(TeamType type, Audience sender, Profile senderp) {
        if ( senderp.membership(type).isEmpty() ) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.team");
            return;
        }

//        if (!sprofile.clanProfile().get().isLeader()) {
//            I18nAPI.get(this).send(sender, "cmd.clans.perms.error.not.leader");
//            return;
//        }

        TeamAPI.get().remove(senderp.membership(type).get().team());
        I18nAPI.get(this).send(sender, "cmd.team.disband");
    }

    public void create(Audience sender, Profile sprofile, @Argument("name") String name, @Argument("tag") String tag) {
        if (sprofile.clanProfile().isPresent()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.already.in.clan");
            return;
        }

        if (sprofile.clanProfile().isPresent()) {
            I18nAPI.get(this).send(sender, "cmd.clans.join.error.already");
            return;
        }

        if (!name.matches("[a-zA-Z0-9]{2,24}")) {
            I18nAPI.get(this).send(sender, "cmd.clans.create.error.name.format", name);
            return;
        }

        if (!tag.matches("[a-zA-Z0-9]{2,3}")) {
            I18nAPI.get(this).send(sender, "cmd.clans.create.error.tag.format", tag);
            return;
        }

        if (TeamAPI.get().findClan(name).isPresent()) {
            I18nAPI.get(this).send(sender, "cmd.clans.create.error.name.exists");
            return;
        }

        tag = tag.toUpperCase();
        if (TeamAPI.get().findClanByTag(tag).isPresent()) {
            I18nAPI.get(this).send(sender, "cmd.clans.create.error.tag.exists");
            return;
        }

        TeamAPI.get().create(sprofile, name, tag).thenAccept(clan -> {
            I18nAPI.get(this).send(sender, "cmd.clans.create", clan.name(), clan.tag());
        });
    }

}

package com.guflimc.teams.common.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.guflimc.brick.i18n.api.I18nAPI;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.clans.api.domain.*;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamInvite;
import com.guflimc.teams.api.domain.ClanPermission;
import com.guflimc.teams.api.domain.Profile;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

//@CommandContainer
public class ClanCommands {

    private final AudienceProvider adventure;

    public ClanCommands(AudienceProvider adventure) {
        this.adventure = adventure;
    }

    @CommandMethod("clans list")
    @CommandPermission("clans.list")
    public void list(Audience sender) {
        I18nAPI.get(this).send(sender, "cmd.clans.list",
                TeamAPI.get().clans().stream().map(Team::name).toList());
    }

    @CommandMethod("clans invite <player>")
    @CommandPermission("clans.invite")
    public void invite(Audience sender, Profile sprofile, @Argument("player") String username) {
        if (sprofile.clanProfile().isEmpty()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.clan");
            return;
        }

        Team team = sprofile.clanProfile().orElseThrow().clan();

        if (!sprofile.clanProfile().get().hasPermission(ClanPermission.INVITE_PLAYERS)) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        if (team.memberCount() >= team.memberLimit()) {
            I18nAPI.get(this).send(sender, "cmd.clans.invite.error.max.members");
            return;
        }

        TeamAPI.get().findProfile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            if (target.clanProfile().isPresent()) {
                I18nAPI.get(this).send(sender, "cmd.clans.invite.error.already.in.clan");
                return;
            }

            TeamInvite recent = target.mostRecentInvite(team).orElse(null);
            if (recent != null && !recent.isExpired() && !recent.isAnswered()) {
                I18nAPI.get(this).send(sender, "cmd.clans.invite.error.already.invited");
                return;
            }

            target.addInvite(sprofile, team);
            TeamAPI.get().update(target);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.clans.invite.sender", target.name());

            Audience targetAudience = adventure.player(target.id());

            // to target
            Component accept = I18nAPI.get(this).hoverable(targetAudience, "chat.button.accept", "chat.button.accept.hover")
                    .clickEvent(ClickEvent.runCommand("/clans join " + team.name()));
            Component decline = I18nAPI.get(this).hoverable(targetAudience, "chat.button.decline", "chat.button.decline.hover")
                    .clickEvent(ClickEvent.runCommand("/clans reject " + team.name()));

            Component message = I18nAPI.get(this).translate(targetAudience, "cmd.clans.invite.target", sprofile.name(), team.name());

            int width = I18nAPI.get(this).width(message);
            Component buttons = I18nAPI.get(this).paddingAround(width, accept, decline);

            I18nAPI.get(this).menu(targetAudience, message, Component.text(""), buttons);

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @CommandMethod("clans uninvite <player>")
    @CommandPermission("clans.uninvite")
    public void uninvite(Audience sender, Profile sprofile, @Argument("player") String username) {
        if (sprofile.clanProfile().isEmpty()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.clan");
            return;
        }

        Team team = sprofile.clanProfile().orElseThrow().clan();

        if (!sprofile.clanProfile().get().hasPermission(ClanPermission.INVITE_PLAYERS)) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        TeamAPI.get().findProfile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            TeamInvite recent = target.mostRecentInvite(team).orElse(null);
            if (recent == null || !recent.isActive()) {
                I18nAPI.get(this).send(sender, "cmd.clans.uninvite.error.not.invited");
                return;
            }

            recent.cancel();
            TeamAPI.get().update(target);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.clans.uninvite.sender", target.name());
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @CommandMethod("clans kick <player>")
    @CommandPermission("clans.kick")
    public void kick(Audience sender, Profile sprofile, @Argument("player") String username) {
        if (sprofile.clanProfile().isEmpty()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.clan");
            return;
        }

        if (!sprofile.clanProfile().get().hasPermission(ClanPermission.KICK_MEMBERS)) {
            I18nAPI.get(this).send(sender, "cmd.error.base.no.permission");
            return;
        }

        Team team = sprofile.clanProfile().orElseThrow().clan();

        TeamAPI.get().findProfile(username).thenAccept(target -> {
            if (target == null) {
                I18nAPI.get(this).send(sender, "cmd.error.args.player", username);
                return;
            }

            if (target.clanProfile().isEmpty() || !target.clanProfile().get().clan().equals(team)) {
                I18nAPI.get(this).send(sender, "cmd.clans.kick.error.not.in.clan");
                return;
            }

            target.clanProfile().get().quit();
            TeamAPI.get().update(target);

            // send messages
            I18nAPI.get(this).send(sender, "cmd.clans.kick.sender", target.name());

            Audience targetAudience = adventure.player(target.id());
            I18nAPI.get(this).send(targetAudience, "cmd.clans.kick.target");
        });
    }

    @CommandMethod("clans join <clan>")
    @CommandPermission("clans.join")
    public void join(Audience sender, Profile sprofile, @Argument("clan") Team team) {
//        if (sprofile.clanProfile().isPresent()) {
//            I18nAPI.get(this).send(sender, "cmd.error.base.already.in.clan");
//            return;
//        }

        TeamInvite recent = sprofile.mostRecentInvite(team).orElse(null);
        if (recent == null || !recent.isActive()) {
            I18nAPI.get(this).send(sender, "cmd.clans.join.error.missing");
            return;
        }

        if (team.memberCount() >= team.memberLimit()) {
            I18nAPI.get(this).send(sender, "cmd.clans.join.error.max.members");
            return;
        }

        recent.accept();
        TeamAPI.get().update(sprofile);

        I18nAPI.get(this).send(sender, "cmd.clans.join", team.name());
    }

    @CommandMethod("clans reject <clan>")
    @CommandPermission("clans.reject")
    public void reject(Audience sender, Profile sprofile, @Argument("clan") Team team) {
        TeamInvite recent = sprofile.mostRecentInvite(team).orElse(null);
        if (recent == null || !recent.isActive()) {
            I18nAPI.get(this).send(sender, "cmd.clans.join.error.missing");
            return;
        }

        recent.reject();
        TeamAPI.get().update(sprofile);

        I18nAPI.get(this).send(sender, "cmd.clans.reject", team.name());

        Audience invsender = adventure.player(recent.sender().id());
        I18nAPI.get(this).send(invsender, "cmd.clans.reject.sender", sprofile.name());
    }

    @CommandMethod("clans quit")
    @CommandPermission("clans.quit")
    public void quit(Audience sender, Profile sprofile) {
        if (sprofile.clanProfile().isEmpty()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.clan");
            return;
        }

        if (sprofile.clanProfile().get().isLeader()) {
            I18nAPI.get(this).send(sender, "cmd.clans.quit.error.leader");
            return;
        }

        sprofile.clanProfile().get().quit();
        TeamAPI.get().update(sprofile);

        I18nAPI.get(this).send(sender, "cmd.clans.quit");
    }

    @CommandMethod("clans disband")
    @CommandPermission("clans.disband")
    public void disband(Audience sender, Profile sprofile) {
        if (sprofile.clanProfile().isEmpty()) {
            I18nAPI.get(this).send(sender, "cmd.error.base.not.in.clan");
            return;
        }

        if (!sprofile.clanProfile().get().isLeader()) {
            I18nAPI.get(this).send(sender, "cmd.clans.perms.error.not.leader");
            return;
        }

        TeamAPI.get().remove(sprofile.clanProfile().get().clan());
        I18nAPI.get(this).send(sender, "cmd.clans.disband");
    }

    @CommandMethod("clans create <name> <tag>")
    @CommandPermission("clans.create")
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

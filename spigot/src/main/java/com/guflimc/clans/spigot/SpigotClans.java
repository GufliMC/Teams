package com.guflimc.clans.spigot;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.brick.i18n.spigot.api.SpigotI18nAPI;
import com.guflimc.brick.i18n.spigot.api.namespace.SpigotNamespace;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.common.config.TeamsConfig;
import com.guflimc.teams.common.TeamsDatabaseContext;
import com.guflimc.teams.common.EventManager;
import com.guflimc.teams.common.commands.TeamCommands;
import com.guflimc.teams.common.commands.arguments.ClanArgument;
import com.guflimc.clans.spigot.api.SpigotClanAPI;
import com.guflimc.clans.spigot.chat.ClanChat;
import com.guflimc.clans.spigot.commands.SpigotClanCommands;
import com.guflimc.clans.spigot.commands.SpigotCrestCommands;
import com.guflimc.clans.spigot.listeners.ClanListener;
import com.guflimc.clans.spigot.listeners.JoinQuitListener;
import com.guflimc.clans.spigot.placeholders.ClanPlaceholders;
import com.guflimc.config.toml.TomlConfig;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SpigotClans extends JavaPlugin {

    public static SpigotScheduler scheduler;

    private TeamsDatabaseContext databaseContext;
    public SpigotBrickTeamManager clanManager;

    public TeamsConfig config;
    public BukkitAudiences adventure;

    @Override
    public void onEnable() {
        // CONFIG
        config = TomlConfig.load(getDataFolder().toPath().resolve("config.toml"), new TeamsConfig());

        // GUI
        SpigotBrickGUI.register(this);

        // ADVENTURE
        adventure = BukkitAudiences.create(this);

        // DATABASE
        databaseContext = new TeamsDatabaseContext(config.database);

        // EVENT MANAGER
        EventManager.INSTANCE = new SpigotEventManager();

        // CLAN MANAGER
        clanManager = new SpigotBrickTeamManager(databaseContext);
        SpigotClanAPI.register(clanManager);

        // LOAD PLAYERS
        CompletableFuture.allOf(Bukkit.getServer().getOnlinePlayers().stream()
                .map(p -> clanManager.login(p.getUniqueId(), p.getName()))
                .toArray(CompletableFuture[]::new)).join();

        // TRANSLATIONS
        SpigotNamespace namespace = new SpigotNamespace(this, Locale.ENGLISH);
        namespace.loadValues(this, "languages");
        SpigotI18nAPI.get().register(namespace);

        // SCHEDULER
        scheduler = new SpigotScheduler(this, getName());

        // COMMANDS
        setupCommands();

        PluginManager pm = getServer().getPluginManager();

        // PLACEHOLDERS
        if (pm.isPluginEnabled("BrickPlaceholders")) {
            ClanPlaceholders.init();
        }

        // CHAT
        if (pm.isPluginEnabled("BrickChat")) {
            ClanChat.init(this);
        }

        // EVENTS
        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(new ClanListener(), this);

        //
        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void onDisable() {
        if (databaseContext != null) {
            databaseContext.shutdown();
        }

        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getDescription().getName() + " v" + getDescription().getVersion();
    }

    private void setupCommands() {
        // COMMANDS
        try {
            BukkitCommandManager<CommandSender> commandManager = new BukkitCommandManager<>(
                    this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );
//            commandManager.registerBrigadier();

            commandManager.parserRegistry().registerParserSupplier(TypeToken.get(Team.class),
                    ps -> new ClanArgument.ClanParser<>());

            AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(
                    commandManager,
                    CommandSender.class,
                    parameters -> SimpleCommandMeta.empty()
            );

            annotationParser.getParameterInjectorRegistry().registerInjector(Profile.class,
                    (context, annotationAccessor) -> clanManager.findCachedProfile(((Player) context.getSender()).getUniqueId()).orElseThrow());

            annotationParser.parse(new TeamCommands(adventure));
            annotationParser.parse(new SpigotClanCommands(this));
            annotationParser.parse(new SpigotCrestCommands(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.setFormat(MessageType.SYNTAX, ChatColor.GRAY, ChatColor.GREEN, ChatColor.DARK_GREEN);

        // REPLACEMENTS
        commandManager.getCommandReplacements().addReplacement("rootCommand", config.rootCommand);

        // CONTEXTS
        CommandContexts<BukkitCommandExecutionContext> ctxs = commandManager.getCommandContexts();

        ctxs.registerIssuerOnlyContext(Audience.class,
                ctx -> adventure.player(ctx.getPlayer()));

        ctxs.registerIssuerOnlyContext(Profile.class,
                ctx -> manager.findCachedProfile(ctx.getPlayer().getUniqueId()));

        ctxs.registerContext(Clan.class, ctx -> {
            String name = ctx.popFirstArg();
            return manager.findClan(name)
                    .orElseThrow(() -> {
                        SpigotI18nAPI.get(this).send(ctx.getPlayer(), "cmd.error.args.clan", name);
                        return new InvalidCommandArgument();
                    });
        });

        // CONDITIONS
        CommandConditions<BukkitCommandIssuer, BukkitCommandExecutionContext, BukkitConditionContext> conds
                = commandManager.getCommandConditions();

        conds.addCondition("clan", ctx -> {
            if (manager.findCachedProfile(ctx.getIssuer().getUniqueId()).clanProfile().isEmpty()) {
                SpigotI18nAPI.get(this).send(ctx.getIssuer().getPlayer(), "cmd.error.base.not.in.clan");
                throw new ConditionFailedException();
            }
        });

        // COMPLETIONS
        CommandCompletions<BukkitCommandCompletionContext> cmpls = commandManager.getCommandCompletions();

        cmpls.registerCompletion("clan", ctx ->
                manager.clans().stream().map(Clan::name).toList());

        // REGISTER
        commandManager.registerCommand(new SpigotLavaClansCommands(this));
         */
    }
}
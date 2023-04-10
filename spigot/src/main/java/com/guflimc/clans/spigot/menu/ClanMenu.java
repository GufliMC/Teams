package com.guflimc.clans.spigot.menu;

import com.guflimc.brick.gui.spigot.SpigotBrickGUI;
import com.guflimc.brick.gui.spigot.api.ISpigotMenu;
import com.guflimc.brick.gui.spigot.api.ISpigotMenuBuilder;
import com.guflimc.brick.gui.spigot.api.ISpigotMenuRowBuilder;
import com.guflimc.brick.gui.spigot.api.ISpigotPaginatedMenuBuilder;
import com.guflimc.brick.gui.spigot.item.ItemStackBuilder;
import com.guflimc.brick.gui.spigot.menu.SpigotMenuItem;
import com.guflimc.brick.i18n.spigot.api.SpigotI18nAPI;
import com.guflimc.brick.i18n.spigot.api.namespace.SpigotNamespace;
import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.crest.CrestConfig;
import com.guflimc.teams.api.crest.CrestType;
import com.guflimc.clans.api.domain.*;
import com.guflimc.clans.spigot.SpigotClans;
import com.guflimc.clans.spigot.api.SpigotClanAPI;
import com.guflimc.clans.spigot.util.ClanTools;
import com.guflimc.teams.api.domain.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ClanMenu {

    private static final SpigotNamespace namespace = SpigotI18nAPI.get().byClass(SpigotClans.class);

    private static void setup(ISpigotPaginatedMenuBuilder bmenu, Player player) {
        setup(bmenu, player, null);
    }

    private static ItemStack backItem(Player player) {
        return ItemStackBuilder.of(Material.RED_BED)
                .withName(namespace.string(player, "menu.items.back.name"))
                .withLore(namespace.string(player, "menu.items.back.lore"))
                .build();
    }

    private static void setup(ISpigotPaginatedMenuBuilder bmenu, Player player, Runnable back) {
        if (back != null) {
            bmenu.withHotbarItem(4, backItem(player), c -> {
                back.run();
            });
        }

//        bmenu.withBackItem(ItemStackBuilder.of(Material.RED_BED)
//                .withName(namespace.string(player, "menu.items.previousPage"))
//                .build());
//
//        bmenu.withBackItem(ItemStackBuilder.of(Material.RED_BED)
//                .withName(namespace.string(player, "menu.items.nextPage"))
//                .build());
    }

    private static boolean hasPermission(Player player, Team team, ClanPermission permission) {
        if (player.hasPermission("clans.admin")) {
            return true;
        }
        Membership profile = TeamAPI.get().findCachedProfile(player.getUniqueId())
                .flatMap(Profile::clanProfile).orElse(null);
        if (profile == null || !profile.clan().equals(team)) {
            return false;
        }
        return profile.hasPermission(permission);
    }

    //

    public static void open(Player player) {
        ISpigotMenuBuilder bmenu = SpigotBrickGUI.builder()
                .withTitle(namespace.string(player, "menu.main.title"));

        Profile profile = SpigotClanAPI.get().findCachedProfile(player.getUniqueId()).orElseThrow();
        Team team = profile.clanProfile().map(Membership::clan).orElse(null);

        // clans
        if (team != null) {
            ItemStack clanItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                    .withName(team.displayName())
                    .withLore(namespace.string(player, "menu.main.clan.lore"))
                    .build();
            bmenu.withItem(clanItem, (e) -> {
                clan(player, team, () -> open(player));
            });
        }

        if (TeamAPI.get().clans().size() > 0) {
            ItemStack clanListItem = ItemStackBuilder.of(Material.BOOK)
                    .withName(namespace.string(player, "menu.main.clanList.name"))
                    .withLore(namespace.string(player, "menu.main.clanList.lore"))
                    .build();
            bmenu.withItem(clanListItem, (e) -> {
                clanList(player, () -> open(player));
            });
        }

        // profiles
        ItemStack profileListItem = ItemStackBuilder.of(Material.PLAYER_HEAD)
                .withName(namespace.string(player, "menu.main.profileList.name"))
                .withLore(namespace.string(player, "menu.main.profileList.lore"))
                .build();
        bmenu.withItem(profileListItem, (e) -> {
            profileList(player, () -> open(player));
        });

        ItemStack profileItem = ItemStackBuilder.skull().withPlayer(player.getPlayerProfile())
                .withName(Component.text(player.getName(), NamedTextColor.WHITE))
                .withLore(namespace.string(player, "menu.main.profile.lore"))
                .build();
        bmenu.withItem(profileItem, (e) -> {
            profile(player, profile, () -> open(player));
        });

        bmenu.build().open(player);
    }

    private static void clanList(Player player, Runnable back) {
        ISpigotPaginatedMenuBuilder bmenu = SpigotBrickGUI.paginatedBuilder();
        setup(bmenu, player, back);

        List<Team> teams = new ArrayList<>(SpigotClanAPI.get().clans());
        bmenu.withTitle(index -> namespace.string(player, "menu.clanList.title", index + 1, teams.size()));

        bmenu.withItems(teams.size(), index -> {
            Team team = teams.get(index);
            ItemStack clanItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                    .withName(team.displayName())
                    .withLore(namespace.string(player, "menu.clanList.clan.lore", team.name()))
                    .build();
            return new SpigotMenuItem(clanItem, c -> clan(player, team, () -> clanList(player, back)));
        });

        bmenu.build().open(player);
    }


    public static void clan(Player player, Team team, Runnable back) {
        boolean any = hasPermission(player, team, ClanPermission.CHANGE_CREST);
        ISpigotMenu bmenu = SpigotBrickGUI.create(any ? 54 : 36, namespace.string(player, "menu.clan.title", team.name()));

        ItemStack infoItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                .withName(team.displayName())
                .withLore(namespace.string(player, "menu.clan.info.lore", format(player, team.createdAt())))
                .build();
        bmenu.setItem(12, infoItem);

        ItemStack membersItem = ItemStackBuilder.skull()
                .withName(namespace.string(player, "menu.clan.members.name", team.memberCount(), team.memberLimit()))
                .withLore(namespace.string(player, "menu.clan.members.lore", team.name()))
                .build();
        bmenu.setItem(14, membersItem, c -> {
            clanMembers(player, team, () -> clan(player, team, back));
        });

        if (!any) {
            bmenu.setItem(31, backItem(player), (c) -> {
                back.run();
            });
            bmenu.open(player);
            return;
        }

        ISpigotMenuRowBuilder row = SpigotBrickGUI.rowBuilder();

        // TODO
//        if (hasPermission(player, clan, ClanPermission.ACCESS_STORAGE)) {
//            ItemStack storageItem = ItemStackBuilder.of(Material.CHEST)
//                    .withName(namespace.string(player, "menu.clan.storage.name"))
//                    .withLore(namespace.string(player, "menu.clan.storage.lore", clan.name()))
//                    .build();
//            row.withItem(storageItem, (e) -> {
//                clanStorage(player, clan, () -> clan(player, clan, back));
//            });
//        }
//
//
//        if (hasPermission(player, clan, ClanPermission.ACCESS_VAULT)) {
//            ItemStack vaultItem = ItemStackBuilder.of(Material.ENDER_CHEST)
//                    .withName(namespace.string(player, "menu.clan.vault.name"))
//                    .withLore(namespace.string(player, "menu.clan.vault.lore", clan.name()))
//                    .build();
//            row.withItem(vaultItem, (e) -> {
//                clanVault(player, clan, () -> clan(player, clan, back));
//            });
//        }

        if (hasPermission(player, team, ClanPermission.CHANGE_CREST)) {
            ItemStack bannerItem = ItemStackBuilder.banner(DyeColor.WHITE)
                    .withName(namespace.string(player, "menu.clan.change-crest.name"))
                    .withLore(namespace.string(player, "menu.clan.change-crest.lore", team.name()))
                    .build();
            row.withItem(bannerItem, (e) -> {
                clanEditCrest(player, team, () -> clan(player, team, back));
            });
        }

        row.fill(bmenu, 3);

        bmenu.setItem(49, backItem(player), (c) -> {
            back.run();
        });

        bmenu.open(player);
    }

    private static void clanStorage(Player player, Team team, Runnable back) {

    }

    private static void clanVault(Player player, Team team, Runnable back) {

    }

    private static void clanMembers(Player player, Team team, Runnable back) {
        ISpigotPaginatedMenuBuilder bmenu = SpigotBrickGUI.paginatedBuilder();
        setup(bmenu, player, back);

        SpigotClanAPI.get().profiles(team).thenAccept(profiles -> {
            bmenu.withTitle(index -> namespace.string(player, "menu.clan.members.title", team.name(), index + 1, profiles.size()));

            bmenu.withItems(profiles.size(), index -> {
                Profile profile = profiles.get(index);
                ItemStack profileItem = ItemStackBuilder.skull().withPlayer(profile.id())
                        .withName(Component.text(profile.name(), NamedTextColor.WHITE))
                        .withLore(namespace.string(player, "menu.clan.members.profile.lore", profile.name()))
                        .build();
                return new SpigotMenuItem(profileItem, c -> {
                    profile(player, profile, () -> clanMembers(player, team, back));
                });
            });

            SpigotClans.scheduler.sync().execute(() -> bmenu.build().open(player));
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    private static void kickMember(Player player, Profile profile, Runnable back) {
        SpigotBrickGUI.confirmationBuilder()
                .withTitle(namespace.string(player, "menu.profile.kick.confirm.title", profile.name()))
                .withDisplay(ItemStackBuilder.skull().withPlayer(profile.id())
                        .withName(Component.text(profile.name(), NamedTextColor.WHITE))
                        .build())
                .withAccept(() -> {
                    player.chat("/clans kick " + profile.name());
                    back.run();
                })
                .withDeny(back)
                .build().open(player);
    }

    private static void clanEditCrest(Player player, Team team, Runnable back) {
        ISpigotMenu menu = SpigotBrickGUI.create(54, namespace.string(player, "menu.clan.change-crest.title", team.name()));

        ItemStack previewItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                .withName(namespace.string(player, "menu.clan.change-crest.preview.name"))
                .build();
        menu.setItem(13, previewItem);

        CrestConfig config = team.crestConfig();

        DyeColor primaryColor = ClanTools.dyeColor(team);
        ItemStack primaryColorItem = ItemStackBuilder.wool(primaryColor)
                .withName(namespace.string(player, "menu.clan.change-crest.primary-color.name"))
                .withLore(namespace.string(player, "menu.clan.change-crest.primary-color.lore", primaryColor.name()))
                .build();
        menu.setItem(28, primaryColorItem, c -> {
            clanEditCrestColor(player, team,
                    () -> clanEditCrest(player, team, back),
                    color -> {
                        team.setColor(color.getColor().asRGB());
                        TeamAPI.get().update(team);
                        clanEditCrest(player, team, back);
                    });
        });

        DyeColor secondaryColor = DyeColor.valueOf(config.color().name());
        ItemStack secondaryColorItem = ItemStackBuilder.wool(secondaryColor)
                .withName(namespace.string(player, "menu.clan.change-crest.secondary-color.name"))
                .withLore(namespace.string(player, "menu.clan.change-crest.secondary-color.lore", secondaryColor.name()))
                .build();
        menu.setItem(30, secondaryColorItem, c -> {
            clanEditCrestColor(player, team,
                    () -> clanEditCrest(player, team, back),
                    color -> {
                        team.setCrestConfig(config.withColor(CrestType.Color.valueOf(color.name())));
                        TeamAPI.get().update(team);
                        clanEditCrest(player, team, back);
                    });
        });

        CrestConfig.ColorTarget target = config.target();
        ItemStack colorToggleItem = ItemStackBuilder.of(target == CrestConfig.ColorTarget.FOREGROUND ? Material.GLOW_INK_SAC : Material.INK_SAC)
                .withName(namespace.string(player, "menu.clan.change-crest.color-toggle.name"))
                .withLore(namespace.string(player, "menu.clan.change-crest.color-toggle.lore", target.name()))
                .build();
        menu.setItem(32, colorToggleItem, (e) -> {
            team.setCrestConfig(config.withTarget(CrestConfig.ColorTarget.values()[(target.ordinal() + 1) % 2]));
            TeamAPI.get().update(team);
            clanEditCrest(player, team, back);
        });

        ItemStack crestTypeItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                .withName(namespace.string(player, "menu.clan.change-crest.type.name"))
                .withLore(namespace.string(player, "menu.clan.change-crest.type.lore"))
                .build();
        menu.setItem(34, crestTypeItem, (e) -> {
            clanEditCrestType(player, team, () -> clanEditCrest(player, team, back));
        });

        menu.setItem(49, backItem(player), c -> {
            back.run();
        });

        menu.open(player);
    }

    private static void clanEditCrestType(Player player, Team team, Runnable back) {
        ISpigotPaginatedMenuBuilder bmenu = SpigotBrickGUI.paginatedBuilder();
        setup(bmenu, player, back);
        bmenu.withTitle(index -> namespace.string(player, "menu.clan.change-crest.type.title", team.name(), index + 1, 1));

        List<CrestTemplate> crestTemplates = new ArrayList<>(SpigotClanAPI.get().crestTemplates());
        crestTemplates.sort(Comparator.<CrestTemplate>comparingInt(c -> c.restricted() ? 1 : 0).thenComparing(CrestTemplate::name));

        bmenu.withItems(crestTemplates.size(), index -> {
            CrestTemplate crestTemplate = crestTemplates.get(index);
            ItemStack item = ItemStackBuilder.of(ClanTools.crest(crestTemplate.type(), team.crestConfig(), ClanTools.dyeColor(team.color())))
                    .withName(Component.text(crestTemplate.name(), NamedTextColor.WHITE))
                    .build();
            return new SpigotMenuItem(item, c -> {
                team.setCrestTemplate(crestTemplate);
                TeamAPI.get().update(team);
                back.run();
            });
        });

        bmenu.build().open(player);
    }

    private static void clanEditCrestColor(Player player, Team team, Runnable back, Consumer<DyeColor> callback) {
        ISpigotMenu menu = SpigotBrickGUI.create(54, namespace.string(player, "menu.clan.change-crest.color.title", team.name()));

        int index = 10;
        for (DyeColor color : DyeColor.values()) {
            menu.setItem(index, ItemStackBuilder.wool(color)
                    .withName(color.name().charAt(0) + color.name().toLowerCase()
                            .substring(1).replace("_", " "))
                    .build(), c -> {
                callback.accept(color);
            });

            index++;
            if ((index + 1) % 9 == 0) {
                index += 2;
            }
        }

        menu.setItem(49, backItem(player), c -> {
            back.run();
        });

        menu.open(player);
    }

    private static void profileList(Player player, Runnable back) {
        ISpigotPaginatedMenuBuilder bmenu = SpigotBrickGUI.paginatedBuilder();
        setup(bmenu, player, back);

        List<Profile> profiles = new ArrayList<>(SpigotClanAPI.get().cachedProfiles());
        bmenu.withTitle(index -> namespace.string(player, "menu.profileList.title", index + 1, profiles.size()));

        bmenu.withItems(profiles.size(), index -> {
            Profile profile = profiles.get(index);
            ItemStack profileItem = ItemStackBuilder.skull().withPlayer(profile.id())
                    .withName(Component.text(profile.name(), NamedTextColor.WHITE))
                    .withLore(namespace.string(player, "menu.profileList.profile.lore", profile.name()))
                    .build();
            return new SpigotMenuItem(profileItem, c -> {
                profile(player, profile, () -> profileList(player, back));
            });
        });

        bmenu.build().open(player);
    }

    private static String format(Player player, Instant time) {
        return time.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    }

    private static String format(Player player, Duration duration) {
        String result = duration.toString().substring(2);
        int index = result.indexOf(".");
        if (index > 0) {
            result = result.substring(0, index);
        }

        result = result.replace("H", "h ");
        result = result.replace("M", "m ");
        result = result.replace("S", "s");
        return result;
    }

    public static void profile(Player player, Profile target, Runnable back) {
        ISpigotMenuBuilder bmenu = SpigotBrickGUI.builder();
        bmenu.withTitle(namespace.string(player, "menu.profile.title", target.name()));

        ItemStack infoItem = ItemStackBuilder.skull().withPlayer(target.id())
                .withName(Component.text(target.name(), NamedTextColor.WHITE))
                .withLore(namespace.string(player, "menu.profile.info.lore",
                        format(player, target.createdAt())))
                .build();
        bmenu.withItem(infoItem);

        Team team = target.clanProfile().map(Membership::clan).orElse(null);
        if (team != null) {
            ItemStack clanItem = ItemStackBuilder.of(SpigotClanAPI.get().crest(team))
                    .withName(team.displayName())
                    .withLore(namespace.string(player, "menu.profile.clan.lore", team.name()))
                    .build();
            bmenu.withItem(clanItem, c -> {
                clan(player, team, () -> profile(player, target, back));
            });

            if (hasPermission(player, team, ClanPermission.KICK_MEMBERS) && !target.id().equals(player.getUniqueId())) {
                ItemStack kickItem = ItemStackBuilder.of(Material.REDSTONE_BLOCK)
                        .withName(namespace.string(player, "menu.profile.kick.name"))
                        .withLore(namespace.string(player, "menu.profile.kick.lore", target.name()))
                        .build();
                bmenu.withItem(kickItem, c -> {
                    kickMember(player, target, () -> profile(player, target, back));
                });
            }

            // TODO
//            ClanProfile cp = ClanTools.clanProfile(player).orElse(null);
//            if (cp != null && cp.isLeader() && !target.id().equals(player.getUniqueId())) {
//                ItemStack changePermissionItem = ItemStackBuilder.of(Material.COMMAND_BLOCK)
//                        .withName(namespace.string(player, "menu.profile.change-permissions.name"))
//                        .withLore(namespace.string(player, "menu.profile.change-permissions.lore", target.name()))
//                        .build();
//                bmenu.withItem(changePermissionItem, c -> {
//                    changePlayerPermissions(player, cp, () -> profile(player, target, back));
//                });
//            }
        } else {
            Team pclan = SpigotClanAPI.get().findClan(player).orElse(null);
            if (pclan != null && hasPermission(player, pclan, ClanPermission.INVITE_PLAYERS)) {
                ItemStack inviteItem = ItemStackBuilder.of(Material.WRITABLE_BOOK)
                        .withName(namespace.string(player, "menu.profile.invite.name"))
                        .withLore(namespace.string(player, "menu.profile.invite.lore", target.name()))
                        .build();
                bmenu.withItem(inviteItem, c -> {
                    invitePlayer(player, target, () -> profile(player, target, back));
                });
            }
        }

        bmenu.withHotbarItem(4, backItem(player), c -> {
            back.run();
        });

        bmenu.build().open(player);
    }

    private static void invitePlayer(Player player, Profile profile, Runnable back) {
        SpigotBrickGUI.confirmationBuilder()
                .withTitle(namespace.string(player, "menu.profile.invite.confirm.title", profile.name()))
                .withDisplay(ItemStackBuilder.skull().withPlayer(profile.id())
                        .withName(Component.text(profile.name(), NamedTextColor.WHITE))
                        .build())
                .withAccept(() -> {
                    player.chat("/clans invite " + profile.name());
                    back.run();
                })
                .withDeny(back)
                .build().open(player);
    }

    private static final Map<ClanPermission, Material> materials = Map.of(
            ClanPermission.CHANGE_CREST, Material.WHITE_BANNER,
            ClanPermission.INVITE_PLAYERS, Material.PLAYER_HEAD,
            ClanPermission.KICK_MEMBERS, Material.IRON_SWORD
    );

    public static void changePlayerPermissions(Player player, Membership cp, Runnable back) {
        ISpigotMenu menu = SpigotBrickGUI.create(45, namespace.string(player, "menu.clan.permissions.title", cp.profile().name()));

        int index = 11;
        for (ClanPermission perm : materials.keySet()) {
            String statusKey = "menu.clan.permissions." + cp.hasPermission(perm);
            ItemStack item = ItemStackBuilder.of(materials.get(perm))
//                    .withName(perm.display()) // TODO
                    .withLore(namespace.string(player, "menu.clan.permissions.status", namespace.string(player, statusKey)))
                    .apply(cp.hasPermission(perm), b -> {
                        b.withEnchantment(Enchantment.SILK_TOUCH);
                        b.withItemFlag(ItemFlag.HIDE_ENCHANTS);
                    })
                    .build();

            menu.setItem(index, item, click -> {
                if (cp.hasPermission(perm)) {
                    cp.removePermission(perm);
                } else {
                    cp.addPermission(perm);
                }
                TeamAPI.get().update(cp);
                changePlayerPermissions(player, cp, back);
            });

            index++;
            if ((index + 2) % 9 == 0) {
                index += 4;
            }
        }

        menu.setItem(40, backItem(player), c -> {
            back.run();
        });

        menu.open(player);
    }

}

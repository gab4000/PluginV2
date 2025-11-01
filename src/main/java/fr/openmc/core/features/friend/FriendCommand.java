package fr.openmc.core.features.friend;

import fr.openmc.core.commands.autocomplete.OnlinePlayerAutoComplete;
import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.friend.autocomplete.FriendsAutoComplete;
import fr.openmc.core.features.friend.autocomplete.FriendsRequestAutoComplete;
import fr.openmc.core.features.settings.PlayerSettingsManager;
import fr.openmc.core.utils.cache.CacheOfflinePlayer;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;

import javax.annotation.Syntax;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Command({"friends", "friend", "ami", "f"})
public class FriendCommand {

    @Subcommand("add")
    @Description("Envoyer une demande d'ami")
    public void addCommand(
            Player player,
            @Named("joueur") @SuggestWith(OnlinePlayerAutoComplete.class) Player target
    ) {
        try {
            if (player.getUniqueId().equals(target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous ne pouvez pas vous ajouter vous-même en ami."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            if (!PlayerSettingsManager.canReceiveFriendRequest(target.getUniqueId(), player.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cCe joueur a désactivé les demandes d'amis."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            if (FriendManager.isRequestPending(target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous avez déjà envoyé une demande à ce joueur."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            if (FriendManager.areFriends(player.getUniqueId(), target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous êtes déjà ami de ce joueur."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            FriendManager.addRequest(player.getUniqueId(), target.getUniqueId());
            MessagesManager.sendMessage(player, Component.text("§aDemande d'ami envoyée à §e" + target.getName() + "§a."), Prefix.FRIEND, MessageType.INFO, true);

            Component acceptButton = Component.text(" [Accepter]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/friend accept " + player.getName()))
                    .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour accepter la demande d'ami", NamedTextColor.GREEN)));

            Component ignoreButton = Component.text(" [Ignorer]", NamedTextColor.GRAY)
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (!FriendManager.isRequestPending(player.getUniqueId())) {
                            MessagesManager.sendMessage(target, Component.text("§cLa demande d'ami a expiré."), Prefix.FRIEND, MessageType.INFO, true);
                            return;
                        }
                        FriendManager.removeRequest(FriendManager.getRequest(player.getUniqueId()));
                        MessagesManager.sendMessage(target, Component.text("§cVous avez ignoré la demande d'ami de §e" + player.getName() + "§c."), Prefix.FRIEND, MessageType.INFO, true);
                    }))
                    .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour ignorer la demande d'ami", NamedTextColor.GRAY)));

            Component denyButton = Component.text(" [Refuser]", NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand("/friend deny " + player.getName()))
                    .hoverEvent(HoverEvent.showText(Component.text("Cliquez pour refuser la demande d'ami", NamedTextColor.RED)));

            MessagesManager.sendMessage(target, Component.text("§e" + player.getName() + " §avous a envoyé une demande d'ami.").append(acceptButton).append(ignoreButton).append(denyButton), Prefix.FRIEND, MessageType.INFO, true);
        } catch (Exception e) {
            MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors de l'envoi de la demande d'ami."), Prefix.FRIEND, MessageType.ERROR, true);
            throw new RuntimeException(e);
        }
    }

    @Subcommand("remove")
    @Description("Supprimer un ami de votre liste")
    public void removeCommand(
            Player player,
            @Named("ami") @SuggestWith(FriendsAutoComplete.class) String targetName
    ) {
        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (!target.hasPlayedBefore()) {
                MessagesManager.sendMessage(player, Component.text("§cCe joueur n'existe pas."), Prefix.OPENMC, MessageType.ERROR, true);
                return;
            }
            if (!FriendManager.areFriends(player.getUniqueId(), target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous n'êtes pas amis avec ce joueur."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            if (!FriendManager.removeFriend(player.getUniqueId(), target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors de la suppression de l'ami."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            MessagesManager.sendMessage(player, Component.text("§aVous avez supprimé §e" + target.getName() + " §ade votre liste d'amis."), Prefix.FRIEND, MessageType.INFO, true);
            if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                MessagesManager.sendMessage(targetPlayer, Component.text("§cVous avez été supprimé de la liste d'amis de §e" + player.getName() + "§c."), Prefix.FRIEND, MessageType.INFO, true);
            }
        } catch (Exception e) {
            MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors de la suppression de l'ami."), Prefix.FRIEND, MessageType.ERROR, true);
            throw new RuntimeException(e);
        }
    }

    @Subcommand("list")
    @Description("Afficher la liste de vos amis")
    @Syntax("[page]")
    public void listCommand(
            Player player,
            @Named("page") @Optional Integer page
    ) {
        int currentPage = (page != null && page > 0) ? page : 1;
        final int ITEMS_PER_PAGE = 7;

        FriendManager.getFriendsAsync(player.getUniqueId()).thenAccept(friends -> {
            if (friends.isEmpty()) {
                MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas d'amis pour le moment."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }

            List<UUID> friendsList = new ArrayList<>(friends);
            int totalPages = (int) Math.ceil((double) friendsList.size() / ITEMS_PER_PAGE);

            if (currentPage > totalPages) {
                MessagesManager.sendMessage(player, Component.text("§cLa page " + currentPage + " n'existe pas. Total: " + totalPages + " pages"), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }

            int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, friendsList.size());

            Component header = Component.text("  ✦ Liste d'amis (" + currentPage + "/" + totalPages + ") ✦  ")
                    .color(NamedTextColor.GOLD);
            player.sendMessage(header);
            for (int i = startIndex; i < endIndex; i++) {
                UUID friendUUID = friendsList.get(i);
                OfflinePlayer friend = CacheOfflinePlayer.getOfflinePlayer(friendUUID);

                try {
                    Timestamp timestamp = FriendManager.getTimestamp(player.getUniqueId(), friend.getUniqueId());
                    String formattedDate = getFormattedDate(timestamp);

                    boolean isOnline = friend.isOnline();

                    City city = CityManager.getPlayerCity(friend.getUniqueId());
                    String formattedMoney = EconomyManager.getFormattedBalance(friend.getUniqueId());

                    TextComponent friendComponent = Component.text("  " + (i+1) + ". ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text(friend.getName())
                                    .color(isOnline ? NamedTextColor.GREEN : NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.BOLD, isOnline))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("§7Vile : §e" + (city != null ? city.getName() : "Aucune") +
                                            "\n§7Argent : §e" + formattedMoney +
                                            "\n§7Statut : " + (isOnline ? "§aEn ligne" : "§cHors ligne")
                                    )))
                            ;

                    Component statusIcon = isOnline
                            ? Component.text(" ⬤ ").color(NamedTextColor.GREEN)
                            : Component.text(" ⬤ ").color(NamedTextColor.RED);
                    
                    Component dateInfo = Component.text("Depuis le " + formattedDate)
                            .color(NamedTextColor.GRAY);

                    Component actions = Component.text(" [✖]")
                            .color(NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/friends remove " + friend.getName()))
                            .hoverEvent(HoverEvent.showText(Component.text("Supprimer cet ami", NamedTextColor.RED)));

                    player.sendMessage(friendComponent.append(statusIcon).append(dateInfo).append(actions));

                } catch (Exception e) {
                    player.sendMessage(Component.text("Erreur lors de la récupération des informations de " + friend.getName())
                            .color(NamedTextColor.RED));
                    throw new RuntimeException(e);
                }
            }

            if (totalPages > 1) {
                Component navigation = Component.empty();

                if (currentPage > 1) {
                    navigation = navigation.append(
                            Component.text(" « Page précédente ")
                                    .color(NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.runCommand("/friends list " + (currentPage - 1)))
                                    .hoverEvent(HoverEvent.showText(Component.text("Page " + (currentPage - 1))))
                    );
                } else {
                    navigation = navigation.append(
                            Component.text(" « Page précédente ")
                                    .color(NamedTextColor.DARK_GRAY)
                    );
                }

                navigation = navigation.append(Component.text(" | ").color(NamedTextColor.GRAY));

                if (currentPage < totalPages) {
                    navigation = navigation.append(
                            Component.text("Page suivante » ")
                                    .color(NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.runCommand("/friends list " + (currentPage + 1)))
                                    .hoverEvent(HoverEvent.showText(Component.text("Page " + (currentPage + 1))))
                    );
                } else {
                    navigation = navigation.append(
                            Component.text("Page suivante » ")
                                    .color(NamedTextColor.DARK_GRAY)
                    );
                }

                player.sendMessage(navigation);
            }
        }).exceptionally(ex -> {
            MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors de la récupération de vos amis."), Prefix.FRIEND, MessageType.ERROR, true);
            throw new RuntimeException(ex);
        });
    }

    @Subcommand("accept")
    @Description("Accepter une demande d'ami")
    public void acceptCommand(
            Player player,
            @Named("ami") @SuggestWith(FriendsRequestAutoComplete.class) String targetName
    ) {
        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (!target.hasPlayedBefore()) {
                MessagesManager.sendMessage(player, Component.text("§cCe joueur n'existe pas."), Prefix.OPENMC, MessageType.ERROR, true);
                return;
            }
            if (!FriendManager.isRequestPending(target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas reçu de demande d'ami de ce joueur."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            FriendManager.addFriend(player.getUniqueId(), target.getUniqueId());
            MessagesManager.sendMessage(player, Component.text("§aVous êtes désormais ami avec §e" + target.getName() + "§a."), Prefix.FRIEND, MessageType.INFO, true);
            if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                MessagesManager.sendMessage(targetPlayer, Component.text("§aVous êtes désormais ami avec §e" + player.getName() + "§a."), Prefix.FRIEND, MessageType.INFO, true);
            }
        } catch (Exception e) {
            MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors de l'acceptation de la demande d'ami."), Prefix.FRIEND, MessageType.ERROR, true);
            throw new RuntimeException(e);
        }
    }

    @Subcommand("deny")
    @Description("Refuser une demande d'ami")
    public void denyCommand(
            Player player,
            @Named("demande d'ami") @SuggestWith(FriendsRequestAutoComplete.class) String targetName
    ) {
        try {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            if (!target.hasPlayedBefore()) {
                MessagesManager.sendMessage(player, Component.text("§cCe joueur n'existe pas."), Prefix.OPENMC, MessageType.ERROR, true);
                return;
            }
            if (!FriendManager.isRequestPending(target.getUniqueId())) {
                MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas reçu de demande d'ami de ce joueur."), Prefix.FRIEND, MessageType.ERROR, true);
                return;
            }
            FriendManager.removeRequest(FriendManager.getRequest(target.getUniqueId()));
            MessagesManager.sendMessage(player, Component.text("§cVous avez refusé la demande d'ami de §e" + target.getName() + "§c."), Prefix.FRIEND, MessageType.INFO, true);
            if (target instanceof Player targetPlayer && targetPlayer.isOnline()) {
                MessagesManager.sendMessage(targetPlayer, Component.text("§cVotre demande d'ami a été refusée par §e" + player.getName() + "§c."), Prefix.FRIEND, MessageType.INFO, true);
            }
        } catch (Exception e) {
            MessagesManager.sendMessage(player, Component.text("§cUne erreur est survenue lors du refus de la demande d'ami."), Prefix.FRIEND, MessageType.ERROR, true);
            throw new RuntimeException(e);
        }
    }

    public String getFormattedDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(timestamp.getTime());
        return sdf.format(date);
    }
}

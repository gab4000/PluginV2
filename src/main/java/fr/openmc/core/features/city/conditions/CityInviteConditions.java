package fr.openmc.core.features.city.conditions;

import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.CityPermission;
import fr.openmc.core.features.city.commands.CityInviteCommands;
import fr.openmc.core.features.city.sub.milestone.rewards.MemberLimitRewards;
import fr.openmc.core.features.settings.PlayerSettingsManager;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Le but de cette classe est de regrouper toutes les conditions necessaires
 * pour inviter une personne (utile pour faire une modif sur menu et commandes).
 */
public class CityInviteConditions {
	
	/**
	 * Retourne un booleen pour dire si le joueur peut etre invité
	 *
	 * @param city la ville sur laquelle on fait les actions
	 * @param player le joueur sur lequel tester les permissions
	 * @param target le joueur sur lequel tester s'il peut etre inviter
	 * @return booleen
	 */
	public static boolean canCityInvitePlayer(City city, Player player, Player target) {
		if (city == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NO_CITY.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}
		
		if (!(city.hasPermission(player.getUniqueId(), CityPermission.INVITE))) {
			MessagesManager.sendMessage(player, Component.text("Tu n'as pas la permission d'inviter des joueurs dans la ville"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}
		
		if (CityManager.getPlayerCity(target.getUniqueId()) != null) {
			MessagesManager.sendMessage(player, Component.text("Cette personne est déjà dans une ville"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}

		if (!PlayerSettingsManager.canReceiveCityInvite(player.getUniqueId(), target.getUniqueId())) {
			MessagesManager.sendMessage(player, Component.text("§cCette personne ne peut pas recevoir d'invitation"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}

        if (city.getMembers().size() >= MemberLimitRewards.getMemberLimit(city.getLevel())) {
	        MessagesManager.sendMessage(player, Component.text("§cVous avez atteint la limite de membre qui est de §3" + MemberLimitRewards.getMemberLimit(city.getLevel()) + "§f, Améliorez votre ville au niveau supérieur !"), Prefix.CITY, MessageType.ERROR, false);
            return false;
        }

		return true;
	}
	
	/**
	 * Retourne un booleen pour dire si le joueur peut refuser l'invitation
	 *
	 * @param player le joueur sur lequel tester les permissions
	 * @return booleen
	 */
	public static boolean canCityInviteDeny(Player player, Player inviter) {
		List<Player> playerInvitations = CityInviteCommands.invitations.get(player);

		if (playerInvitations == null) {
			MessagesManager.sendMessage(player, Component.text("Tu n'as aucune invitation en attente"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}

		if (!playerInvitations.contains(inviter)) {
			MessagesManager.sendMessage(player, Component.text(inviter.getName() + " ne vous a pas invité"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Retourne un booleen pour dire si le joueur peut etre invité
	 *
	 * @param newCity la ville sur laquelle on fait les actions
	 * @param inviter le joueur qui invite
	 * @param invitedPlayer le joueur qui est invité
	 * @return booleen
	 */
	public static boolean canCityInviteAccept(City newCity, Player inviter, Player invitedPlayer) {
		if (!CityInviteCommands.invitations.containsKey(invitedPlayer)) {
			MessagesManager.sendMessage(invitedPlayer, Component.text("Tu n'as aucune invitation en attente"), Prefix.CITY, MessageType.ERROR, false);
			return false;
		}
		
		if (newCity == null) {
			MessagesManager.sendMessage(invitedPlayer, Component.text("L'invitation a expiré"), Prefix.CITY, MessageType.ERROR, false);

			List<Player> playerInvitations = CityInviteCommands.invitations.get(invitedPlayer);
			playerInvitations.remove(inviter);
			if (playerInvitations.isEmpty()) {
				CityInviteCommands.invitations.remove(invitedPlayer);
			}
			return false;
		}

        if (newCity.getMembers().size() >= MemberLimitRewards.getMemberLimit(newCity.getLevel())) {
            MessagesManager.sendMessage(invitedPlayer, Component.text("La ville a atteint sa limite de membre"), Prefix.CITY, MessageType.ERROR, false);

            List<Player> playerInvitations = CityInviteCommands.invitations.get(invitedPlayer);
            playerInvitations.remove(inviter);
            if (playerInvitations.isEmpty()) {
                CityInviteCommands.invitations.remove(invitedPlayer);
            }
            return false;
        }
		
		return true;
	}
}

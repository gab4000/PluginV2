package fr.openmc.core.features.city.sub.rank;

import fr.openmc.api.input.DialogInput;
import fr.openmc.api.menulib.template.ConfirmMenu;
import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.CityPermission;
import fr.openmc.core.features.city.models.DBCityRank;
import fr.openmc.core.features.city.sub.milestone.rewards.FeaturesRewards;
import fr.openmc.core.features.city.sub.rank.menus.CityRankDetailsMenu;
import fr.openmc.core.features.city.sub.rank.menus.CityRankMemberMenu;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class CityRankAction {
	private static final int MAX_LENGTH_RANK_NAME = 16;
	
	/**
	 * Begin the process to create a new rank.
	 *
	 * @param player The player who wants to create a rank.
	 */
	public static void beginCreateRank(Player player) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (!CityRankCondition.canCreateRank(city, player)) return;
		
		DialogInput.send(player, Component.text("Entrez le nom de votre grade"), MAX_LENGTH_RANK_NAME, input -> {
					if (input == null) return;
					
					CityRankAction.afterCreateRank(player, input);
				}
		);
	}
	
	/**
	 * After the player has entered the rank name, open the rank details menu.
	 *
	 * @param player   The player who wants to create a rank.
	 * @param rankName The name of the rank to create.
	 */
	public static void afterCreateRank(Player player, String rankName) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (!CityRankCondition.canCreateRank(city, player)) return;
		
		if (city.isRankExists(rankName)) {
			MessagesManager.sendMessage(player, MessagesManager.Message.CITY_RANKS_ALREADY_EXIST.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		
		new CityRankDetailsMenu(player, city, rankName).open();
	}
	
	/**
	 * Rename a rank from the menu.
	 *
	 * @param player  The player who wants to rename a rank.
	 * @param oldRank The old rank to pass.
	 * @param newRank The new rank to rename.
	 */
	public static void renameRankFromMenu(Player player, DBCityRank oldRank, DBCityRank newRank) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (city == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NO_CITY.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		if (oldRank == null || newRank == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.CITY_RANKS_NOT_EXIST.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		if (!CityRankCondition.canRenameRank(city, player, newRank.getName())) return;
		DialogInput.send(player, Component.text("Entrez le nouveau nom de votre grade"), MAX_LENGTH_RANK_NAME, input -> {
			if (input == null) return;
			
			if (!CityRankCondition.canRenameRank(city, player, newRank.getName())) return;
			
			new CityRankDetailsMenu(player, city, oldRank, new DBCityRank(newRank.getRankUUID(), newRank.getCityUUID(), newRank.getPriority(), input, newRank.getIcon(), newRank.getPermissionsSet(), newRank.getMembersSet())).open();
		});
	}
	
	/**
	 * Rename a rank.
	 *
	 * @param player  The player who wants to rename a rank.
	 * @param oldName The old name of the rank to rename.
	 */
	public static void renameRank(Player player, String oldName) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (!CityRankCondition.canRenameRank(city, player, oldName)) {
			return;
		}
		
		DialogInput.send(player, Component.text("Entrez le nouveau nom de votre grade"), MAX_LENGTH_RANK_NAME, input -> {
			if (input == null) return;
			
			if (!CityRankCondition.canRenameRank(city, player, oldName)) {
				return;
			}
			
			DBCityRank rank = city.getRankByName(oldName);
			if (rank == null) {
				MessagesManager.sendMessage(player, MessagesManager.Message.CITY_RANKS_NOT_EXIST.getMessage(), Prefix.CITY, MessageType.ERROR, false);
				return;
			}
			
			city.updateRank(rank, new DBCityRank(rank.getRankUUID(), city.getUniqueId(), rank.getPriority(), input, rank.getIcon(), rank.getPermissionsSet(), rank.getMembersSet()));
			MessagesManager.sendMessage(player, Component.text("Le nom du grade a été mis à jour : " + oldName + " → " + input), Prefix.CITY, MessageType.SUCCESS, false);
		});
	}
	
	/**
	 * Delete a rank.
	 *
	 * @param player   The player who wants to delete a rank.
	 * @param rankName The name of the rank to delete.
	 */
	public static void deleteRank(Player player, String rankName) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (city == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NO_CITY.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		
		if ( CityRankCondition.canDeleteRank(city, player, rankName)) {
			return;
		}
		
		DBCityRank rank = city.getRankByName(rankName);
		if (rank == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.CITY_RANKS_NOT_EXIST.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		
		new ConfirmMenu(player, () -> {
			try {
				if (!CityRankCondition.canDeleteRank(city, player, rankName)) {
					return;
				}
				
				city.deleteRank(rank);
				player.closeInventory();
				MessagesManager.sendMessage(player, Component.text("Grade " + rank.getName() + " supprimé avec succès !"), Prefix.CITY, MessageType.SUCCESS, false);
			} catch (IllegalArgumentException e) {
				MessagesManager.sendMessage(player, Component.text("Impossible de supprimer le grade : " + e.getMessage()), Prefix.CITY, MessageType.ERROR, false);
			}
		}, () -> {
			if (!CityRankCondition.canDeleteRank(city, player, rankName)) return;
			
			new CityRankDetailsMenu(player, city, rank).open();
		}, List.of(Component.text("§cCette action est irréversible")), List.of()).open();
	}
	
	/**
	 * Assign a rank to a member.
	 *
	 * @param player   The player who is assigning the rank.
	 * @param rankName The name of the rank to assign.
	 * @param member   The member to assign the rank to.
	 */
	public static void assignRank(Player player, String rankName, OfflinePlayer member) {
		City city = CityManager.getPlayerCity(player.getUniqueId());
		if (city == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NO_CITY.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		
		if (!FeaturesRewards.hasUnlockFeature(city, FeaturesRewards.Feature.RANK)) {
			MessagesManager.sendMessage(player, Component.text("Vous n'avez pas débloqué cette Feature ! Veuillez Améliorer votre Ville au niveau " + FeaturesRewards.getFeatureUnlockLevel(FeaturesRewards.Feature.RANK) + "!"), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		
		if (!city.hasPermission(player.getUniqueId(), CityPermission.ASSIGN_RANKS)) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NO_ACCESS_PERMS.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		DBCityRank rank = city.getRankByName(rankName);
		if (member == null && rank == null) {
			new CityRankMemberMenu(player, city).open();
			return;
		} else if (member == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.PLAYER_NOT_FOUND.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		} else if (rank == null) {
			MessagesManager.sendMessage(player, MessagesManager.Message.CITY_RANKS_NOT_EXIST.getMessage(), Prefix.CITY, MessageType.ERROR, false);
			return;
		}
		if (!CityRankCondition.canModifyRankPermissions(city, player, rank.getPriority())) {
			return;
		}
		
		city.changeRank(player, member.getUniqueId(), rank);
	}
}
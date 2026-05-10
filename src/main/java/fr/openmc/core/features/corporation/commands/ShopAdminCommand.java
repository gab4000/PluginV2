package fr.openmc.core.features.corporation.commands;

import fr.openmc.core.features.corporation.ShopFurniture;
import fr.openmc.core.features.corporation.commands.autocomplete.ShopAdminCommandPlayerAutocomplete;
import fr.openmc.core.features.corporation.manager.PlayerShopManager;
import fr.openmc.core.features.corporation.manager.ShopManager;
import fr.openmc.core.features.corporation.models.Shop;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("shopadmin")
@CommandPermission("omc.admins.commands.shop")
public class ShopAdminCommand {
	
	@Subcommand("multiblock set")
	public void setShopMultiblock(Player player, @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop, @Named("barrelLoc") Location barrelLoc) {
		if (playerShop == null) return;
		
		Shop shop = ShopManager.getPlayerShop(playerShop.getUniqueId());
		if (shop == null) {
			MessagesManager.sendMessage(player, Component.text("§cCe joueur n'a pas de shop, ou n'a pas été trouvé."), Prefix.SHOP, MessageType.ERROR, false);
			return;
		}
		
		Location cashLoc = barrelLoc.clone().add(0, 1, 0);
		
		Shop.Multiblock multiblock = shop.getMultiblock();
		if (multiblock == null) {
			multiblock = new Shop.Multiblock(barrelLoc, cashLoc);
			if (!shop.setMultiblock(multiblock)) {
				MessagesManager.sendMessage(player, Component.text("§cImpossible de poser le multiblock car un des blocks n'est pas valide."), Prefix.SHOP, MessageType.ERROR, false);
				return;
			}
			MessagesManager.sendMessage(player, Component.text("§aMultiblock associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
			return;
		}
		
		Location shopBarrelLoc = multiblock.stockBlockLoc();
		if (shopBarrelLoc != null) {
			if (shopBarrelLoc == barrelLoc) {
				MessagesManager.sendMessage(player, Component.text("§cCe barrel est déjà associé au shop."), Prefix.SHOP, MessageType.WARNING, false);
			} else {
				if (shopBarrelLoc.getBlock().getType() != Material.BARREL) {
					MessagesManager.sendMessage(player, Component.text("§cAucune barrel détecté."), Prefix.SHOP, MessageType.ERROR, false);
					return;
				}
				shopBarrelLoc.set(barrelLoc.x(), barrelLoc.y(), barrelLoc.z());
				MessagesManager.sendMessage(player, Component.text("§aBarrel associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
			}
		} else {
			shopBarrelLoc = barrelLoc;
			MessagesManager.sendMessage(player, Component.text("§aBarrel associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
		}
		
		Location shopCashLoc = multiblock.cashBlockLoc();
		if (shopCashLoc != null) {
			if (shopCashLoc == cashLoc) {
				MessagesManager.sendMessage(player, Component.text("§cCe cash est déjà associé au shop."), Prefix.SHOP, MessageType.WARNING, false);
			} else {
				if (shopCashLoc.getBlock().getType() != Material.OAK_SIGN && !ShopFurniture.hasFurniture(shopCashLoc.getBlock())) {
					MessagesManager.sendMessage(player, Component.text("§cAucune cash ou panneau détecté."), Prefix.SHOP, MessageType.ERROR, false);
					return;
				}
				shopCashLoc.set(cashLoc.x(), cashLoc.y(), cashLoc.z());
				MessagesManager.sendMessage(player, Component.text("§aCash associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
			}
		} else {
			shopCashLoc = cashLoc;
			MessagesManager.sendMessage(player, Component.text("§aCash associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
		}
		
		shop.setMultiblock(new Shop.Multiblock(shopBarrelLoc, shopCashLoc));
		MessagesManager.sendMessage(player, Component.text("§aMultiblock associé au shop."), Prefix.SHOP, MessageType.SUCCESS, false);
	}
	
	@Subcommand("database query shops")
	public void databaseQueryShops(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
		if (playerShop == null) {
			if (ShopManager.loadShops()) {
				MessagesManager.sendMessage(player, Component.text("§aChargement des shops depuis la base de données réussie"), Prefix.SHOP, MessageType.SUCCESS, false);
			} else {
				MessagesManager.sendMessage(player, Component.text("§cÉchec du chargement des shops depuis la base de données (Error in console)"), Prefix.SHOP, MessageType.ERROR, false);
			}
		} else {
			if (ShopManager.loadShopFor(playerShop)) {
				MessagesManager.sendMessage(player, Component.text("§aChargement du shop du joueur §6" + playerShop.getName() + "§a depuis la base de données réussie"), Prefix.SHOP, MessageType.SUCCESS, false);
			} else {
				MessagesManager.sendMessage(player, Component.text("§cÉchec du chargement du shop du joueur §6" + playerShop.getName() + "§c depuis la base de données (Error in console)"), Prefix.SHOP, MessageType.ERROR, false);
			}
		}
	}
	
	@Subcommand("database query shop_sales")
	public void databaseQueryShopSales(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
	
	}
	
	@Subcommand("database query shop_items")
	public void databaseQueryShopItems(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
	
	}
	
	@Subcommand("database save shops")
	public void databaseSaveShops(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
		if (playerShop == null) {
			if (ShopManager.saveShops()) {
				MessagesManager.sendMessage(player, Component.text("§aSauvegarde des shops dans la base de données réussie"), Prefix.SHOP, MessageType.SUCCESS, false);
			} else {
				MessagesManager.sendMessage(player, Component.text("§cÉchec de la sauvegarde des shops dans la base de données (Error in console)"), Prefix.SHOP, MessageType.ERROR, false);
			}
		} else {
			if (ShopManager.saveShopFor(playerShop)) {
				MessagesManager.sendMessage(player, Component.text("§aSauvegarde du shop du joueur §6" + playerShop.getName() + "§a dans la base de données réussie"), Prefix.SHOP, MessageType.SUCCESS, false);
			} else {
				MessagesManager.sendMessage(player, Component.text("§cÉchec de la sauvegarde du shop du joueur §6" + playerShop.getName() + "§c dans la base de données (Error in console)"), Prefix.SHOP, MessageType.ERROR, false);
			}
		}
	}
	
	@Subcommand("database save shop_sales")
	public void databaseSaveShopSales(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
	
	}
	
	@Subcommand("database save shop_items")
	public void databaseSaveShopItems(Player player, @Optional @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer playerShop) {
	
	}
	
	@Subcommand("removeshop")
	public void removeShop(Player player, @Named("playerShop") @SuggestWith(ShopAdminCommandPlayerAutocomplete.class) OfflinePlayer target) {
		if (target == null) {
			MessagesManager.sendMessage(player, Component.text("§cLe joueur spécifié est introuvable"), Prefix.SHOP, MessageType.ERROR, false);
			return;
		}
		
		if (!ShopManager.hasShop(target.getUniqueId())) {
			MessagesManager.sendMessage(player, Component.text("§cLe joueur spécifié n'a pas de shop"), Prefix.SHOP, MessageType.ERROR, false);
			return;
		}
		
		Shop shop = ShopManager.getPlayerShop(target.getUniqueId());
		if (shop == null) {
			MessagesManager.sendMessage(player, Component.text("§cLe shop du joueur spécifié est introuvable"), Prefix.SHOP, MessageType.ERROR, false);
			return;
		}
		
		if (shop.getItem().getAmount() > 0) {
			MessagesManager.sendMessage(player, Component.text("§cLe shop du joueur spécifié n'est pas vide"), Prefix.SHOP, MessageType.ERROR, false);
			return;
		}
		
		PlayerShopManager.adminDeleteShop(target, player);
	}
}

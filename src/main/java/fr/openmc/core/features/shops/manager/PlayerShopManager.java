package fr.openmc.core.features.shops.manager;

import fr.openmc.api.input.location.ItemInteraction;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.bootstrap.integration.OMCLogger;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.models.Shop;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import fr.openmc.core.utils.world.WorldUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerShopManager {


    /**
     * Create a shop if the player has enough money and does not already have one
     *
     * @param player the player who creates it
     */
    public static void startCreatingShop(Player player) {
        if (!EconomyManager.withdrawBalance(player.getUniqueId(), 500)) {
			MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas assez d'argent pour créer un shop (500 " + EconomyManager.getEconomyIcon() + " requis)"), Prefix.SHOP, MessageType.ERROR, true);
			return;
        }
        
        ItemInteraction.runLocationInteraction(
                player,
                new ItemStack(Material.BARREL),
                "shop:shop_creator",
                300,
                Component.text("Vous avez reçu un tonneau pour poser votre shop"),
                Component.text("§cCréation de shop annulée"),
                location -> {
                    if (location == null) return false;
	                return createShop(player, location);
                },
                () -> {
                    EconomyManager.addBalance(player.getUniqueId(), 500, "Annulation création shop");
                    MessagesManager.sendMessage(player, Component.text("§cVous avez été remboursé de 500 " + EconomyManager.getEconomyIcon() + " §cpour l'annulation de la création de votre shop"), Prefix.SHOP, MessageType.INFO, true);
                }
        );
    }
    
    private static boolean createShop(Player player, Location location) {
        Shop shop = new Shop(player.getUniqueId(), location.setRotation(0, 0));
        
        Block barrel = shop.getMultiblock().stockBlockLoc().getBlock();
        Block cashBlock = shop.getMultiblock().cashBlockLoc().getBlock();
        
        if (barrel.getType() != Material.AIR) {
            MessagesManager.sendMessage(player, Component.text("§cImpossible de créer le shop ici, l'espace du tonneau doit être libre"), Prefix.SHOP, MessageType.ERROR, true);
            return false;
        }
        
        if (cashBlock.getType() != Material.AIR) {
            MessagesManager.sendMessage(player, Component.text("§cImpossible de créer le shop ici, l'espace au-dessus du tonneau doit être libre"), Prefix.SHOP, MessageType.ERROR, true);
            return false;
        }
        
        if (ShopManager.placeShop(player, shop)) {
            barrel.setType(Material.BARREL);
            BlockData barrelData = barrel.getBlockData();
            if (barrelData instanceof Directional directional) {
                directional.setFacing(WorldUtils.getYaw(player).getOpposite().toBlockFace());
                barrel.setBlockData(barrelData);
            }
            
            Bukkit.getScheduler().runTaskAsynchronously(OMCPlugin.getInstance(), () -> {
                if (!ShopDatabaseManager.saveShop(shop)) {
                    MessagesManager.sendMessage(player, Component.text("§cErreur lors de la création du shop (cannot save shop location), essai de retrait du shop... §bVous, appelez un admin ou faites un ticket sur le discord d'OpenMC"), Prefix.SHOP, MessageType.ERROR, false);
	                OMCLogger.error("Error when saving shop location for player {}! Trying to remove shop...", player.getName());
                    if (!ShopManager.removeShop(shop)) MessagesManager.sendMessage(player, Component.text("§cImpossible de retirer le shop, tentez d'appeller un admin ou faites un ticket sur le discord d'OpenMC"), Prefix.SHOP, MessageType.ERROR, false);
                    MessagesManager.sendMessage(player, Component.text("§6La création de shop n'ayant pas été possible, 500 " + EconomyManager.getEconomyIcon() + " vous ont été remboursés"), Prefix.SHOP, MessageType.INFO, true);
                }
                else {
                    ShopManager.getPlayerShops().put(player.getUniqueId(), shop);
                    MessagesManager.sendMessage(player, Component.text("§aVotre shop a été créé avec succès ! Vous pouvez maintenant y ajouter des articles"), Prefix.SHOP, MessageType.SUCCESS, true);
                    MessagesManager.sendMessage(player, Component.text("§c500" + EconomyManager.getEconomyIcon() + " retirés de votre compte personnel"), Prefix.SHOP, MessageType.SUCCESS, false);
                }
            });
            return true;
        } else {
            MessagesManager.sendMessage(player, Component.text("§cErreur lors de la création du shop (multiblock is null) : §bappelez un admin ou faites un ticket sur le discord d'OpenMC"), Prefix.SHOP, MessageType.ERROR, false);
            return false;
        }
    }

    /**
     * Delete a shop if it is empty
     *
     * @param player The player who deletes the shop
     */
    public static void deleteShop(Player player) {
        Shop shop = ShopManager.getPlayerShop(player.getUniqueId());
        if (shop == null) {
	        OMCLogger.error("Shop for player {} is null!", player.getName());
            return;
        }
        
        if (shop.getItem() != null && shop.getItem().getAmount() > 0) {
            MessagesManager.sendMessage(player, Component.text("§cVotre shop n'est pas vide"), Prefix.SHOP, MessageType.WARNING, false);
            return;
        }
        
        if (!ShopManager.removeShop(shop)) {
            MessagesManager.sendMessage(player, Component.text("§cShop introuvable : appelez un admin ou faites un ticket sur le discord d'OpenMC"), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        
        ShopManager.getPlayerShops().remove(player.getUniqueId());
        
        Bukkit.getScheduler().runTaskAsynchronously(OMCPlugin.getInstance(), () -> {
            if (!ShopDatabaseManager.deleteShop(shop)) {
                MessagesManager.sendMessage(player, Component.text("§cErreur lors de la suppression du shop dans la database : veuillez faire un ticket sur le discord d'OpenMC pour signaler cet incident."), Prefix.SHOP, MessageType.ERROR, false);
                OMCLogger.error("Error when " + player.getName() + " trying to delete his shop!");
            }
        });
        
        MessagesManager.sendMessage(player, Component.text("§6Votre shop a bien été supprimé !"), Prefix.SHOP, MessageType.SUCCESS, false);
        
        EconomyManager.addBalance(player.getUniqueId(), 400);
        MessagesManager.sendMessage(player, Component.text("§a400" + EconomyManager.getEconomyIcon() + " remboursés sur votre compte personnel"), Prefix.SHOP, MessageType.SUCCESS, true);
    }
    
    public static void adminDeleteShop(OfflinePlayer player, Player admin) {
        Shop shop = ShopManager.getPlayerShop(player.getUniqueId());
        if (shop == null) return;
        
        if (!ShopManager.removeShop(shop)) {
            MessagesManager.sendMessage(admin, Component.text("§cShop introuvable"), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        ShopManager.getPlayerShops().remove(player.getUniqueId());
        
        Bukkit.getScheduler().runTaskAsynchronously(OMCPlugin.getInstance(), () -> {
            if (!ShopDatabaseManager.deleteShop(shop)) {
                MessagesManager.sendMessage(admin, Component.text("§cErreur lors de la suppression du shop dans la db"), Prefix.SHOP, MessageType.ERROR, false);
            }
        });
        
        MessagesManager.sendMessage(admin, Component.text("§6Le shop a bien été supprimé !"), Prefix.SHOP, MessageType.SUCCESS, false);
        EconomyManager.addBalance(player.getUniqueId(), 400);
        MessagesManager.sendMessage(player, Component.text("§a400" + EconomyManager.getEconomyIcon() + " remboursés sur votre compte personnel"), Prefix.SHOP, MessageType.SUCCESS, true);
    }
}

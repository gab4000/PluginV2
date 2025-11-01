package fr.openmc.core.features.corporation.manager;

import fr.openmc.api.input.location.ItemInteraction;
import fr.openmc.core.features.corporation.MethodState;
import fr.openmc.core.features.corporation.shops.Shop;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.economy.Transaction;
import fr.openmc.core.features.economy.TransactionsManager;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerShopManager {

    @Getter
    private static final Map<UUID, Shop> playerShops = new HashMap<>();

    /**
     * Create a shop if the player has enough money and does not already have one
     *
     * @param player the player who creates it
     */
    public static void startCreatingShop(Player player) {
        if (! EconomyManager.withdrawBalance(player.getUniqueId(), 500)) return;
        
        ItemInteraction.runLocationInteraction(
                player,
                new ItemStack(Material.BARREL),
                "shop:shop_creator",
                300,
                "Vous avez reçu un baril pour poser votre shop",
                "§cCréation de shop annulée",
                location -> {
                    Shop shop = new Shop(player.getUniqueId(), location);
                    playerShops.put(player.getUniqueId(), shop);
                    return true;
                },
                () -> {
                    EconomyManager.addBalance(player.getUniqueId(), 500);
                    TransactionsManager.registerTransaction(new Transaction(player.getUniqueId().toString(), "CONSOLE", 500, "Annulation création shop"));
                    MessagesManager.sendMessage(player, Component.text("§cVous avez été remboursé de 500 " + EconomyManager.getEconomyIcon() + " §cpour l'annulation de la création de votre shop"), Prefix.SHOP, MessageType.INFO, true);
                }
        );
    }

    /**
     * Delete a shop if it's empty
     *
     * @param playerUUID The UUID of the player who deletes the shop
     * @return a MethodeState indicating the result
     */
    public static MethodState deleteShop(UUID playerUUID) {
        Shop shop = getPlayerShop(playerUUID);
        if (! shop.getItems().isEmpty()) return MethodState.WARNING;
        
        if (! ShopManager.removeShop(shop)) return MethodState.ESCAPE;
        
        playerShops.remove(playerUUID);
        EconomyManager.addBalance(playerUUID, 400);
        return MethodState.SUCCESS;
    }

    /**
     * Get a shop from the UUID of a player
     *
     * @param playerUUID the UUID of the player to check
     * @return the Shop if found
     */
    public static Shop getPlayerShop(UUID playerUUID) {
        return playerShops.get(playerUUID);
    }

    /**
     * Get a shop from a shop UUID
     *
     * @param shopOwnerUUID the UUID of the shop to check
     * @return the Shop if found
     */
    public static Shop getShopByUUID(UUID shopOwnerUUID) {
        return playerShops.values().stream().filter(shop -> shop.getOwnerUUID().equals(shopOwnerUUID)).findFirst().orElse(null);
    }

    /**
     * Check if a player has a shop
     *
     * @param playerUUID the UUID of the player to check
     * @return true if a shop is found
     */
    public static boolean hasShop(UUID playerUUID) {
        return getPlayerShop(playerUUID) != null;
    }
}

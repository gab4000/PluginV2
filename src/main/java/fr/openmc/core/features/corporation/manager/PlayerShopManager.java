package fr.openmc.core.features.corporation.manager;

import fr.openmc.core.features.corporation.MethodState;
import fr.openmc.core.features.corporation.shops.Shop;
import fr.openmc.core.features.corporation.shops.ShopOwner;
import fr.openmc.core.features.economy.EconomyManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerShopManager {

    @Getter
    private static final Map<UUID, Shop> playerShops = new HashMap<>();

    /**
     * Create a shop if the player has enough money and doesn't already have one
     *
     * @param playerUUID   the UUID of the player who creates it
     * @param barrel       the barrel block of the shop
     * @param cashRegister the cash register block of the shop
     * @param shopUUID     the UUID of the shop if it already has one
     * @return true if the shop has been created
     */
    public static boolean createShop(UUID playerUUID, Block barrel, Block cashRegister, UUID shopUUID) {
        if (! EconomyManager.withdrawBalance(playerUUID, 500) && shopUUID == null) {
            return false;
        }
        
        Shop newShop = new Shop(new ShopOwner(playerUUID), 0, shopUUID);

        playerShops.put(playerUUID, newShop);
        ShopBlocksManager.registerMultiblock(newShop,
                new Shop.Multiblock(barrel.getLocation(), cashRegister.getLocation()));
        
        if (shopUUID == null) {
            ShopBlocksManager.placeShop(newShop, Bukkit.getPlayer(playerUUID), false);
        }
        return true;
    }

    /**
     * Delete a shop if it's empty
     *
     * @param playerUUID The UUID of the player who deletes the shop
     * @return a MethodeState indicating the result
     */
    public static MethodState deleteShop(UUID playerUUID) {
        Shop shop = getPlayerShop(playerUUID);
        if (!shop.getItems().isEmpty()) {
            return MethodState.WARNING;
        }
        if (!ShopBlocksManager.removeShop(shop)) {
            return MethodState.ESCAPE;
        }
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
     * @param shopUUID the UUID of the shop to check
     * @return the Shop if found
     */
    public static Shop getShopByUUID(UUID shopUUID) {
        return playerShops.values().stream().filter(shop -> shop.getUuid().equals(shopUUID)).findFirst().orElse(null);
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

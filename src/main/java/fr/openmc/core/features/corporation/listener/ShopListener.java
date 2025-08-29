package fr.openmc.core.features.corporation.listener;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import fr.openmc.core.features.corporation.manager.ShopBlocksManager;
import fr.openmc.core.features.corporation.menu.ShopMenu;
import fr.openmc.core.features.corporation.shops.Shop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {

    private final Map<UUID, Boolean> inShopBarrel = new HashMap<>();

    @EventHandler
    public void onShopBreak(BlockBreakEvent event) {
        if (ShopBlocksManager.getShop(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onShopExplode(BlockExplodeEvent event){
        event.blockList().removeIf(block -> ShopBlocksManager.getShop(block.getLocation()) != null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> ShopBlocksManager.getShop(block.getLocation()) != null);
    }

    @EventHandler
    public void onShopClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        // Check if the clicked block is a sign with tags
        // Instead of getting the entire state of the block
        // This is much faster and avoids unnecessary overhead
        if (!Tag.SIGNS.isTagged(event.getClickedBlock().getType()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Shop shop = ShopBlocksManager.getShop(event.getClickedBlock().getLocation());
        if (shop == null)
            return;
        
        event.setCancelled(true);
        ShopMenu menu = new ShopMenu(event.getPlayer(), shop, 0);
        menu.open();
    }

    @EventHandler
    public void onInteractWithBlock(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block != null && block.getType() == Material.BARREL) {
            Shop shop = ShopBlocksManager.getShop(block.getLocation());
            boolean isShop = shop != null;
            if (isShop){
                if (shop.getOwner().getPlayer() == null) {
                    e.setCancelled(true);
                    return;
                }
                if (! shop.getOwner().getPlayer().equals(e.getPlayer().getUniqueId())) {
                    e.setCancelled(true);
                    return;
                }
            }
            inShopBarrel.put(e.getPlayer().getUniqueId(), isShop);
        }
    }

    /**
     * check if an item is valid
     *
     * @param item the item to check
     * @return true if it's a valid item
     */
    private boolean isValidItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }
    
    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        CustomFurniture furniture = event.getFurniture();
        
        if (furniture != null && furniture.getNamespacedID().equals("omc_company:caisse")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent e) {
        
        if (e.getFurniture() == null) {
            return;
        }
        
        if (e.getFurniture().getNamespacedID().equals("omc_company:caisse")) {
            
            double x = e.getFurniture().getEntity().getLocation().getBlockX();
            double y = e.getFurniture().getEntity().getLocation().getBlockY();
            double z = e.getFurniture().getEntity().getLocation().getBlockZ();
            
            Shop shop = ShopBlocksManager.getShop(new Location(e.getFurniture().getEntity().getWorld(), x, y, z));
            if (shop == null) {
                return;
            }
            e.setCancelled(true);
            new ShopMenu(e.getPlayer(), shop, 0).open();
            
        }
    }
}

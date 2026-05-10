package fr.openmc.core.features.corporation.listener;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.corporation.manager.ShopManager;
import fr.openmc.core.features.corporation.menu.ShopMenu;
import fr.openmc.core.features.corporation.menu.ShopStocksMenu;
import fr.openmc.core.features.corporation.models.Shop;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
    private final boolean initialized;
    
    public ShopListener(boolean initialized) {
        this.initialized = true;
    }

    @EventHandler
    public void onShopBreak(BlockBreakEvent e) {
        if (ShopManager.getShopAt(e.getBlock().getLocation()) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onShopExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> ShopManager.getShopAt(block.getLocation()) != null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> ShopManager.getShopAt(block.getLocation()) != null);
    }

    @EventHandler
    public void onShopClick(PlayerInteractEvent e) {
        if (!initialized) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!block.getType().equals(Material.OAK_SIGN)) return;

        // Check if the clicked block is a sign with tags
        // Instead of getting the entire state of the block
        // This is much faster and avoids unnecessary overhead
        if (!Tag.SIGNS.isTagged(block.getType())) return;
        
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Shop shop = ShopManager.getShopAt(block.getLocation().subtract(0, 1, 0));
        if (shop == null) return;
        
        e.setCancelled(true);
        new ShopMenu(e.getPlayer(), shop).open();
    }

    @EventHandler
    public void onInteractWithBlock(PlayerInteractEvent e) {
        if (!initialized) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.BARREL) return;
        
        Shop shop = ShopManager.getShopAt(block.getLocation());
        if (shop == null) return;
        
        if (shop.getOwnerUUID() == null) {
            e.setCancelled(true);
            return;
        }
        
        Player player = e.getPlayer();
        if (!shop.getOwnerUUID().equals(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        
        Bukkit.getScheduler().runTaskLater(OMCPlugin.getInstance(), () -> {
            player.closeInventory();
            new ShopStocksMenu(player).open();
        }, 1);
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
    public void onFurnitureBreak(FurnitureBreakEvent e) {
        CustomFurniture furniture = e.getFurniture();
        
        if (furniture != null && furniture.getNamespacedID().equals("omc_company:caisse")) e.setCancelled(true);
    }
    
    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent e) {
        if (!initialized) return;
		CustomFurniture furniture = e.getFurniture();
		
        if (furniture == null) return;
        
        if (!furniture.getNamespacedID().equals("omc_company:caisse")) return;
        
        Player player = e.getPlayer();
	    if (furniture.getEntity() == null) {
		    MessagesManager.sendMessage(player, Component.text("§cErreur lors de l'ouverture du shop, veuillez contacter le staff. (Entity is null)"), Prefix.SHOP, MessageType.ERROR, true);
		    return;
	    }
	    
	    Shop shop = ShopManager.getShopAt(furniture.getEntity().getLocation().subtract(0, 1, 0).toBlockLocation());
	    if (shop == null) {
		    MessagesManager.sendMessage(player, Component.text("§cErreur lors de l'ouverture du shop, veuillez contacter le staff. (Shop is null)"), Prefix.SHOP, MessageType.ERROR, true);
			return;
	    }
	    
	    e.setCancelled(true);
	    new ShopMenu(player, shop).open();
    }
}

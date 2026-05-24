package fr.openmc.core.features.shops.menu;

import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.OMCRegistry;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.models.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShopStatsMenu extends Menu {
	
	private final Shop shop;
	private int totalSoldItems = 0;
	private final Set<UUID> totalUniquePlayers = new HashSet<>();
	
	protected ShopStatsMenu(Player owner, Shop shop) {
		super(owner);
		this.shop = shop;
		if (shop.getItem() == null) owner.closeInventory();
		if (!shop.getSales().isEmpty()) {
			shop.getSales().values().forEach(tuple -> {
				totalSoldItems += tuple.getB().getAmount();
				totalUniquePlayers.add(tuple.getA());
			});
		}
	}
	
	@Override
	public @NotNull Component getName() {
		return Component.text("Statistiques");
	}
	
	@Override
	public String getTexture() {
		return "";
	}
	
	@Override
	public @NotNull InventorySize getInventorySize() {
		return InventorySize.NORMAL;
	}
	
	@Override
	public void onInventoryClick(InventoryClickEvent e) {
	
	}
	
	@Override
	public void onClose(InventoryCloseEvent event) {
	
	}
	
	@Override
	public @NotNull Map<Integer, ItemBuilder> getContent() {
		Map<Integer, ItemBuilder> map = new HashMap<>();
		
		map.put(11, new ItemBuilder(this, Material.PAPER, itemMeta ->
				itemMeta.displayName(Component.text("§aTotal de ventes : " + this.shop.getSales().size()))));
		map.put(12, new ItemBuilder(this, Material.GOLD_INGOT, itemMeta ->
				itemMeta.displayName(Component.text("§2Total d'items vendus : " + this.totalSoldItems))));
		map.put(13, new ItemBuilder(this, Material.GOLD_BLOCK, itemMeta ->
				itemMeta.displayName(Component.text("§6Chiffre d'affaires : " + this.shop.getTurnover() + " " + EconomyManager.getEconomyIcon()))));
		map.put(14, new ItemBuilder(this, Material.PLAYER_HEAD, itemMeta ->
				itemMeta.displayName(Component.text("§dTotal d'acheteurs uniques : " + this.totalUniquePlayers.size()))));
		map.put(15, new ItemBuilder(this, Material.BARREL, itemMeta ->
				itemMeta.displayName(Component.text("§bItems restants dans les stocks : " + this.shop.getItem().getAmount()))));
		
		map.put(18, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("_iainternal:icon_back_orange").getBest(), _ -> new ShopMenu(getOwner(), shop).open()));
		
		return map;
	}
	
	@Override
	public List<Integer> getTakableSlot() {
		return List.of();
	}
}

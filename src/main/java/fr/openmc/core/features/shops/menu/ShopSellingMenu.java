package fr.openmc.core.features.shops.menu;

import fr.openmc.api.input.dialog.DialogInput;
import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.features.shops.models.Shop;
import fr.openmc.core.features.shops.models.ShopItem;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShopSellingMenu extends Menu {
	
	private final Shop shop;
	private final Inventory barrelInventory;
	private double price;
	
	public ShopSellingMenu(Player owner, Shop shop) {
		super(owner);
		this.shop = shop;
		this.barrelInventory = ((Barrel) shop.getMultiblock().stockBlockLoc().getBlock().getState()).getSnapshotInventory();
	}
	
	@Override
	public @NotNull Component getName() {
		return Component.text("Menu de vente");
	}
	
	@Override
	public String getTexture() {
		return "§r§f:offset_-11::large_shop_menu:";
	}
	
	@Override
	public @NotNull InventorySize getInventorySize() {
		return InventorySize.LARGEST;
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
		
		List<ItemStack> items = getUniqueItemStacks(barrelInventory.getContents()).stream().toList();
		for (int i = 0; i < items.size(); i++) {
			ItemStack item = items.get(i);
			map.put(i, new ItemBuilder(this, item, itemMeta -> {
				if (itemMeta.hasLore()) itemMeta.lore().add(Component.text("§l§8CLIQUEZ ICI POUR SELECTIONNER CET ITEM À VENDRE"));
				else itemMeta.lore(List.of(Component.text("§l§8CLIQUEZ ICI POUR SELECTIONNER CET ITEM À VENDRE")));
			}).setOnClick(_ -> DialogInput.send(getOwner(),
					Component.text("§6Entrez le prix auquel vous voulez vendre votre item :"),
					Integer.MAX_VALUE,
					s -> {
						double price = Double.parseDouble(s);
						if (Double.isNaN(price)) return;
						if (price <= 0) return;
						shop.setItem(new ShopItem(shop.getShopUUID(), item, price));
						new ShopMenu(getOwner(), shop).open();
						MessagesManager.sendMessage(getOwner(), Component.text("§aItem ajouté au shop."), Prefix.SHOP, MessageType.SUCCESS, true);
					})));
		}
		return map;
	}
	
	@Override
	public List<Integer> getTakableSlot() {
		return List.of();
	}
	
	private Set<ItemStack> getUniqueItemStacks(ItemStack[] items) {
		Set<ItemStack> itemStacks = new HashSet<>();
		for (ItemStack item : items) {
			if (item == null) continue;
			itemStacks.add(item.asOne());
		}
		return itemStacks;
	}
}

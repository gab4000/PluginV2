package fr.openmc.core.features.shops.menu;

import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.template.ConfirmMenu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.OMCRegistry;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.manager.PlayerShopManager;
import fr.openmc.core.features.shops.manager.ShopManager;
import fr.openmc.core.features.shops.models.Shop;
import fr.openmc.core.features.shops.models.ShopItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenu extends Menu {

    private int amountToBuy = 1;
    private final Shop shop;
    private final ShopItem item;
    private final boolean isShopOwner;
    
    private final InventorySize size;
    private final String texture;
    
    public ShopMenu(Player owner, Shop shop) {
        super(owner);
        this.shop = shop;
        this.item = shop.getItem();
        this.isShopOwner = ShopManager.isShopOwner(owner, shop);
        this.size = isShopOwner ? InventorySize.LARGER : InventorySize.LARGE;
        this.texture = isShopOwner ? "shop_menu" : "sell_shop_menu";
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Menu du shop de " + shop.getOwner().getName());
    }

    @Override
    public String getTexture() {
		return "§r§f:offset_-11::" + this.texture + ":";
    }

    @Override
    public @NotNull InventorySize getInventorySize() {
        return this.size;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        switch (event.getSlot()) {
            case 10, 11, 12, 14, 15, 16, 19, 20, 21, 23, 24, 25 -> update();
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Map<Integer, ItemBuilder> getContent() {
        Map<Integer, ItemBuilder> map = new HashMap<>();
        
        if (this.isShopOwner) {
            map.put(0, new ItemBuilder(this, Material.RED_DYE, itemMeta -> {
                itemMeta.displayName(Component.text("§4Supprimer le shop"));
                itemMeta.lore(List.of(
                        Component.text("§6Retirer tout l'argent et les items est nécessaire pour supprimer le shop."),
                        Component.text("§4§lATTENTION, IL SERA IMPOSSIBLE DE REVENIR EN ARRIÈRE !")
                ));
            }).setOnClick(_ -> new ConfirmMenu(
                    getOwner(),
                    () -> {
                        getOwner().closeInventory();
                        PlayerShopManager.deleteShop(getOwner(), shop);
                    },
                    () -> new ShopMenu(getOwner(), shop).open(),
                    List.of(Component.text("§4Supprimer DÉFINITIVEMENT le shop.")),
                    List.of(Component.text("§9Revenir dans le menu du shop."))
            ).open()));
            
            map.put(3, new ItemBuilder(this, Material.PAPER, itemMeta -> {
                itemMeta.displayName(Component.text("§dAccéder aux ventes du shop"));
                if (this.item == null) itemMeta.lore(List.of(Component.text("§cAucune statistique disponible, car aucun item n'est en vente.")));
            }).setOnClick(_ -> {
                if (this.item != null) new ShopSalesMenu(getOwner()).open();
            }));
            
            map.put(4, new ItemBuilder(this, Material.GOLD_INGOT, itemMeta -> {
                itemMeta.displayName(Component.text("§6Accéder aux statistiques du shop"));
                if (this.item == null) itemMeta.lore(List.of(Component.text("§cAucune statistique disponible, car aucun item n'est en vente.")));
            }).setOnClick(_ -> {
                if (this.item != null) new ShopStatsMenu(getOwner(), this.shop).open();
            }));
            
            map.put(5, new ItemBuilder(this, Material.BARREL, itemMeta -> {
                itemMeta.displayName(Component.text("§bAccéder aux stocks du shop"));
                if (this.item == null) itemMeta.lore(List.of(Component.text("§cImpossible d'accéder aux stocks, car aucun item n'est en vente.")));
            }).setOnClick(_ -> {
                if (this.item != null) new ShopStocksMenu(getOwner(), shop).open();
            }));
            
            map.put(8, new ItemBuilder(this, Material.GREEN_BANNER, itemMeta -> itemMeta.displayName(Component.text("§aCe shop est le vôtre"))));
        }
		
        map.put(isShopOwner ? 19 : 10, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:64_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 64"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 64")
            ));
        }).setOnClick(_ -> removeAmount(64)));
        map.put(isShopOwner ? 20 : 11, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:10_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 10"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 10")
            ));
        }).setOnClick(_ -> removeAmount(10)));
        map.put(isShopOwner ? 21 : 12, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:minus_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 1"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 1")
            ));
        }).setOnClick(_ -> removeAmount(1)));
        
        map.put(isShopOwner ? 23 : 14, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:plus_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 1"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 1")
            ));
        }).setOnClick(_ -> addAmount(1)));
        map.put(isShopOwner ? 24 : 15, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:10_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 10"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 10")
            ));
        }).setOnClick(_ -> addAmount(10)));
        map.put(isShopOwner ? 25 : 16, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:64_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 64"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 64")
            ));
        }).setOnClick(_ -> addAmount(64)));
        
        map.put(22, new ItemBuilder(this, this.item.getItemStack().asOne()));
        map.put(isShopOwner ? 30 : 21, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:refuse_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRefuser l'achat"));
            itemMeta.lore(List.of(
                    Component.text("§6Vous refusez d'acheter " + this.amountToBuy + " item(s)"),
                    Component.text("§e§lCLIQUEZ ICI POUR REFUSER L'ACHAT")
            ));
        }).setCloseButton());
        map.put(isShopOwner ? 32 : 23, new ItemBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("omc_menus:accept_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAccepter l'achat"));
            itemMeta.lore(List.of(
                    Component.text("§6Cela vous coûtera " + this.item.getPrice(this.amountToBuy) + " " + EconomyManager.getEconomyIcon() + " §6pour " + this.amountToBuy + " item(s)"),
                    Component.text("§e§lCLIQUEZ ICI POUR ACCEPTER L'ACHAT")
            ));
        }).setOnClick(_ -> this.shop.buy(getOwner(), this.amountToBuy)));
        
        return map;
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }
    
    private void addAmount(int amount) {
        if (this.item == null || this.item.getAmount() == 0) return;
        if (amount <= 0) return;
        if ((amountToBuy + amount) > this.item.getAmount()) {
            amountToBuy = this.item.getAmount();
            return;
        }
        this.amountToBuy += amount;
    }
    
    private void removeAmount(int amount) {
        if (this.item == null || this.item.getAmount() == 0) return;
        if (amount <= 0) return;
        if ((amountToBuy - amount) < 0) {
            amountToBuy = 1;
            return;
        }
        this.amountToBuy -= amount;
    }
}

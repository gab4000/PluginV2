package fr.openmc.core.features.corporation.menu;

import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.template.ConfirmMenu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.features.corporation.manager.PlayerShopManager;
import fr.openmc.core.features.corporation.models.Shop;
import fr.openmc.core.registry.items.CustomItemRegistry;
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
    private final boolean isShopOwner;
    
    private final InventorySize size;
    private final String texture;
    
    public ShopMenu(Player owner, Shop shop) {
        super(owner);
        this.shop = shop;
        this.isShopOwner = shop.getOwnerUUID().equals(owner.getUniqueId());
        this.size = isShopOwner ? InventorySize.LARGER : InventorySize.LARGE;
        this.texture =  isShopOwner ? "shop_menu" : "sell_shop_menu";
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Menu du shop de " + shop.getName().replace("'s Shop", ""));
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
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {

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
            }).setOnClick(event -> {
                new ConfirmMenu(
                        getOwner(),
                        () -> {
                            getOwner().closeInventory();
                            PlayerShopManager.deleteShop(getOwner());
                        },
                        () -> new ShopMenu(getOwner(), shop).open(),
                        List.of(Component.text("§4Supprimer DÉFINITIVEMENT le shop.")),
                        List.of(Component.text("§9Revenir dans le menu du shop."))
                ).open();
            }));
            
            map.put(3, new ItemBuilder(this, Material.PAPER, itemMeta -> {
                itemMeta.displayName(Component.text("§dAccéder aux ventes du shop"));
            }).setOnClick(event -> new ShopSalesMenu(getOwner()).open()));
            
            map.put(4, new ItemBuilder(this, Material.GOLD_INGOT, itemMeta -> {
                itemMeta.displayName(Component.text("§6Accéder au chiffre d'affaires du shop"));
            }));
            
            map.put(5, new ItemBuilder(this, Material.BARREL, itemMeta -> {
                itemMeta.displayName(Component.text("§bAccéder aux stocks du shop"));
            }).setOnClick(event -> new ShopStocksMenu(getOwner()).open()));
            
            map.put(8, new ItemBuilder(this, Material.GREEN_BANNER, itemMeta -> itemMeta.displayName(Component.text("§aCe shop est le vôtre"))));
        }
		
        map.put(isShopOwner ? 19 : 10, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:64_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 64"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 64")
            ));
        }).setOnClick(event -> removeAmount(64)));
        map.put(isShopOwner ? 20 : 11, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:10_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 10"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 10")
            ));
        }).setOnClick(event -> removeAmount(10)));
        map.put(isShopOwner ? 21 : 12, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:minus_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§cRetirer 1"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR RETIRER 1")
            ));
        }).setOnClick(event -> removeAmount(1)));
        
        map.put(isShopOwner ? 23 : 14, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:plus_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 1"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 1")
            ));
        }).setOnClick(event -> addAmount(1)));
        map.put(isShopOwner ? 24 : 15, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:10_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 10"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 10")
            ));
        }).setOnClick(event -> addAmount(10)));
        map.put(isShopOwner ? 25 : 16, new ItemBuilder(this, CustomItemRegistry.getByName("omc_menus:64_btn").getBest(), itemMeta -> {
            itemMeta.displayName(Component.text("§aAjouter 64"));
            itemMeta.lore(List.of(
                    Component.text("§e§lCLIQUEZ ICI POUR AJOUTER 64")
            ));
        }).setOnClick(event -> addAmount(64)));
        
        return map;
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }
    
    private void addAmount(int amount) {
        if (shop.getItem() == null || shop.getItem().getAmount() == 0) return;
        if (amount <= 0) return;
        if ((amountToBuy + amount) > this.shop.getItem().getAmount()) return;
        this.amountToBuy += amount;
    }
    
    private void removeAmount(int amount) {
        if (shop.getItem() == null || shop.getItem().getAmount() == 0) return;
        if (amount <= 0) return;
        if ((amountToBuy - amount) < 0) return;
        this.amountToBuy -= amount;
    }
}

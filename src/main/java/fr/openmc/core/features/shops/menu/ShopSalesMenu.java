package fr.openmc.core.features.shops.menu;

import fr.openmc.api.menulib.PaginatedMenu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemMenuBuilder;
import fr.openmc.api.menulib.utils.StaticSlots;
import fr.openmc.core.OMCRegistry;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.models.Shop;
import fr.openmc.core.features.shops.models.ShopSale;
import fr.openmc.core.utils.text.messages.TranslationManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSalesMenu extends PaginatedMenu {

    private final Shop shop;
    
    public ShopSalesMenu(Player owner, Shop shop) {
        super(owner);
        this.shop = shop;
    }

    @Override
    public @NotNull InventorySize getInventorySize() {
        return InventorySize.LARGEST;
    }

    @Override
    public int getSizeOfItems() {
        return getItems().size();
    }

    @Override
    public @Nullable Material getBorderMaterial() {
        return null;
    }

    @Override
    public @NotNull List<Integer> getStaticSlots() {
        return StaticSlots.getStandardSlots(getInventorySize());
    }

    @Override
    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        List<ShopSale> sales = this.shop.getSales();
        sales.forEach(s -> {
            ItemStack item = s.getItem().getItemStack();
            item.editMeta(itemMeta -> {
                itemMeta.displayName(Component.text("§6Achat de " + s.getBuyer().getName()));
                itemMeta.lore(List.of(
                        Component.text("§dle " + s.getDate().toLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))),
                        Component.text("§6 " + s.getAmount() + " items achetés pour " + s.getPrice() + " " + EconomyManager.getEconomyIcon())
                ));
            });
            items.add(item);
        });
        return items;
    }

    @Override
    public Map<Integer, ItemMenuBuilder> getButtons() {
        Map<Integer, ItemMenuBuilder> map = new HashMap<>();
        map.put(45, new ItemMenuBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("_iainternal:icon_cancel").getBest()).setOnClick(_ -> new ShopMenu(getOwner(), shop).open()));
        map.put(48, new ItemMenuBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("_iainternal:icon_back_orange").getBest()).setPreviousPageButton());
        map.put(49, new ItemMenuBuilder(this, Material.GOLD_BLOCK, itemMeta -> {
            itemMeta.displayName(Component.text("§6Récupérer les bénéfices."));
            itemMeta.lore(List.of(
                    Component.text("§d" + this.shop.getTurnover() * 0.8 + " " + EconomyManager.getEconomyIcon() + " §d à récupérer dans le shop (Hors taxes)."),
                    Component.text("§7La TVA est de 20 %")
            ));
        }).setOnClick(_ -> this.shop.withdrawTurnover()));
        map.put(50, new ItemMenuBuilder(this, OMCRegistry.CUSTOM_ITEMS.get("_iainternal:icon_next_orange").getBest()).setNextPageButton());
        return map;
    }

    @Override
    public @NotNull Component getName() {
        return TranslationManager.translation("feature.shop.menu.sales.title");
    }

    @Override
    public String getTexture() {
        return "§r§f:offset_-11::large_shop_menu:";
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {

    }
}

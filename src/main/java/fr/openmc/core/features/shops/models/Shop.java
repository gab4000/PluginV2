package fr.openmc.core.features.shops.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.ShopFurniture;
import fr.openmc.core.utils.bukkit.ItemUtils;
import fr.openmc.core.utils.cache.CacheOfflinePlayer;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@DatabaseTable(tableName = "shops")
public class Shop {
    
    @DatabaseField(id = true, columnName = "shop_uuid", canBeNull = false)
    private UUID shopUUID;
    @DatabaseField(columnName = "owner_uuid", canBeNull = false)
    private UUID ownerUUID;
    @DatabaseField(canBeNull = false)
    private int x;
    @DatabaseField(canBeNull = false)
    private int y;
    @DatabaseField(canBeNull = false)
    private int z;
    
    private ShopItem item;
    private final Map<LocalDateTime, Tuple<UUID, ShopItem>> sales = new HashMap<>();
	
    private Location location;
	private Multiblock multiblock;

    @Setter
    private double turnover = 0;
    
    Shop() {
        // required for ORMLite
    }
    
    public Shop(UUID ownerUUID, int x, int y, int z) {
		this(ownerUUID, new Location(Bukkit.getWorld("world"), x, y, z));
    }
    
    public Shop(UUID ownerUUID, Location location) {
        this.shopUUID = UUID.randomUUID();
        this.ownerUUID = ownerUUID;
	    this.x = location.getBlockX();
	    this.y = location.getBlockY();
	    this.z = location.getBlockZ();
        this.location = location.toBlockLocation();
		this.multiblock = new Multiblock(this.location, this.location.clone().add(0, 1, 0));
    }
    
    public Player getOwner() {
        return CacheOfflinePlayer.getOfflinePlayer(ownerUUID).getPlayer();
    }

    public String getName() {
        return getOwner().getName() + "'s Shop";
    }

    /**
     * know if the uuid is the shop owner
     *
     * @param uuid the uuid we check
     */
    public boolean isOwner(UUID uuid) {
        return ownerUUID.equals(uuid);
    }
    
    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }
    
    public void addSale(Player player, ShopItem item) {
        this.sales.put(LocalDateTime.now(), new Tuple<>(player.getUniqueId(), item));
    }
    
    public void addTurnover(double amount) {
        this.turnover += amount;
    }
    
    public double withdrawTurnover(Player player) {
        if (!isOwner(player)) return 0;
        if (getTurnover() <= 0) return 0;
        double tempTurnover = getTurnover();
        EconomyManager.addBalance(player.getUniqueId(), tempTurnover, "salaires");
        MessagesManager.sendMessage(player, Component.text("§6Vous avez récupéré " + tempTurnover + " " + EconomyManager.getEconomyIcon() + " de votre shop."), Prefix.SHOP, MessageType.SUCCESS, false);
        setTurnover(0);
        return tempTurnover;
    }
    
    public void buy(Player player, int amount) {
        if (isOwner(player)) {
            MessagesManager.sendMessage(player, Component.text("§cVous ne pouvez pas acheter des items à votre propre shop."), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        if (this.item.getAmount() < amount) {
            MessagesManager.sendMessage(player, Component.text("§cLe nombre d'items achetés dépasse le nombre d'items présents dans le shop."), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        if (!ItemUtils.hasEnoughSpace(player, item.getItemStack(), amount)) {
            MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas assez de place dans votre inventaire pour acheter ce nombre d'items."), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        double totalPrice = this.item.getPrice(amount);
        if (!EconomyManager.withdrawBalance(player.getUniqueId(), totalPrice, getName() + " buying")) {
            MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas assez d'argent pour acheter ce nombre d'items."), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        addSale(player, item.setAmount(amount));
        addTurnover(totalPrice);
        player.give(item.getItemStack().asQuantity(amount));
    }

    /**
     * get the shop Icon
     *
     * @param menu the menu
     * @param fromShopMenu know if it from shopMenu
     */
    public ItemBuilder getIcon(Menu menu, boolean fromShopMenu) {
        return new ItemBuilder(menu, fromShopMenu ? Material.GOLD_INGOT : Material.BARREL, itemMeta -> {
            itemMeta.displayName(Component.text("§e§l" + (fromShopMenu ? "Informations" : getName())));
            
            List<Component> lore = new ArrayList<>(List.of(
                    Component.text("§7■ Chiffre d'affaires : " + EconomyManager.getFormattedNumber(turnover)),
                    Component.text("§7■ Ventes : §f" + sales.size())
            ));
            if (!fromShopMenu) lore.add(Component.text("§7■ Cliquez pour accéder au shop"));
            itemMeta.lore(lore);
        });
    }
    
    public boolean setMultiblock(Multiblock multiblock) {
        if (multiblock.stockBlockLoc.getBlock().getType() != Material.BARREL
            || (multiblock.cashBlockLoc.getBlock().getType() != Material.OAK_SIGN
            && !ShopFurniture.hasFurniture(multiblock.cashBlockLoc.getBlock()))) {
            return false;
        }
        this.multiblock = multiblock;
        return true;
    }
    
    public void setItem(ShopItem item) {
        if (this.item != null) return;
        if (item.getPrice() < 0) return;
        if (item.getItemStack() == null) return;
        this.item = item;
    }
    
    public void removeItem() {
        if (this.item == null) return;
        if (this.item.getAmount() > 0) return;
        this.item = null;
    }
    
    public boolean hasItem() {
        return this.item != null;
    }

    public record Multiblock(Location stockBlockLoc, Location cashBlockLoc) {}
}

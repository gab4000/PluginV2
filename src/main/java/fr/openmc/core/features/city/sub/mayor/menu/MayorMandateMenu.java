package fr.openmc.core.features.city.sub.mayor.menu;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.api.menulib.utils.ItemUtils;
import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.CityPermission;
import fr.openmc.core.features.city.sub.mayor.managers.PerkManager;
import fr.openmc.core.features.city.sub.mayor.models.Mayor;
import fr.openmc.core.features.city.sub.mayor.perks.Perks;
import fr.openmc.core.utils.cache.CacheOfflinePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MayorMandateMenu extends Menu {

    public MayorMandateMenu(Player owner) {
        super(owner);
    }

    @Override
    public @NotNull String getName() {
        return "Menu des Maires - Mandat";
    }

    @Override
    public String getTexture() {
        return FontImageWrapper.replaceFontImages("§r§f:offset_-38::mayor:");
    }

    @Override
    public @NotNull InventorySize getInventorySize() {
        return InventorySize.LARGEST;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent click) {
        //empty
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public @NotNull Map<Integer, ItemBuilder> getContent() {
        Map<Integer, ItemBuilder> inventory = new HashMap<>();
        Player player = getOwner();

        City city = CityManager.getPlayerCity(player.getUniqueId());
        Mayor mayor = city.getMayor();

        Perks perk1 = PerkManager.getPerkById(mayor.getIdPerk1());
        Perks perk2 = PerkManager.getPerkById(mayor.getIdPerk2());
        Perks perk3 = PerkManager.getPerkById(mayor.getIdPerk3());

        List<Component> loreMayor = new ArrayList<>(List.of(
                Component.text("§8§oMaire de " + city.getName())
        ));
        loreMayor.add(Component.empty());
	    loreMayor.add(Component.text(perk2 == null ? "§cErreur de la réforme" : perk2.getName()));
        loreMayor.addAll(perk2 == null ? List.of() : perk2.getLore());
        loreMayor.add(Component.empty());
	    loreMayor.add(Component.text(perk3 == null ? "§cErreur de la réforme" : perk3.getName()));
        loreMayor.addAll(perk3 == null ? List.of() : perk3.getLore());


        inventory.put(3, new ItemBuilder(this, ItemUtils.getPlayerSkull(mayor.getMayorUUID()), itemMeta -> {
            itemMeta.displayName(Component.text("Maire " + mayor.getName()).color(mayor.getMayorColor()).decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(loreMayor);
        }));

        // ACCES DES LOIS
        // - PVP ENTRE MEMBRES (activé/désactiver) - Maire
        // - Annonce Ville (genre de broadcast ds ville) - Maire
        // - /city warp (donc le setspawnpoint de la ville en gros) => baton de set warp - Maire
        // - Evenement Déclanchable - Maire

        // si le joueur est maire

        if (player.getUniqueId().equals(mayor.getMayorUUID())) {
            List<Component> loreLaw = List.of(
		            Component.text("§7Vous êtes le ").append(Component.text("maire").color(mayor.getMayorColor()).decoration(TextDecoration.ITALIC, false).append(Component.text("§7!"))),
                    Component.empty(),
		            Component.text("§7Vous pouvez changer les §1lois §7et lancer des §6évènements §7!"),
                    Component.empty(),
                    Component.text("§e§lCLIQUEZ ICI POUR OUVRIR UN MENU")

            );
            inventory.put(4, new ItemBuilder(this, Material.STONE_BUTTON, itemMeta -> {
                itemMeta.itemName(Component.text("§1Les Lois"));
                itemMeta.lore(loreLaw);
            }).setOnClick(event -> new MayorLawMenu(player).open()));
        }

        List<Component> loreOwner = new ArrayList<>(List.of(
                Component.text("§8§oPropriétaire de " + city.getName())
        ));
        loreOwner.add(Component.empty());
	    loreOwner.add(Component.text(perk1 == null ? "§cErreur de la réforme" : perk1.getName()));
        loreOwner.addAll(perk1 == null ? List.of() : perk1.getLore());

        inventory.put(5, new ItemBuilder(this, ItemUtils.getPlayerSkull(city.getPlayerWithPermission(CityPermission.OWNER)), itemMeta -> {
            itemMeta.displayName(Component.text("§ePropriétaire " + CacheOfflinePlayer.getOfflinePlayer(city.getPlayerWithPermission((CityPermission.OWNER))).getName()));
            itemMeta.lore(loreOwner);
        }));

        ItemStack iaPerk1 = (perk1 != null) ? perk1.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
	    String namePerk1 = (perk1 != null) ? perk1.getName() : "§8Réforme vide";
        List<Component> lorePerk1 = (perk1 != null) ? new ArrayList<>(perk1.getLore()) : null;
        inventory.put(29, new ItemBuilder(this, iaPerk1, itemMeta -> {
            itemMeta.customName(Component.text(namePerk1));
            itemMeta.lore(lorePerk1);
        }).hide((perk1 != null) ? perk1.getToHide() : null));

        ItemStack iaPerk2 = (perk2 != null) ? perk2.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
	    String namePerk2 = (perk2 != null) ? perk2.getName() : "§8Réforme vide";
        List<Component> lorePerk2 = (perk2 != null) ? new ArrayList<>(perk2.getLore()) : null;
        inventory.put(22, new ItemBuilder(this, iaPerk2, itemMeta -> {
            itemMeta.customName(Component.text(namePerk2));
            itemMeta.lore(lorePerk2);
        }).hide((perk2 != null) ? perk2.getToHide() : null));

        ItemStack iaPerk3 = (perk3 != null) ? perk3.getItemStack() : ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK);
	    String namePerk3 = (perk3 != null) ? perk3.getName() : "§8Réforme vide";
        List<Component> lorePerk3 = (perk3 != null) ? new ArrayList<>(perk3.getLore()) : null;
        inventory.put(33, new ItemBuilder(this, iaPerk3, itemMeta -> {
            itemMeta.customName(Component.text(namePerk3));
            itemMeta.lore(lorePerk3);
        }).hide((perk3 != null) ? perk3.getToHide() : null));

        inventory.put(46, new ItemBuilder(this, Material.ARROW, itemMeta -> {
            itemMeta.itemName(Component.text("§aRetour"));
            itemMeta.lore(List.of(
		            Component.text("§7Vous allez retourner au menu précédent"),
                    Component.text("§e§lCLIQUEZ ICI POUR CONFIRMER")
            ));
        }, true));

        List<Component> loreInfo = Arrays.asList(
		        Component.text("§7Apprenez en plus sur les maires !"),
		        Component.text("§7Le déroulement..., les éléctions, ..."),
                Component.text("§e§lCLIQUEZ ICI POUR EN VOIR PLUS!")
        );

        inventory.put(52, new ItemBuilder(this, Material.BOOK, itemMeta -> {
            itemMeta.displayName(Component.text("§r§aPlus d'info !"));
            itemMeta.lore(loreInfo);
        }).setOnClick(inventoryClickEvent -> new MoreInfoMenu(getOwner()).open()));

        return inventory;
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }
}

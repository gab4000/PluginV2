package fr.openmc.core.features.city.sub.mayor.menu.create;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.openmc.api.menulib.Menu;
import fr.openmc.api.menulib.template.ConfirmMenu;
import fr.openmc.api.menulib.utils.InventorySize;
import fr.openmc.api.menulib.utils.ItemBuilder;
import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.sub.mayor.ElectionType;
import fr.openmc.core.features.city.sub.mayor.managers.MayorManager;
import fr.openmc.core.features.city.sub.mayor.models.MayorCandidate;
import fr.openmc.core.features.city.sub.mayor.perks.Perks;
import fr.openmc.core.utils.ColorUtils;
import fr.openmc.core.utils.cache.CacheOfflinePlayer;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.MessagesManager;
import fr.openmc.core.utils.messages.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MayorColorMenu extends Menu {
    private final String type;
    private final Perks perk1;
    private final Perks perk2;
    private final Perks perk3;
    private final MenuType menuType;

    public MayorColorMenu(Player owner, Perks perk1, Perks perk2, Perks perk3, String type, MenuType menuType) {
        super(owner);
        this.type = type;
        this.perk1 = perk1;
        this.perk2 = perk2;
        this.perk3 = perk3;
        this.menuType = menuType;
    }

    @Override
    public @NotNull String getName() {
	    return "Menu des maires - Couleur";
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
        Map<NamedTextColor, Integer> colorSlot = new HashMap<>();
        {
            colorSlot.put(NamedTextColor.RED, 12);
            colorSlot.put(NamedTextColor.GOLD, 13);
            colorSlot.put(NamedTextColor.YELLOW, 14);
            colorSlot.put(NamedTextColor.GREEN, 21);
            colorSlot.put(NamedTextColor.DARK_GREEN, 22);
            colorSlot.put(NamedTextColor.BLUE, 23);
            colorSlot.put(NamedTextColor.AQUA, 30);
            colorSlot.put(NamedTextColor.DARK_BLUE, 31);
            colorSlot.put(NamedTextColor.DARK_PURPLE, 32);
            colorSlot.put(NamedTextColor.LIGHT_PURPLE, 39);
            colorSlot.put(NamedTextColor.WHITE, 40);
            colorSlot.put(NamedTextColor.GRAY, 41);
            colorSlot.put(NamedTextColor.DARK_GRAY, 49);
        }
        colorSlot.forEach((color, slot) -> {
            List<Component> loreColor = List.of(
                    Component.text("§7Votre nom sera affiché en " + ColorUtils.getNameFromColor(color)),
                    Component.empty(),
                    Component.text("§e§lCLIQUEZ ICI POUR CONFIRMER")
            );
            inventory.put(slot, new ItemBuilder(this, ColorUtils.getMaterialFromColor(color), itemMeta -> {
                itemMeta.displayName(Component.text("§7Mettez du " + ColorUtils.getNameFromColor(color)));
                itemMeta.lore(loreColor);
            }).setOnClick(inventoryClickEvent -> {
                if (type.equals("create")) {
                    List<Component> loreAccept = new ArrayList<>(List.of(
		                    Component.text("§7Vous allez vous présenter en tant que §6maire de " + city.getName()),
                            Component.empty(),
                            Component.text("Maire " + player.getName()).color(color).decoration(TextDecoration.ITALIC, false)
                    ));
                    if (perk1 != null) {
                        loreAccept.add(Component.text(perk1.getName()));
                        loreAccept.addAll(perk1.getLore());
                        loreAccept.add(Component.empty());
                    }
                    loreAccept.add(Component.text(perk2.getName()));
                    loreAccept.addAll(perk2.getLore());
                    loreAccept.add(Component.empty());
                    loreAccept.add(Component.text(perk3.getName()));
                    loreAccept.addAll(perk3.getLore());
                    loreAccept.add(Component.empty());
                    loreAccept.add(Component.text("§c§lAUCUN RETOUR EN ARRIERE POSSIBLE!"));


                    ConfirmMenu menu = new ConfirmMenu(player,
                            () -> {
                                try {
                                    if (menuType == MenuType.CANDIDATE) {
                                        MayorCandidate candidate = new MayorCandidate(city.getUniqueId(), player.getName(), player.getUniqueId(), color, perk2.getId(), perk3.getId(), 0);
                                        MayorManager.createCandidate(city, candidate);

                                        for (UUID uuid : city.getMembers()) {
                                            OfflinePlayer playerMember = CacheOfflinePlayer.getOfflinePlayer(uuid);
                                            assert playerMember != null;
                                            if (playerMember == player) continue;
                                            if (playerMember.isOnline()) {
	                                            MessagesManager.sendMessage(playerMember.getPlayer(), Component.text(player.getName()).color(color).append(Component.text(" §7s'est présenté en tant que §6maire§7!")), Prefix.MAYOR, MessageType.ERROR, false);
                                            }
                                        }
                                    } else { // donc si c MenuType.OWNER
                                        MayorManager.createMayor(player.getName(), player.getUniqueId(), city, perk1, perk2, perk3, color, city.getElectionType());
                                    }
                                    MessagesManager.sendMessage(player, Component.text("§7Vous vous êtes présenter avec §asuccès§7!"), Prefix.MAYOR, MessageType.ERROR, false);
                                    player.closeInventory();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            player::closeInventory,
                            loreAccept,
                            List.of(
                                    Component.text("§7Ne pas se présenter en tant que §6Maire de " + city.getName())
                            )
                    );
                    menu.open();
                } else if (type.equals("change")) {
                    if (city.getElectionType() == ElectionType.OWNER_CHOOSE) {
                        if (city.getMayor() == null) {
                            MessagesManager.sendMessage(player, Component.text("Votre ville n'a pas de maire !"), Prefix.MAYOR, MessageType.ERROR, false);
                            return;
                        }
                        NamedTextColor thisColor = city.getMayor().getMayorColor();
                        ConfirmMenu menu = new ConfirmMenu(player,
                                () -> {
                                    city.getMayor().setMayorColor(color);
                                    MessagesManager.sendMessage(player, Component.text("§7Vous avez changer votre ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color)), Prefix.MAYOR, MessageType.SUCCESS, false);
                                    player.closeInventory();
                                },
                                player::closeInventory,
                                List.of(
                                        Component.text("§7Changer sa ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color))
                                ),
                                List.of(
                                        Component.text("§7Ne pas changer sa ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color))
                                )
                        );
                        menu.open();
                    } else {
                        MayorCandidate mayorCandidate = MayorManager.getCandidate(player.getUniqueId());
                        NamedTextColor thisColor = mayorCandidate.getCandidateColor();
                        ConfirmMenu menu = new ConfirmMenu(player,
                                () -> {
                                    mayorCandidate.setCandidateColor(color);
                                    MessagesManager.sendMessage(player, Component.text("§7Vous avez changer votre ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color)), Prefix.CITY, MessageType.SUCCESS, false);
                                    player.closeInventory();
                                },
                                player::closeInventory,
                                List.of(
                                        Component.text("§7Changer sa ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color))
                                ),
                                List.of(
                                        Component.text("§7Ne pas changer sa ").append(Component.text("couleur ").decoration(TextDecoration.ITALIC, false).color(thisColor)).append(Component.text("§7en ")).append(Component.text("celle ci").decoration(TextDecoration.ITALIC, false).color(color))
                                )
                        );
                        menu.open();
                    }
                }
            }));
        });

        return inventory;
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }
}
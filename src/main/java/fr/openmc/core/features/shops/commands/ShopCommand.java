package fr.openmc.core.features.shops.commands;

import fr.openmc.core.features.economy.EconomyManager;
import fr.openmc.core.features.shops.manager.PlayerShopManager;
import fr.openmc.core.features.shops.manager.ShopManager;
import fr.openmc.core.features.shops.menu.ShopSearchMenu;
import fr.openmc.core.utils.text.messages.MessageType;
import fr.openmc.core.utils.text.messages.MessagesManager;
import fr.openmc.core.utils.text.messages.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("shop")
@Description("Manage shops")
@CommandPermission("omc.commands.shop")
public class ShopCommand {

    @Subcommand("help")
    @Description("Explique comment marche un shop")
    @Cooldown(30)
    public void help(Player player) {
        MessagesManager.sendMessage(player, Component.text("""
            §6§lListe des commandes shop :
            
            §e▪ /shop create§7 - Crée un shop si vous regardez un tonneau
            §e▪ /shop sell <prix>§7 - Permet de mettre en vente l'item dans votre main
            §e▪ /shop unsell§7 - Permet de retirer de la vente l'item que vous tenez en main
            §e▪ /shop delete§7 - Permet de supprimer votre shop en le regardant
            §e▪ /shop search§7 - Permet de rechercher des shops par leur nom ou le nom du joueur
            """),
                Prefix.SHOP, MessageType.INFO, false);
    }
    
    @Subcommand("create")
    @Description("Create a shop")
    public void createShop(Player player) {
        if (ShopManager.hasShop(player.getUniqueId())) {
            MessagesManager.sendMessage(player, Component.text("§cVous avez déjà un shop"), Prefix.SHOP, MessageType.INFO, false);
            return;
        }
        if (! EconomyManager.hasEnoughMoney(player.getUniqueId(), 500)) {
            MessagesManager.sendMessage(player, Component.text("§cVous n'avez pas assez d'argent pour créer un shop (500" + EconomyManager.getEconomyIcon() + " nécessaires)"), Prefix.SHOP, MessageType.ERROR, false);
            return;
        }
        
        PlayerShopManager.startCreatingShop(player);
    }

    @Subcommand("search")
    @Description("Recherche un shop")
    public void searchShop(Player player){
        new ShopSearchMenu(player).open();
    }
}

package fr.openmc.core.features.dream.milestone;

import fr.openmc.api.menulib.Menu;
import fr.openmc.core.features.milestones.Milestone;
import fr.openmc.core.features.milestones.MilestoneType;
import fr.openmc.core.features.milestones.menus.MilestoneMenu;
import fr.openmc.core.features.quests.objects.Quest;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DreamMilestone implements Milestone {
	@Override
	public String getName() {
		return "Tutoriel de la Dream Dim";
	}
	
	@Override
	public List<Component> getDescription() {
		return List.of(
				Component.text("§7Plongez-vous dans un §6long sommeil §7!"),
				Component.text("§7Arpentez vos rêves et gagnez des §6récompenses §7!"),
				Component.text("§7Un monde nouveau s'offre à vous,"),
				Component.text("§7mais arriverez-vous à vous extraire de ce lieu sombre et chaotique ?")
		);
	}
	
	@Override
	public ItemStack getIcon() {
		return ItemStack.of(Material.SCULK);
	}
	
	@Override
	public List<Quest> getSteps() {
		return Arrays.stream(DreamSteps.values()).map(DreamSteps::getQuest).toList();
	}
	
	@Override
	public MilestoneType getType() {
		return MilestoneType.DREAM;
	}
	
	@Override
	public Menu getMenu(Player player) {
		return new MilestoneMenu(player, this);
	}
}

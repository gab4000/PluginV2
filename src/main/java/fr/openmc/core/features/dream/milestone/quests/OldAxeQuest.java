package fr.openmc.core.features.dream.milestone.quests;

import fr.openmc.core.features.dream.milestone.DreamSteps;
import fr.openmc.core.features.dream.models.registry.items.DreamItem;
import fr.openmc.core.features.dream.registries.DreamItemRegistry;
import fr.openmc.core.features.dream.registries.items.tools.OldCreakingAxe;
import fr.openmc.core.features.milestones.MilestoneType;
import fr.openmc.core.features.milestones.MilestoneUtils;
import fr.openmc.core.features.milestones.MilestonesManager;
import fr.openmc.core.features.quests.objects.Quest;
import fr.openmc.core.features.quests.objects.QuestTier;
import fr.openmc.core.features.quests.rewards.QuestMethodsReward;
import fr.openmc.core.features.quests.rewards.QuestTextReward;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.Prefix;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OldAxeQuest extends Quest implements Listener {
	
	public OldAxeQuest() {
		super(
				"Le premier outil ! Enfin !",
				List.of(
						"§fFabriquer une §dVieille hache du Creaking",
						"§8§oNotre meilleur ami dans ce monde !"
				),
				DreamItemRegistry.getByName("omc_dream:old_creaking_axe").getBestTransferable()
		);
		
		this.addTier(new QuestTier(
				1,
				new QuestTextReward("Une hâche, c'est déjà ça. Il faut collecter les orbes qui sont au nombre de 5. La première est d'ailleurs craftable...", Prefix.DREAM, MessageType.SUCCESS),
				new QuestMethodsReward(player -> MilestoneUtils.completeStep(MilestoneType.DREAM, player, DreamSteps.OLD_AXE.ordinal()))
		));
	}
	
	@EventHandler
	public void onCraft(CraftItemEvent e) {
		ItemStack item = e.getCurrentItem();
		if (item == null) return;
		
		DreamItem dreamItem = DreamItemRegistry.getByItemStack(item);
		if (dreamItem == null) return;
		if (dreamItem instanceof OldCreakingAxe) {
			if (e.getWhoClicked() instanceof Player player) {
				if (MilestonesManager.getPlayerStep(MilestoneType.DREAM, player) != DreamSteps.OLD_AXE.ordinal()) return;
				this.incrementProgressInDream(player.getUniqueId());
			}
		}
	}
}

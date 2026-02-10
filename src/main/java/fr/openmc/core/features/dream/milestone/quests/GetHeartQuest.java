package fr.openmc.core.features.dream.milestone.quests;

import fr.openmc.core.features.dream.DreamUtils;
import fr.openmc.core.features.dream.milestone.DreamSteps;
import fr.openmc.core.features.dream.models.registry.items.DreamItem;
import fr.openmc.core.features.dream.registries.DreamItemRegistry;
import fr.openmc.core.features.dream.registries.items.loots.CreakingHeart;
import fr.openmc.core.features.milestones.MilestoneType;
import fr.openmc.core.features.milestones.MilestoneUtils;
import fr.openmc.core.features.milestones.MilestonesManager;
import fr.openmc.core.features.quests.objects.Quest;
import fr.openmc.core.features.quests.objects.QuestTier;
import fr.openmc.core.features.quests.rewards.QuestMethodsReward;
import fr.openmc.core.features.quests.rewards.QuestTextReward;
import fr.openmc.core.utils.messages.MessageType;
import fr.openmc.core.utils.messages.Prefix;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.List;

public class GetHeartQuest extends Quest implements Listener {
	
	public GetHeartQuest() {
		super(
				"La résine n'a pas de coeur",
				List.of(
						"§fRécupérer un §dCoeur de Creaking",
						"§8§oOn cherche la résine ou le coeur ?"
				),
				Material.RESIN_CLUMP
		);
		
		this.addTier(new QuestTier(
				1,
				new QuestTextReward("Ah ! Mais c'est vrai qu'il protègent leurs coeurs, qui va mettre bien utile par la suite. Mais bref, trêve de tergiversation, il me faut des outils.", Prefix.DREAM, MessageType.SUCCESS),
				new QuestMethodsReward(player -> MilestoneUtils.completeStep(MilestoneType.DREAM, player, DreamSteps.GET_HEART.ordinal()))
		));
	}
	
	@EventHandler
	public void onPickUp(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player player) {
			if (!DreamUtils.isInDreamWorld(player)) return;
			
			DreamItem item = DreamItemRegistry.getByItemStack(e.getItem().getItemStack());
			if (item == null) return;
			if (item instanceof CreakingHeart) {
				if (MilestonesManager.getPlayerStep(MilestoneType.DREAM, player) != DreamSteps.GET_HEART.ordinal()) return;
				this.incrementProgressInDream(player.getUniqueId());
			}
		}
	}
}

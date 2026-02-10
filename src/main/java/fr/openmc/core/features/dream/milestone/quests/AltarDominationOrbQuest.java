package fr.openmc.core.features.dream.milestone.quests;

import fr.openmc.core.features.dream.DreamUtils;
import fr.openmc.core.features.dream.events.AltarBindEvent;
import fr.openmc.core.features.dream.milestone.DreamSteps;
import fr.openmc.core.features.dream.models.registry.items.DreamItem;
import fr.openmc.core.features.dream.registries.items.orb.DominationOrb;
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

import java.util.List;

public class AltarDominationOrbQuest extends Quest implements Listener {
	public AltarDominationOrbQuest() {
		super(
				"Début du rituel",
				List.of(
						"§fDéposer l'§dOrbe de Domination §fsur l'§dAltar",
						"§8§oCommençons le rituel de conversion de l'orbe"
				),
				Material.ENCHANTING_TABLE
		);
		
		this.addTier(new QuestTier(
				1,
				new QuestTextReward("Hmmm... avec cette table étrange, il est visiblement possible de transformer l'Orbe de Domination. Mais pour en faire quoi !? \n" +
						"Ce qui est sur, c'est qui me manque quelque chose pour accomplir ce rituel jusqu'au bout.", Prefix.DREAM, MessageType.SUCCESS),
				new QuestMethodsReward(player -> MilestoneUtils.completeStep(MilestoneType.DREAM, player, DreamSteps.ALTAR_DOMINATION.ordinal()))
		));
	}
	
	@EventHandler
	public void onAltarBind(AltarBindEvent e) {
		Player player = e.getPlayer();
		if (!DreamUtils.isInDreamWorld(player)) return;
		
		DreamItem item = e.getItem();
		if (item == null) return;
		if (item instanceof DominationOrb) {
			if (MilestonesManager.getPlayerStep(MilestoneType.DREAM, player) != DreamSteps.ALTAR_DOMINATION.ordinal()) return;
			
			this.incrementProgressInDream(player.getUniqueId());
		}
	}
}

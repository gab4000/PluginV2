package fr.openmc.core.features.dream.events;

import lombok.Getter;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerEnterBiomeEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Player player;
	private final Biome biome;
	
	/**
	 * @param player The player whose dream time has ended
	 */
	public PlayerEnterBiomeEvent(Player player, Biome biome) {
		this.player = player;
		this.biome = biome;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}

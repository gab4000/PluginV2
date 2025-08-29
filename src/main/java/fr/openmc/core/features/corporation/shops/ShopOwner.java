package fr.openmc.core.features.corporation.shops;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ShopOwner {
    
    private final UUID player;

    /**
     * ShopOwner for a company or a player
     *
     * @param owner to set a player as the owner
     */
    public ShopOwner(UUID owner) {
        this.player = owner;
    }
    
    /**
     * Check if the owner is a player
     *
     * @return true if the owner is a player, false otherwise
     */
    public boolean isPlayer() {
        return player != null;
    }

}

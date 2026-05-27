package fr.openmc.core.utils.bukkit;

import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

public class ContainerUtils {
	
	public static int getTotalItemsIn(Container container, ItemStack item) {
		if (container == null || item == null) return 0;
		int total = 0;
		for (ItemStack itemStack : container.getSnapshotInventory().getStorageContents()) {
			if (itemStack == null) continue;
			if (!ItemUtils.isSimilar(item, itemStack)) continue;
			total += itemStack.getAmount();
		}
		return total;
	}
	
	/**
	 * Retirer le nombre d'objets au container (vérification obligatoire avant execution)
	 *
	 * @param container the container whose inventory will be modified
	 * @param item the item to remove, must be similar to the items in the inventory {@link ItemStack}
	 * @param amountToRemove the number of items to remove
	 */
	public static int removeItemsFromInventory(Container container, ItemStack item, int amountToRemove) {
		if (container == null || item == null || amountToRemove <= 0) return 0;
		
		int removed = 0;
		ItemStack[] contents = container.getInventory().getContents();
		
		for (int i = 0; i < contents.length && removed < amountToRemove; i++) {
			ItemStack stack = contents[i];
			if (stack == null) continue;
			
			if (ItemUtils.isSimilar(stack, item)) {
				int stackAmount = stack.getAmount();
				int toRemove = Math.min(amountToRemove - removed, stackAmount);
				
				removed += toRemove;
				
				if (stackAmount <= toRemove) {
					container.getInventory().setItem(i, null);
				} else {
					stack.setAmount(stackAmount - toRemove);
				}
			}
		}
		
		return removed;
	}
}

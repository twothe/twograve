/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.API;

import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Handler for vanilla inventory management.
 *
 * This handles the entire Vanilla player inventory (including the hot bar and
 * armor) by using the base implementation functions.
 *
 * Also serves as an example.
 *
 * @author Two
 */
public class InventoryHandlerVanilla extends InventoryHandlerBase {

  /* Remove and return all items in the player's inventory. */
  @Override
  public Collection<InventoryContent> removeAllItems(final EntityPlayer player, final boolean isRemote) {
    return this.stripInventory(player, player.inventory); // using the base implementation to strip standard IInventory.
  }

  /* Set item to a specific slot if possible. */
  @Override
  public boolean set(final EntityPlayer player, final int slot, final ItemStack itemStack, final boolean isRemote) {
    return this.setInventorySlot(player, player.inventory, slot, itemStack); // using the base implementation to set a IInventory slot.
  }

  /* Add the item to any possible slot. */
  @Override
  public boolean add(final EntityPlayer player, final ItemStack itemStack, final boolean isRemote) {
    return this.addToInventory(player, player.inventory, itemStack, isRemote); // using the base implementation to merge with standard IInventory.
  }

  /* Return the ID of this handler. */
  @Override
  public String getID() {
    return ""; // this is a special code to minimze network packet size. You must return a real ID here, like your mod-ID.
  }

  @Override
  public void markDirty(final EntityPlayer player, final boolean isRemote) {
    if (isRemote) {
      player.inventory.markDirty(); // notify that the inventory has changed
    }
  }

}

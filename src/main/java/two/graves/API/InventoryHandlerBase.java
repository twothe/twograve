/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.API;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Base implementation of an IInventoryHandler.
 * 
 * This provides convenience functions to handle standard Minecraft IInventory.
 * See InventoryHandlerVanilla for an example of how to use this.
 * 
 * @author Two
 */
public abstract class InventoryHandlerBase implements IInventoryHandler {

  /* This will strip all items from a standard Minecraft IInventory.*/
  protected Collection<InventoryContent> stripInventory(final EntityPlayer player, final IInventory inventory) {
    final ArrayList<InventoryContent> result = new ArrayList<InventoryContent>();

    final int size = inventory.getSizeInventory();
    for (int slotID = 0; slotID < size; ++slotID) {
      final ItemStack itemStack = inventory.getStackInSlot(slotID);
      if ((itemStack != null) && canRemove(player, inventory, slotID, itemStack)) {
        final InventoryContent content = new InventoryContent(this.getID(), slotID, itemStack.copy());
        result.add(content);
        inventory.setInventorySlotContents(slotID, null);
      }
    }

    return result;
  }

  /* Sets the slot of a standard Minecraft IInventory. */
  protected boolean setInventorySlot(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    if (canAdd(player, inventory, slotID, itemStack)) {
      inventory.setInventorySlotContents(slotID, itemStack);
      return true;
    }
    return false;
  }

  /* Adds the item to a standard Minecraft IInventory. */
  protected boolean addToInventory(final EntityPlayer player, final IInventory inventory, final ItemStack itemStack, final boolean isRemote) {
    final int size = inventory.getSizeInventory();
    for (int slotID = 0; slotID < size; ++slotID) {
      if (set(player, slotID, itemStack, isRemote)) {
        return true;
      }
    }
    return false;
  }

  /* Intended to be overwritte by handlers that require a more complex handling. */
  protected boolean canAdd(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    return ((inventory.getStackInSlot(slotID) == null) && inventory.isItemValidForSlot(slotID, itemStack));
  }

  /* Intended to be overwritte by handlers that require a more complex handling. */
  protected boolean canRemove(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    return (inventory.getStackInSlot(slotID) != null);
  }
}

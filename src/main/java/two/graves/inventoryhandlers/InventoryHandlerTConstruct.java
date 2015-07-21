/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.inventoryhandlers;

import java.util.Collection;
import java.util.LinkedList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import tconstruct.api.IPlayerExtendedInventoryWrapper;
import tconstruct.api.TConstructAPI;
import two.graves.API.InventoryContent;
import two.graves.API.InventoryHandlerBase;

/**
 *
 * @author Two
 */
public class InventoryHandlerTConstruct extends InventoryHandlerBase {

  protected static final int SLOT_KNAPSACK = 2;
  protected static final int SLOT_HEART_RED = 4;
  protected static final int SLOT_HEART_YELLOW = 5;
  protected static final int SLOT_HEART_GREEN = 6;

  protected static final int OFFSET_KNAPSACK = 100;

  @Override
  public Collection<InventoryContent> removeAllItems(final EntityPlayer player, final boolean isRemote) {
    final IPlayerExtendedInventoryWrapper inventoryWrapper = TConstructAPI.getInventoryWrapper(player);
    final IInventory knapsack = inventoryWrapper.getKnapsackInventory(player);
    final IInventory accessories = inventoryWrapper.getAccessoryInventory(player);
    final LinkedList<InventoryContent> result = new LinkedList<InventoryContent>();

    final int sizeKnapsack = knapsack.getSizeInventory();
    for (int slotID = 0; slotID < sizeKnapsack; ++slotID) {
      final ItemStack itemStack = knapsack.getStackInSlot(slotID);
      if ((itemStack != null) && canRemove(player, knapsack, slotID, itemStack)) {
        final InventoryContent content = new InventoryContent(this.getID(), slotID + OFFSET_KNAPSACK, itemStack.copy());
        result.add(content);
        knapsack.setInventorySlotContents(slotID, null);
      }
    }

    final int sizeAccesories = accessories.getSizeInventory();
    for (int slotID = 0; slotID < sizeAccesories; ++slotID) {
      if ((slotID != SLOT_HEART_RED) && (slotID != SLOT_HEART_YELLOW) && (slotID != SLOT_HEART_GREEN)) {
        final ItemStack itemStack = accessories.getStackInSlot(slotID);
        if ((itemStack != null) && canRemove(player, accessories, slotID, itemStack)) {
          final InventoryContent content = new InventoryContent(this.getID(), slotID, itemStack.copy());
          result.addFirst(content);
          accessories.setInventorySlotContents(slotID, null);
        }
      }
    }

    return result;
  }

  @Override
  public boolean set(final EntityPlayer player, final int slot, final ItemStack itemStack, final boolean isRemote) {
    if (slot >= 100) {
      return this.setInventorySlot(player, TConstructAPI.getInventoryWrapper(player).getKnapsackInventory(player), slot - OFFSET_KNAPSACK, itemStack);
    } else {
      return this.setInventorySlot(player, TConstructAPI.getInventoryWrapper(player).getAccessoryInventory(player), slot, itemStack);
    }
  }

  @Override
  protected boolean canAdd(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    return (inventory.getStackInSlot(slotID) == null); // TConstruct does not properly implement IsValidForSlot
  }

  @Override
  public boolean add(final EntityPlayer player, final ItemStack itemStack, final boolean isRemote) {
    return false; // if it doesn't fit in the knapsack, then no other slot can take it. Add it to the vanilla inventory instead if possible.
  }

  @Override
  public String getID() {
    return "TConstruct";
  }

  @Override
  public void markDirty(EntityPlayer player, final boolean isRemote) {
    if (isRemote) {
      TConstructAPI.getInventoryWrapper(player).getKnapsackInventory(player).markDirty();
      TConstructAPI.getInventoryWrapper(player).getAccessoryInventory(player).markDirty();
    }
  }
}

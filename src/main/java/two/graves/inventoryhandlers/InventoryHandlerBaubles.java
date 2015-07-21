/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.inventoryhandlers;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import two.graves.API.InventoryContent;
import two.graves.API.InventoryHandlerBase;
import two.graves.util.ItemUtil;

/**
 *
 * @author Two
 */
public class InventoryHandlerBaubles extends InventoryHandlerBase {

  @Override
  public Collection<InventoryContent> removeAllItems(final EntityPlayer player, final boolean isRemote) {
    return this.stripInventory(player, BaublesApi.getBaubles(player));
  }

  @Override
  protected boolean canAdd(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    return (itemStack.getItem() instanceof IBauble)
            && ((IBauble) itemStack.getItem()).canEquip(itemStack, player)
            && super.canAdd(player, inventory, slotID, itemStack);
  }

  @Override
  protected boolean canRemove(final EntityPlayer player, final IInventory inventory, final int slotID, final ItemStack itemStack) {
    return ((itemStack.getItem() instanceof IBauble)
            && ((IBauble) itemStack.getItem()).canUnequip(itemStack, player)
            && super.canRemove(player, inventory, slotID, itemStack));
  }

  @Override
  public boolean set(final EntityPlayer player, final int slot, final ItemStack itemStack, final boolean isRemote) {
    if (isRemote) {
      return this.setInventorySlot(player, BaublesApi.getBaubles(player), slot, itemStack);
    } else {
      return ItemUtil.isSameItem(itemStack, BaublesApi.getBaubles(player).getStackInSlot(slot)); // Baubles syncs on its own
    }
  }

  @Override
  public boolean add(final EntityPlayer player, final ItemStack itemStack, final boolean isRemote) {
    return this.addToInventory(player, BaublesApi.getBaubles(player), itemStack, isRemote);
  }

  @Override
  public String getID() {
    return "Baubles";
  }

  @Override
  public void markDirty(EntityPlayer player, final boolean isRemote) {
    if (isRemote) {
      BaublesApi.getBaubles(player).markDirty();
    }
  }

}

/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.API;

import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Two
 */
public interface IInventoryHandler {

  /**
   * Removes and returns all items from the given player that are handled by this handler.
   *
   * After execution all items that are in the result value must have been stripped
   * from the player. All further items are subject to the usual death routine,
   * which means they are most likely going to be spilled out into the world (if
   * not handled by something else).
   *
   * Note that unless you implement your own synchronization functionality, this
   * should be executed on both client and server side.
   *
   * @param player The player to take the items from.
   * @param isRemote true if this is called on the server, false if it is called on the client.
   * @return A collection containing all the items that have been removed from the player and should be placed into the grave.
   */
  public Collection<InventoryContent> removeAllItems(final EntityPlayer player, final boolean isRemote);

  /**
   * Tries to set the exact slot of an inventory related to the player to itemStack.
   *
   * If for that slot is occupied or cannot be set for some other reason, false
   * must be returned. It is not allowed to try to add the item in any other way,
   * that case is handled by addToInventory later.
   * 
   * Note that unless you implement your own synchronization functionality, this
   * should be executed on both client and server side.
   *
   * @param player The player to modify.
   * @param slot The slot this must be placed in (if possible).
   * @param itemStack The ItemStack to add.
   * @param isRemote true if this is called on the server, false if it is called on the client.
   * @return true if that exact slot could be set, false otherwise.
   */
  public boolean set(final EntityPlayer player, final int slot, final ItemStack itemStack, final boolean isRemote);

  /**
   * Tries to add the given itemStack an inventory related to player.
   *
   * This is called if the specific slot above failed, and is allowed to
   * add the item to any suitable slot. If that is not possible (or reasonable)
   * this method must return false, in which case the item is added to the
   * generic (vanilla) player inventory (if possible).
   *
   * Note that unless you implement your own synchronization functionality, this
   * should be executed on both client and server side.
   *
   * @param player The player to modify.
   * @param itemStack The ItemStack to add.
   * @param isRemote true if this is called on the server, false if it is called on the client.
   * @return true if the items was added to any valid inventory slot, false otherwise.
   */
  public boolean add(final EntityPlayer player, final ItemStack itemStack, final boolean isRemote);

  /**
   * Called to notify the handler of inventory changes.
   * Should call markDirty on the handled inventory to sync changes.
   *
   * Note that unless you implement your own synchronization functionality, this
   * should be executed on both client and server side.
   *
   * @param player The player who's inventory has changed.
   * @param isRemote true if this is called on the server, false if it is called on the client.
   */
  public void markDirty(final EntityPlayer player, final boolean isRemote);

  /**
   * Returns the ID of this InventoryHandler.
   *
   * @return The ID of this InventoryHandler.
   */
  public String getID();
}

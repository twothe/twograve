/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.API;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author Two
 */
public class InventoryContent {

  protected final String handlerID;
  protected final int slot;
  protected final ItemStack itemStack;

  /**
   * Creates a new InventoryContent data set.
   * @param handlerID The ID that will handle this content. Must be a valid handlerID as registered with InventoryHandlerRegistry.
   * @param slot The slot this was taken from/is supposed to be put in.
   * @param itemStack The actual item that was removed. Must not be null.
   */
  public InventoryContent(final String handlerID, final int slot, final ItemStack itemStack) {
    if (handlerID == null) {
      throw new IllegalArgumentException("HandlerID cannot be null");
    }
    if (itemStack == null) {
      throw new IllegalArgumentException("ItemStack cannot be null");
    }
    this.handlerID = handlerID;
    this.slot = slot;
    this.itemStack = itemStack;
  }

  public String getHandlerID() {
    return handlerID;
  }

  public int getSlot() {
    return slot;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  @Override
  public String toString() {
    return (this.itemStack == null ? "null" : this.itemStack.toString()) + " for handler " + this.handlerID + " @Slot " + this.slot;
  }

  /** *********************************************************************** */
  /** Streaming ************************************************************* */
  /** *********************************************************************** */
  public static String KEY_TARGET = "InventoryTarget";
  public static String KEY_SLOT = "InventorySlot";

  public static InventoryContent readFromNBT(final NBTTagCompound tagCompound) {
    return new InventoryContent(
            tagCompound.getString(KEY_TARGET),
            tagCompound.getInteger(KEY_SLOT),
            ItemStack.loadItemStackFromNBT(tagCompound)
    );
  }

  public void writeToNBT(final NBTTagCompound tagCompound) {
    tagCompound.setString(KEY_TARGET, this.handlerID);
    tagCompound.setInteger(KEY_SLOT, this.slot);
    this.itemStack.writeToNBT(tagCompound);
  }

  public void toBytes(final ByteBuf buffer) throws Exception {
    ByteBufUtils.writeUTF8String(buffer, this.handlerID);
    buffer.writeInt(this.slot);
    ByteBufUtils.writeItemStack(buffer, this.itemStack);
  }

  public static InventoryContent fromBytes(final ByteBuf buffer) throws Exception {
    final String handlerID = ByteBufUtils.readUTF8String(buffer);
    final int slot = buffer.readInt();
    final ItemStack itemStack = ByteBufUtils.readItemStack(buffer);
    return new InventoryContent(handlerID, slot, itemStack);
  }

}

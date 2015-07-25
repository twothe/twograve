/*
 */
package two.graves.tiles;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.Deque;
import java.util.LinkedList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;
import two.graves.API.IInventoryHandler;
import two.graves.API.InventoryContent;
import two.graves.API.InventoryHandlerRegistry;
import two.graves.Graves;
import two.graves.GravesAssets;
import two.graves.network.PacketForceEquipItems;

/**
 * @author Two
 */
public class TileGrave extends TileEntity {

  protected static final String KEY_OWNER = "GraveOwner";
  protected static final String KEY_INVENTORY = "GraveInventory";
  protected static final String KEY_LOCALIZATION_NOTYOURGRAVE = "notyourgrave";
  protected static final String KEY_LOCALIZATION_UNKNOWN_GRAVE = "unknownGrave";

  protected final LinkedList<InventoryContent> inventoryContent;
  protected String ownerName;

  public TileGrave() {
    super();
    this.inventoryContent = new LinkedList<InventoryContent>();
    this.ownerName = LanguageRegistry.instance().getStringLocalization(KEY_LOCALIZATION_UNKNOWN_GRAVE); // placeholder in case this block is placed in any other way but by a player
  }

  @Override
  public void readFromNBT(final NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);

    this.ownerName = tagCompound.getString(KEY_OWNER);
    this.inventoryContent.clear();
    final NBTTagList inventoryList = tagCompound.getTagList(KEY_INVENTORY, Constants.NBT.TAG_COMPOUND); // we have a list of compounds
    for (int tagCount = inventoryList.tagCount() - 1; tagCount >= 0; --tagCount) {
      final NBTTagCompound inventoryEntry = inventoryList.getCompoundTagAt(tagCount);
      try {
        final InventoryContent content = InventoryContent.readFromNBT(inventoryEntry);
        this.inventoryContent.add(content);
      } catch (Exception e) {
        FMLLog.log(Graves.MOD_ID, Level.ERROR, "Unable to load inventory content entry: %s\n%s", e.toString(), inventoryEntry == null ? "null" : inventoryEntry.toString());
      }
    }
  }

  @Override
  public void writeToNBT(final NBTTagCompound tagCompound) {
    super.writeToNBT(tagCompound);

    tagCompound.setString(KEY_OWNER, this.ownerName);
    final NBTTagList inventoryList = new NBTTagList();
    for (final InventoryContent content : this.inventoryContent) {
      final NBTTagCompound inventoryEntry = new NBTTagCompound();
      content.writeToNBT(inventoryEntry);
      inventoryList.appendTag(inventoryEntry);
    }
    tagCompound.setTag(KEY_INVENTORY, inventoryList);
  }

  public void takePlayerInventory(final EntityPlayer player) {
    this.ownerName = player.getDisplayName();
    for (final IInventoryHandler handler : InventoryHandlerRegistry.getAllHandlers()) {
      this.inventoryContent.addAll(handler.removeAllItems(player, this.worldObj.isRemote));
      handler.markDirty(player, this.worldObj.isRemote);
    }

    if (this.inventoryContent.isEmpty()) {
      this.destroyThisGrave();
    } else {
      this.markDirty();
    }
  }

  /* Called from the server-side block */
  public void giveItemsToPlayer(final EntityPlayer player) {
    if (this.ownerName.equals(player.getDisplayName())) {
      if (player instanceof EntityPlayerMP) {
        final PacketForceEquipItems packet = new PacketForceEquipItems(this.inventoryContent);
        Graves.networkChannel.sendTo(packet, (EntityPlayerMP) player); // have the same happen on the client to synchronize inventories
      }

      if (TileGrave.giveItemsToPlayer(player, this.inventoryContent, true)) {
        this.destroyThisGrave();
      } else {
        this.markDirty();
      }

      for (final IInventoryHandler handler : InventoryHandlerRegistry.getAllHandlers()) {
        handler.markDirty(player, true);
      }

    } else {
      final String message = String.format(LanguageRegistry.instance().getStringLocalization(KEY_LOCALIZATION_NOTYOURGRAVE), this.ownerName);
      player.addChatComponentMessage(new ChatComponentText(message));
    }
  }

  /* Called from the server-side block and the client-side packet handler */
  public static boolean giveItemsToPlayer(final EntityPlayer player, final Deque<InventoryContent> inventoryList, final boolean remote) {
    int count = GravesAssets.itemsPerTick >= 0 ? GravesAssets.itemsPerTick : Integer.MAX_VALUE;
    if (count == 0) {
      return true;
    }
    InventoryContent content;
    final Deque<InventoryContent> notAdded = new LinkedList<InventoryContent>();

    while ((count-- > 0) && ((content = inventoryList.poll()) != null)) {
      try {
        final IInventoryHandler handler = InventoryHandlerRegistry.getHandlerForID(content.getHandlerID());
        if (handler != null) {
          if (handler.set(player, content.getSlot(), content.getItemStack(), remote) == false) {
            notAdded.add(content);
          }
        } else {
          FMLLog.log(Graves.MOD_ID, Level.WARN, "No handler registered for ID '%s'. The item '%s' is lost.", content.getHandlerID(), content.getItemStack().getDisplayName());
        }
      } catch (Exception e) {
        FMLLog.log(Graves.MOD_ID, Level.ERROR, "Failed to add item to inventory: %s\n%s", e.toString(), content.toString());
      }
    }

    final IInventoryHandler vanillaHandler = InventoryHandlerRegistry.getHandlerForID(InventoryHandlerRegistry.INVENTORY_HANDLER_ID_VANILLA);
    for (InventoryContent leftOverContent : notAdded) {
      final IInventoryHandler handler = InventoryHandlerRegistry.getHandlerForID(leftOverContent.getHandlerID());
      if (handler != null) {
        if (handler.add(player, leftOverContent.getItemStack(), remote) == false) {
          if ((handler == vanillaHandler) || (vanillaHandler.add(player, leftOverContent.getItemStack(), remote) == false)) {
            inventoryList.add(leftOverContent); // return the item to the grave for later
          }
        }
      } else {
        FMLLog.log(Graves.MOD_ID, Level.WARN, "No handler registered for ID '%s'. The item '%s' is lost.", leftOverContent.getHandlerID(), leftOverContent.getItemStack().getDisplayName());
      }
    }

    return inventoryList.isEmpty();
  }

  public void spillOutInventory() {
    for (final InventoryContent content : this.inventoryContent) {
      spillOutItem(content.getItemStack());
    }
    this.inventoryContent.clear();
    this.markDirty();
  }

  public void spillOutItem(final ItemStack itemstack) {
    if (itemstack != null) {
      final float modX = worldObj.rand.nextFloat() * 0.8F + 0.1F;
      final float modY = worldObj.rand.nextFloat() * 0.8F + 0.1F;
      final float modZ = worldObj.rand.nextFloat() * 0.8F + 0.1F;

      while (itemstack.stackSize > 0) {
        int stackSplit = worldObj.rand.nextInt(21) + 10;

        if (stackSplit > itemstack.stackSize) {
          stackSplit = itemstack.stackSize;
        }

        itemstack.stackSize -= stackSplit;
        final EntityItem entityitem = new EntityItem(worldObj, (double) ((float) xCoord + modX), (double) ((float) yCoord + modY), (double) ((float) zCoord + modZ), new ItemStack(itemstack.getItem(), stackSplit, itemstack.getItemDamage()));

        if (itemstack.hasTagCompound()) {
          entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
        }

        final float baseVelocity = 0.05F;
        entityitem.motionX = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity);
        entityitem.motionY = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity + 0.2F);
        entityitem.motionZ = (double) ((float) worldObj.rand.nextGaussian() * baseVelocity);
        worldObj.spawnEntityInWorld(entityitem);
      }
    }
  }

  protected void destroyThisGrave() {
    if (this.worldObj.isRemote == false) {
      this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
    }
  }
}

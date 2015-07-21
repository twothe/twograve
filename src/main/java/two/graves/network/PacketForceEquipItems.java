/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.network;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.util.Deque;
import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;
import two.graves.API.InventoryContent;
import two.graves.Graves;
import two.graves.tiles.TileGrave;

/**
 *
 * @author Two
 */
public class PacketForceEquipItems extends ForceEquipMessageHandlerBase implements IMessage {

  protected final Deque<InventoryContent> inventoryContent;

  public PacketForceEquipItems(final Deque<InventoryContent> inventoryContent) {
    this.inventoryContent = inventoryContent == null ? new LinkedList<InventoryContent>() : new LinkedList<InventoryContent>(inventoryContent);
  }

  /* Forge requires a default constructor for initialization from stream. */
  public PacketForceEquipItems() {
    this(null);
  }

  @Override
  public void fromBytes(final ByteBuf buf) {
    int count = buf.readInt();
    while (count-- > 0) {
      try {
        final InventoryContent content = InventoryContent.fromBytes(buf);
        this.inventoryContent.add(content);
      } catch (Exception e) {
        FMLLog.log(Graves.MOD_ID, Level.ERROR, e, "Unable to read Grave content properly");
      }
    }
  }

  @Override
  public void toBytes(final ByteBuf buf) {
    buf.writeInt(this.inventoryContent.size());
    for (final InventoryContent content : this.inventoryContent) {
      try {
        content.toBytes(buf);
      } catch (Exception e) {
        FMLLog.log(Graves.MOD_ID, Level.ERROR, e, "Unable to package Grave content %s", content.toString());
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public PacketForceEquipItems onMessage(final PacketForceEquipItems message, final MessageContext ctx) {
    if (ctx.side == Side.CLIENT) {
      TileGrave.giveItemsToPlayer(Minecraft.getMinecraft().thePlayer, message.inventoryContent, false);
      return null; // no answer required
    } else {
      throw new IllegalStateException("Received " + PacketForceEquipItems.class.getSimpleName() + " for " + ctx.side.toString());
    }
  }
}

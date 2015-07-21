/*
 *  (c) Two aka Stefan Feldbinder
 */
package two.graves.network;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 *
 * @author Two
 */
public class ForceEquipMessageHandlerBase implements IMessageHandler<PacketForceEquipItems, PacketForceEquipItems> {

  @Override
  public PacketForceEquipItems onMessage(PacketForceEquipItems message, MessageContext ctx) {
    return null;
  }

}

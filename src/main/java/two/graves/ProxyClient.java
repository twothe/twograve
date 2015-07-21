/*
 */
package two.graves;

import cpw.mods.fml.relauncher.Side;
import two.graves.network.PacketForceEquipItems;
import two.graves.util.ItemUtil;

/**
 * @author Two
 */
public class ProxyClient extends ProxyBase {

  @Override
  public void onInit() {
    super.onInit();

    ItemUtil.clearCachedTooltips();
  }
}

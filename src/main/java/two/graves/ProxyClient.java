/*
 */
package two.graves;

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

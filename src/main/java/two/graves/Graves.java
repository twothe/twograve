/*
 * Copyright (c) by Stefan Feldbinder aka Two
 */
package two.graves;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 *
 * @author Two
 */
@Mod(modid = Graves.MOD_ID, name = Graves.MOD_NAME, version = Graves.MOD_VERSION)
public class Graves {

  public static final String MOD_NAME = "Graves";
  public static final String MOD_ID = "TwoGraves";
  public static final String MOD_VERSION = "1710.1.0";
  //----------------------------------------------------------------------------
  @Mod.Instance("TwoGraves")
  public static Graves instance;
  @SidedProxy(clientSide = "two.graves.ProxyClient", serverSide = "two.graves.ProxyServer")
  public static ProxyBase proxy;
  public static final Config config = new Config();
  public static final SimpleNetworkWrapper networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
  
  public static boolean baublesEnabled = false;

  public static String getTextureName(final String filePrefix) {
    return Graves.MOD_ID + ":" + filePrefix;
  }

  public static String getSoundName(final String soundName) {
    return Graves.MOD_ID + ":" + soundName;
  }

  public static String getTooltipName(final String itemName) {
    return getTooltipName(itemName, null);
  }

  public static String getTooltipName(final String itemName, final String suffix) {
    return "item." + itemName + ".tooltip" + (suffix == null || suffix.length() == 0 ? "" : "." + suffix);
  }

  public static String getEntityName(final String name) {
    return "entity." + name;
  }

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    config.initialize(event.getSuggestedConfigurationFile());

    proxy.onPreInit();
  }

  @Mod.EventHandler
  public void load(final FMLInitializationEvent event) {
    config.load();
    proxy.onInit();
    config.save();
  }

  @Mod.EventHandler
  public void postInit(final FMLPostInitializationEvent event) {
    proxy.onPostInit();
  }
}

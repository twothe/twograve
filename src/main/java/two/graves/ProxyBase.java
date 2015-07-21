/*
 */
package two.graves;

import two.graves.inventoryhandlers.InventoryHandlerBaubles;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import two.graves.blocks.BlockGrave;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import two.graves.API.InventoryHandlerRegistry;
import two.graves.API.InventoryHandlerVanilla;
import two.graves.inventoryhandlers.InventoryHandlerTConstruct;
import two.graves.network.PacketForceEquipItems;
import two.graves.tiles.TileGrave;

/**
 * @author Two
 */
public class ProxyBase {

  /* Config */
  public final String KEY_ITEMS_PER_SECOND = "Items a player can take each tick";
  public final String KEY_PLAYER_CAN_DESTROY_GRAVE = "Players can destroy graves";
  public final String KEY_HANDLE_BAUBLES = "Handle Baubles items";
  public final String KEY_HANDLE_TCONSTRUCT = "Handle Tinker's Construct items";
  /* Initialization list for content that needs post-initialization. */
  protected ArrayList<InitializableModContent> pendingInitialization = new ArrayList<InitializableModContent>();
  protected final AtomicInteger networkID = new AtomicInteger(0);

  public ProxyBase() {
  }

  protected void loadGlobalConfigValues() {
    GravesAssets.itemsPerTick = Graves.config.getMiscInteger(KEY_ITEMS_PER_SECOND, -1);
    GravesAssets.playerCanDestroyGave = Graves.config.getMiscBoolean(KEY_PLAYER_CAN_DESTROY_GRAVE, true);
    GravesAssets.handleBaubles = Graves.config.getMiscBoolean(KEY_HANDLE_BAUBLES, true);
    GravesAssets.handleTConstruct = Graves.config.getMiscBoolean(KEY_HANDLE_TCONSTRUCT, true);
  }

  protected void registerBlocks() {
    GravesAssets.blockGrave = new BlockGrave();
    pendingInitialization.add(GravesAssets.blockGrave);
  }

  protected void registerItems() {
  }

  protected void registerRenderers() {
  }

  protected void registerNetwork() {
    final int graveID = networkID.getAndIncrement();
    Graves.networkChannel.registerMessage(PacketForceEquipItems.class, PacketForceEquipItems.class, graveID, Side.CLIENT);
    Graves.networkChannel.registerMessage(PacketForceEquipItems.class, PacketForceEquipItems.class, graveID, Side.SERVER);
  }

  protected void registerInventoryHandlers() {
    if (InventoryHandlerRegistry.isHandlerRegistered(InventoryHandlerRegistry.INVENTORY_HANDLER_ID_VANILLA) == false) {
      throw new IllegalStateException("InventoryHandlerVanilla not registered"); // this is mostly to initialize the vanilla handler class
    }
  }

  public void onPreInit() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  public void onInit() {
    loadGlobalConfigValues();
    registerBlocks();
    registerItems();
    registerRenderers();
    registerNetwork();
    registerInventoryHandlers();

    for (final InitializableModContent content : pendingInitialization) {
      content.initialize();
    }
    pendingInitialization.clear();
  }

  public void onPostInit() {
    if (GravesAssets.handleBaubles && Loader.isModLoaded("Baubles")) {
      InventoryHandlerRegistry.registerHandler(new InventoryHandlerBaubles());
    }
    if (GravesAssets.handleTConstruct && Loader.isModLoaded("TConstruct")) {
      InventoryHandlerRegistry.registerHandler(new InventoryHandlerTConstruct());
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
  public void OnDeath(final LivingDeathEvent event) {
    if (event.entityLiving instanceof EntityPlayer) {
      final EntityPlayer player = (EntityPlayer) event.entityLiving;
      if ((player != null) && (player.worldObj.isRemote == false)) {
        final ChunkCoordinates deathChestPos = findNearestAirBlock(player);
        if (deathChestPos != null) {
          if (player.worldObj.setBlock(deathChestPos.posX, deathChestPos.posY, deathChestPos.posZ, GravesAssets.blockGrave, 0, 1 + 2)) {
            final TileEntity tileEntity = player.worldObj.getTileEntity(deathChestPos.posX, deathChestPos.posY, deathChestPos.posZ);
            if (tileEntity instanceof TileGrave) {
              ((TileGrave) tileEntity).takePlayerInventory(player);
            } else {
              FMLLog.severe("Placed Grave, expected %s, but got: %s", TileGrave.class.getName(), tileEntity == null ? "null" : tileEntity.getClass().getName());
            }
          } else {
            FMLLog.severe("Unable to place Grave at %s", deathChestPos.toString());
          }
        }
      }
    }
  }

  protected static ChunkCoordinates findNearestAirBlock(final EntityPlayer player) {
    return new ChunkCoordinates((int) player.posX, (int) player.posY, (int) player.posZ);
  }
}

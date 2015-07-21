/*
 */
package two.graves.blocks;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import two.graves.Graves;
import two.graves.GravesAssets;
import two.graves.tiles.TileGrave;
import two.graves.util.BlockUtil;

/**
 * @author Two
 */
public class BlockGrave extends BlockBase implements ITileEntityProvider {

  protected static final float PIXEL_SIZE = 1.0f / 16.0f;
  public static final String NAME = "grave";
  protected static final Class<TileGrave> tileEntityClass = TileGrave.class;
//-- Class -------------------------------------------------------------------
  protected IIcon textureFront, textureSides;

  public BlockGrave() {
    super(Blocks.stone.getMaterial());
    GameRegistry.registerTileEntity(tileEntityClass, tileEntityClass.getName());
  }

  @Override
  public void initialize() {
    setBaseValues(NAME, null, soundTypeStone, 3.0F, BlockUtil.HARVEST_TOOL_AXE, BlockUtil.HARVEST_LEVEL_STONE);
    setResistance(6000.0F); // Explosion resistance as Obsidian
    if (GravesAssets.playerCanDestroyGave == false) {
      setBlockUnbreakable();
    }
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
    if (world.isRemote == false) {
      final TileEntity tileEntity = world.getTileEntity(x, y, z);
      if (tileEntityClass.isInstance(tileEntity)) {
        tileEntityClass.cast(tileEntity).giveItemsToPlayer(player);
      } else {
        FMLLog.log(Graves.MOD_ID, Level.ERROR, "Grave TileEntity expected %s, but found %s", tileEntityClass.getName(), tileEntity == null ? "null" : tileEntity.getClass().getName());
      }
    }
    return true;
  }

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metadata) {
    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      tileEntityClass.cast(tileEntity).spillOutInventory();
    } else {
      FMLLog.log(Graves.MOD_ID, Level.ERROR, "Grave TileEntity expected %s, but found %s", tileEntityClass.getName(), tileEntity == null ? "null" : tileEntity.getClass().getName());
    }

    super.breakBlock(world, x, y, z, block, metadata);
  }

  @Override
  public void onBlockDestroyedByExplosion(final World world, final int x, final int y, final int z, final Explosion explosion) {
    final TileEntity tileEntity = world.getTileEntity(x, y, z);
    if (tileEntityClass.isInstance(tileEntity)) {
      tileEntityClass.cast(tileEntity).spillOutInventory();
    } else {
      FMLLog.log(Graves.MOD_ID, Level.ERROR, "Grave TileEntity expected %s, but found %s", tileEntityClass.getName(), tileEntity == null ? "null" : tileEntity.getClass().getName());
    }
  }

  @Override
  public TileEntity createNewTileEntity(final World world, final int i) {
    try {
      return tileEntityClass.newInstance();
    } catch (Exception ex) {
      FMLLog.log(Graves.MOD_ID, Level.ERROR, ex, "Creation of %s failed!", tileEntityClass.getName());
      return null;
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister iconRegister) {
    textureFront = iconRegister.registerIcon(Graves.getTextureName(NAME + "_front"));
    textureSides = iconRegister.registerIcon(Graves.getTextureName(NAME + "_sides"));
  }

  @SideOnly(Side.CLIENT)
  @Override
  public IIcon getIcon(final int side, final int metadata) {
    return (side == 2) ? textureFront : textureSides;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public void addCollisionBoxesToList(final World world, final int x, final int y, final int z, final AxisAlignedBB boundingBox, final List list, final Entity entity) {
    this.setBlockBoundsBasedOnState(world, x, y, z);
    super.addCollisionBoxesToList(world, x, y, z, boundingBox, list, entity);
  }

  @Override
  public void setBlockBoundsBasedOnState(final IBlockAccess world, final int x, final int y, final int z) {
    this.setBlockBounds(
            2 * PIXEL_SIZE, 0.0F, 1.0f - 3 * PIXEL_SIZE,
            1.0f - 2 * PIXEL_SIZE, 1.0f - 2 * PIXEL_SIZE, 1.0f - PIXEL_SIZE
    );
  }

  @Override
  public void setBlockBoundsForItemRender() {
    this.setBlockBounds(0.375F, 0.0F, 0.0F, 0.625F, 1.0F, 1.0F);
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }
}

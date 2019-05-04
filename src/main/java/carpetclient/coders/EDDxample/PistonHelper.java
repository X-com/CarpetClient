package carpetclient.coders.EDDxample;


import carpetclient.Config;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A class to help visualize piston update order. Code provided by EDDxample.
 */
public class PistonHelper {

    private static final String gold = "\u00a76", red = "\u00a74===", green = "\u00a72Blocks", pushe = "\u00a76Pushes", pull = "\u00a76Pull";

    public static boolean validState, activated, extending;
    public static BlockPos pistonPos;
    private static BlockPos[] tobreak, tomove;

    /**
     * Sets the piston update order by first removing the piston head.
     *
     * @param worldIn   current player world
     * @param state     block state of the piston being activated
     * @param pos       location of the piston being activated
     * @param extending extending or retracting piston
     */
    public static void setPistonMovement(World worldIn, IBlockState state, BlockPos pos, boolean extending) {
        EnumFacing enumfacing = (EnumFacing) state.getValue(BlockDirectional.FACING);
        IBlockState state1 = worldIn.getBlockState(pos.offset(enumfacing));
        BlockPistonStructureHelper ph = null;

        //Weird trick to remove the piston head
        if (!extending) {
            worldIn.setBlockState(pos, Blocks.BARRIER.getDefaultState(), 2);
            worldIn.setBlockToAir(pos);
            worldIn.setBlockToAir(pos.offset(enumfacing));
        }

        ph = new BlockPistonStructureHelper(worldIn, pos, enumfacing, extending);
        boolean canMove = ph.canMove();
        int storeLimit = Config.pushLimit;
        Config.pushLimit = Integer.MAX_VALUE;
        ph.canMove();
        PistonHelper.set(pos, ph.getBlocksToMove().toArray(new BlockPos[ph.getBlocksToMove().size()]), ph.getBlocksToDestroy().toArray(new BlockPos[ph.getBlocksToDestroy().size()]), canMove, extending);
        Config.pushLimit = storeLimit;
        PistonHelper.activated = true;

        //Weird trick to add the piston head back
        if (!extending) {
            worldIn.setBlockState(pos, state, 2);
            worldIn.setBlockState(pos.offset(enumfacing), state1, 2);
        }
    }

    /**
     * Set logic of the piston data.
     *
     * @param posIn
     * @param btm
     * @param btb
     * @param isValid
     * @param _extending
     */
    private static void set(BlockPos posIn, BlockPos[] btm, BlockPos[] btb, boolean isValid, boolean _extending) {
        pistonPos = posIn;
        tomove = btm;
        tobreak = btb;
        validState = isValid;
        extending = _extending;
    }

    /**
     * Draws the piston update order and other details.
     *
     * @param partialTicks tick sense last render
     */
    public static void draw(float partialTicks) {
        if (Config.pistonVisualizer.getValue() && activated) {
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            final RenderManager rm = Minecraft.getMinecraft().getRenderManager();
            BlockPos pos;

            int count = 0;
            for (int i = 1; i <= tobreak.length; i++) {
                pos = tobreak[tobreak.length - i];
                if (pos != null) {
                    count++;
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, "\u00a7c" + count, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                }

            }
            int moved = -count;
            for (int i = 1; i <= tomove.length; i++) {
                pos = tomove[tomove.length - i];
                if (pos != null) {
                    count++;
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, Integer.toString(count), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                }
            }
            moved += count;
            pos = pistonPos;
            if (validState) {
                if (extending) {
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                } else {
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                }
                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, green, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
            } else {
                if (extending) {
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                } else {
                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
                }
                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, red, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
            }
            EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, gold + (moved < 0 ? 0 : moved), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
        }
    }
}


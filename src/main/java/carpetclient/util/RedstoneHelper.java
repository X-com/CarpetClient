package carpetclient.util;

import carpetclient.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RedstoneHelper {

    public static void blockClicked(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing) {
        System.out.println("Test power at: " + new RedstoneHelper().isWirePowered(worldIn, pos));
    }

    private void calcWireOrder() {
//        if (blockPowered()) {
//            calcDown();
//            calcUp();
//        } else {
//            calcUp();
//            calcDown();
//        }
    }

    private void calcUp(World worldIn, BlockPos pos) {

    }

    private void calcDown() {
    }

    private boolean blockPowered(IBlockState state) {
        if (state.getBlock().getMetaFromState(state) != 0) {
            return true;
        }
        return false;
    }

    private boolean isWirePowered(IBlockAccess worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (isPowerSourceAt(worldIn, pos, enumfacing)) return true;
        }

        return false;
    }

    private boolean isPowerSourceAt(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        BlockPos blockpos = pos.offset(side);
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        boolean normalCube = iblockstate.isNormalCube();

        if (canConnectTo(iblockstate, side, false)) {
            return true;
        } else if (normalCube) {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                if (!enumfacing.equals(side.getOpposite()) && canConnectTo(worldIn.getBlockState(blockpos.offset(enumfacing)), enumfacing, normalCube)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static boolean canConnectTo(IBlockState blockState, @Nullable EnumFacing side, boolean normalCube) {
        Block block = blockState.getBlock();

        if (block == Blocks.REDSTONE_WIRE) {
            return false;
        } else if (Blocks.UNPOWERED_REPEATER.isSameDiode(blockState)) {
            EnumFacing enumfacing = (EnumFacing) blockState.getValue(BlockRedstoneRepeater.FACING);
            return enumfacing == side;
        } else if (Blocks.UNPOWERED_COMPARATOR.isSameDiode(blockState)) {
            EnumFacing enumfacing = (EnumFacing) blockState.getValue(BlockRedstoneRepeater.FACING);
            return enumfacing == side;
        } else if (Blocks.OBSERVER == blockState.getBlock()) {
            return side == blockState.getValue(BlockObserver.FACING);
        } else {
            return !normalCube && blockState.canProvidePower();
        }
    }

    public static void draw(float partialTicks) {
        if (Config.pistonVisualizer) {
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            final RenderManager rm = Minecraft.getMinecraft().getRenderManager();
            BlockPos pos;

//            int count = 0;
//            for (int i = 1; i <= tobreak.length; i++) {
//                pos = tobreak[tobreak.length - i];
//                if (pos != null) {
//                    count++;
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, "\u00a7c" + count, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//
//            }
//            int moved = -count;
//            for (int i = 1; i <= tomove.length; i++) {
//                pos = tomove[tomove.length - i];
//                if (pos != null) {
//                    count++;
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, Integer.toString(count), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//            }
//            moved += count;
//            pos = pistonPos;
//            if (validState) {
//                if (extending) {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                } else {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, green, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//            } else {
//                if (extending) {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pushe, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                } else {
//                    EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, pull, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.8f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//                }
//                EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, red, (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.2f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
//            }
//            EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer, gold + (moved < 0 ? 0 : moved), (float) (pos.getX() + 0.5f - d0), (float) (pos.getY() + 0.5f - d1), (float) (pos.getZ() + 0.5f - d2), 0, rm.playerViewY, rm.playerViewX, rm.options.thirdPersonView == 2, false);
        }
    }

    ////////////////// vanilla code //////////////////////

    HashMap<BlockPos, IBlockState> blocks = new HashMap<>();

    public class SimulatedRedstoneWire extends Block {

        private final Set<BlockPos> blocksNeedingUpdate = Sets.<BlockPos>newHashSet();
        public final PropertyInteger POWER = BlockRedstoneWire.POWER;
        private boolean canProvidePower = true;

        public SimulatedRedstoneWire(Material blockMaterialIn, MapColor blockMapColorIn) {
            super(blockMaterialIn, blockMapColorIn);
        }

        private IBlockState getBlockState(BlockPos pos) {
            return blocks.get(pos);
        }

        public void setBlockState(BlockPos pos, IBlockState newState) {
            if (this.isOutsideBuildHeight(pos)) {
                return;
            } else {
                blocks.put(pos, newState);
                updateObservingBlocksAt(pos, newState.getBlock());
            }
        }

        public void updateObservingBlocksAt(BlockPos pos, Block blockType) {
            observedNeighborChanged(pos.west(), blockType, pos);
            observedNeighborChanged(pos.east(), blockType, pos);
            observedNeighborChanged(pos.down(), blockType, pos);
            observedNeighborChanged(pos.up(), blockType, pos);
            observedNeighborChanged(pos.north(), blockType, pos);
            observedNeighborChanged(pos.south(), blockType, pos);
        }

        private void observedNeighborChanged(BlockPos west, Block blockType, BlockPos pos) {
            System.out.println("observer gets updated here");
        }

        public void neighborChanged(IBlockState state, BlockPos pos, Block blockIn, BlockPos fromPos) {
            if (this.canPlaceBlockAt(pos)) {
                this.updateSurroundingRedstone(pos, state);
            } else {
//                this.dropBlockAsItem(worldIn, pos, state, 0);
//                worldIn.setBlockToAir(pos);
            }
        }

        public boolean canPlaceBlockAt(BlockPos pos) {
            return getBlockState(pos.down()).isTopSolid() || getBlockState(pos.down()).getBlock() == Blocks.GLOWSTONE;
        }

        private IBlockState updateSurroundingRedstone(BlockPos pos, IBlockState state) {
            state = this.calculateCurrentChanges(pos, pos, state);
            List<BlockPos> list = Lists.newArrayList(this.blocksNeedingUpdate);
            this.blocksNeedingUpdate.clear();

            for (BlockPos blockpos : list) {
                notifyNeighborsOfStateChange(blockpos, this);
            }

            return state;
        }

        public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
            neighborChanged2(pos.west(), blockType, pos);
            neighborChanged2(pos.east(), blockType, pos);
            neighborChanged2(pos.down(), blockType, pos);
            neighborChanged2(pos.up(), blockType, pos);
            neighborChanged2(pos.north(), blockType, pos);
            neighborChanged2(pos.south(), blockType, pos);
        }

        private void neighborChanged2(BlockPos newPos, Block blockType, BlockPos fromPos) {
            neighborChanged(getBlockState(newPos), newPos, blockType, fromPos);
        }

        public boolean isBlockPowered(BlockPos pos) {
            if (this.getRedstonePower(pos.down(), EnumFacing.DOWN) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.up(), EnumFacing.UP) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.north(), EnumFacing.NORTH) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.south(), EnumFacing.SOUTH) > 0) {
                return true;
            } else if (this.getRedstonePower(pos.west(), EnumFacing.WEST) > 0) {
                return true;
            } else {
                return this.getRedstonePower(pos.east(), EnumFacing.EAST) > 0;
            }
        }

        public int getRedstonePower(BlockPos pos, EnumFacing facing) {
            IBlockState iblockstate = this.getBlockState(pos);
//            if(iblockstate.getBlock() instanceof BlockRedstoneWire) return 0;
            return iblockstate.isNormalCube() ? getStrongPower(pos) : iblockstate.getBlock().getWeakPower(iblockstate, null, pos, facing);
        }

        public int getStrongPower(BlockPos pos, EnumFacing facing)
        {
            return getBlockState(pos).getBlock().getStrongPower(getBlockState(pos), null, pos, facing);
        }

        public int getStrongPower(BlockPos pos)
        {
            int i = 0;
            i = Math.max(i, this.getStrongPower(pos.down(), EnumFacing.DOWN));

            if (i >= 15)
            {
                return i;
            }
            else
            {
                i = Math.max(i, this.getStrongPower(pos.up(), EnumFacing.UP));

                if (i >= 15)
                {
                    return i;
                }
                else
                {
                    i = Math.max(i, this.getStrongPower(pos.north(), EnumFacing.NORTH));

                    if (i >= 15)
                    {
                        return i;
                    }
                    else
                    {
                        i = Math.max(i, this.getStrongPower(pos.south(), EnumFacing.SOUTH));

                        if (i >= 15)
                        {
                            return i;
                        }
                        else
                        {
                            i = Math.max(i, this.getStrongPower(pos.west(), EnumFacing.WEST));

                            if (i >= 15)
                            {
                                return i;
                            }
                            else
                            {
                                i = Math.max(i, this.getStrongPower(pos.east(), EnumFacing.EAST));
                                return i >= 15 ? i : i;
                            }
                        }
                    }
                }
            }
        }

        public int isBlockIndirectlyGettingPowered(BlockPos pos) {
            int i = 0;
            EnumFacing[] var3 = EnumFacing.values();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                EnumFacing enumfacing = var3[var5];
                int j = this.getRedstonePower(pos.offset(enumfacing), enumfacing);
                if (j >= 15) {
                    return 15;
                }

                if (j > i) {
                    i = j;
                }
            }

            return i;
        }

        private IBlockState calculateCurrentChanges(BlockPos pos1, BlockPos pos2, IBlockState state) {
            IBlockState iblockstate = state;
            int i = ((Integer) state.getValue(POWER)).intValue();
            int j = 0;
            j = this.getMaxCurrentStrength(pos2, j);
            this.canProvidePower = false;
            int k = isBlockIndirectlyGettingPowered(pos1);
            this.canProvidePower = true;

            if (k > 0 && k > j - 1) {
                j = k;
            }

            int l = 0;

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                BlockPos blockpos = pos1.offset(enumfacing);
                boolean flag = blockpos.getX() != pos2.getX() || blockpos.getZ() != pos2.getZ();

                if (flag) {
                    l = this.getMaxCurrentStrength(blockpos, l);
                }

                if (getBlockState(blockpos).isNormalCube() && !getBlockState(pos1.up()).isNormalCube()) {
                    if (flag && pos1.getY() >= pos2.getY()) {
                        l = this.getMaxCurrentStrength(blockpos.up(), l);
                    }
                } else if (!getBlockState(blockpos).isNormalCube() && flag && pos1.getY() <= pos2.getY()) {
                    l = this.getMaxCurrentStrength(blockpos.down(), l);
                }
            }

            if (l > j) {
                j = l - 1;
            } else if (j > 0) {
                --j;
            } else {
                j = 0;
            }

            if (k > j - 1) {
                j = k;
            }

            if (i != j) {
                state = state.withProperty(POWER, Integer.valueOf(j));

                if (getBlockState(pos1) == iblockstate) {
                    setBlockState(pos1, state);
                }

                this.blocksNeedingUpdate.add(pos1);

                for (EnumFacing enumfacing1 : EnumFacing.values()) {
                    this.blocksNeedingUpdate.add(pos1.offset(enumfacing1));
                }
            }

            return state;
        }

        private int getMaxCurrentStrength(BlockPos pos, int strength) {
            if (getBlockState(pos).getBlock() != this) {
                return strength;
            } else {
                int i = ((Integer) getBlockState(pos).getValue(POWER)).intValue();
                return i > strength ? i : strength;
            }
        }

        private boolean isOutsideBuildHeight(BlockPos pos) {
            return pos.getY() < 0 || pos.getY() >= 256;
        }
    }
}

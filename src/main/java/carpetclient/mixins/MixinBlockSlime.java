package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.coders.EDDxample.PistonHelper;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockSlime.class)
public class MixinBlockSlime extends BlockBreakable {

    protected MixinBlockSlime(Material materialIn, boolean ignoreSimilarityIn) {
        super(materialIn, ignoreSimilarityIn);
    }

    /**
     * Add block activation to get piston update order, code provided by EDDxample.
     *
     * @param worldIn
     * @param pos
     * @param state
     * @param playerIn
     * @param hand
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!Config.pistonVisualizer) return false;

        boolean isSticky = false;
        boolean extending = true;
        boolean flag = playerIn.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && playerIn.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.AIR;

        if (worldIn.isRemote && flag) {
            pos = pos.offset(facing);
            state = new BlockPistonBase(false).getDefaultState().withProperty(BlockPistonBase.FACING, facing.getOpposite()).withProperty(BlockPistonBase.EXTENDED, Boolean.valueOf(false));
            if ((!PistonHelper.activated || !pos.equals(PistonHelper.pistonPos)) && (extending || isSticky)) {
                PistonHelper.setPistonMovement(worldIn, state, pos, extending);
            } else {
                PistonHelper.activated = false;
            }
        }

        if (worldIn.isRemote) {
            return isSticky || !(Boolean) state.getValue(BlockPistonBase.EXTENDED);
        }

        return flag;
    }
}

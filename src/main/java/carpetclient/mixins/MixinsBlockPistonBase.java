package carpetclient.mixins;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/*
Mixen class to make piston/sticky-piston properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockPistonBase.class)
public class MixinsBlockPistonBase extends BlockDirectional {
    
    @Shadow private void checkForMove(World worldIn, BlockPos pos, IBlockState state) { }
    @Shadow public static @Final PropertyBool EXTENDED;

    protected MixinsBlockPistonBase(Material materialIn) {
        super(materialIn);
    }

    // Override this method to comment out a useless line.
    @Overwrite
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        //worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);

        if (!worldIn.isRemote) {
            this.checkForMove(worldIn, pos, state);
        }
    }

    @Overwrite
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // rotate piston/sticky-piston based on hotkeys

        if (!GuiScreen.isCtrlKeyDown()) {
            facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
        }
        if (!GuiScreen.isAltKeyDown()) {
            facing = facing.getOpposite();
        }
        return this.getDefaultState().withProperty(FACING, facing).withProperty(EXTENDED, Boolean.valueOf(false));
    }
}

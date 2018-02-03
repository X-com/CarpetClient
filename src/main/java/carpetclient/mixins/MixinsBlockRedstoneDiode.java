package carpetclient.mixins;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/*
Mixen class to make comperator properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockRedstoneDiode.class)
public abstract class MixinsBlockRedstoneDiode extends BlockHorizontal {
    
    protected MixinsBlockRedstoneDiode(Material materialIn) {
        super(materialIn);
    }

    @Overwrite
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // rotate comperator based on hotkeys
        facing = placer.getHorizontalFacing().getOpposite();
        if (GuiScreen.isAltKeyDown()) {
            facing = facing.getOpposite();
        }

        return this.getDefaultState().withProperty(FACING, facing);
    }
}

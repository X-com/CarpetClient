package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/*
Mixen class to implement relaxed block placement, placement of torches on jack-o-lanterns.
*/
@Mixin(BlockTorch.class)
public class MixinsBlockTorch extends Block {

    public MixinsBlockTorch(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    @Overwrite
    private boolean canPlaceOn(World worldIn, BlockPos pos)
    {
        Block block = worldIn.getBlockState(pos).getBlock();
        boolean flag = block == Blocks.END_GATEWAY || (block == Blocks.LIT_PUMPKIN && !Config.relaxedBlockPlacement);

        if (worldIn.getBlockState(pos).isTopSolid())
        {
            return !flag;
        }
        else
        {
            boolean flag1 = block instanceof BlockFence || block == Blocks.GLASS || block == Blocks.COBBLESTONE_WALL || block == Blocks.STAINED_GLASS;
            return flag1 && !flag;
        }
    }
}

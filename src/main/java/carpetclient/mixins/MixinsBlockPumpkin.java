package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/*
Mixen class to implement relaxed block placement, placement of pumpkins in mid air.
 */
@Mixin(BlockPumpkin.class)
public class MixinsBlockPumpkin extends BlockHorizontal {

    protected MixinsBlockPumpkin(Material materialIn) {
        super(materialIn);
    }

    @Overwrite
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return (worldIn.getBlockState(pos.down()).getMaterial().isSolid() || Config.relaxedBlockPlacement) ? super.canPlaceBlockAt(worldIn, pos) : false;
    }
}

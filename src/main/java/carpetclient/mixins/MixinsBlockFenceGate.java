package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockFenceGate.class)
public class MixinsBlockFenceGate extends BlockHorizontal {

    protected MixinsBlockFenceGate(Material materialIn) {
        super(materialIn);
    }

    @Overwrite
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return (worldIn.getBlockState(pos.down()).getMaterial().isSolid() || Config.relaxedBlockPlacement) ? super.canPlaceBlockAt(worldIn, pos) : false;
    }
}

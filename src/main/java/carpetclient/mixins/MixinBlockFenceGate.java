package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Mixen class to implement relaxed block placement, placement of fences in mid air.
 */
@Mixin(BlockFenceGate.class)
public class MixinBlockFenceGate extends BlockHorizontal {

    protected MixinBlockFenceGate(Material materialIn) {
        super(materialIn);
    }

    // Override to place fence gates mid air
    @Inject(method = "canPlaceBlockAt", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue((worldIn.getBlockState(pos.down()).getMaterial().isSolid() || Config.relaxedBlockPlacement) ? super.canPlaceBlockAt(worldIn, pos) : false);
    }
}

package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient {
    @Inject(method = "invalidateRegionAndSetBlock", at = @At("HEAD"), cancellable = true)
    private void fixPistonBlinking(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(Config.clipThroughPistons.getValue() && state.getBlock() == Blocks.PISTON_EXTENSION) {
            cir.setReturnValue(true);
        }
    }
}

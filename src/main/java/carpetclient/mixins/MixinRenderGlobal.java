package carpetclient.mixins;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Mixing Override to disable light updates on the client.
Big thanks from nessie to help add this fix.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "notifyLightSet", at = @At("HEAD"), cancellable = true)
    public void notifyLightSet(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
    }
}
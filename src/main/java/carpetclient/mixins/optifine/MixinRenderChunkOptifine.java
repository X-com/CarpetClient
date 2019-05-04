package carpetclient.mixins.optifine;

import carpetclient.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunk.class)
public class MixinRenderChunkOptifine {

    /**
     * Optifine Shenanegans that causes blinky clippy pistons. This helps remedy the issue.
     * @param ci
     */
    @Inject(method = "isPlayerUpdate", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void optifineDisablePlayerUpdatesBoolean(CallbackInfoReturnable<Boolean> ci) {
        if (!Config.clipThroughPistons.getValue()) return;
        ci.setReturnValue(true);
    }
}

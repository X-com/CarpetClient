package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Tick rate editing in EntityRenderer.java based on Cubitecks tick rate mod.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    /**
     * fixes the world being culled while noclipping
     */
    @Redirect(method = "renderWorldPass(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    private boolean fixSpectator(EntityPlayerSP player) {
        return player.isSpectator() || (Config.creativeModeNoClip.getValue() && player.isCreative());
    }
}

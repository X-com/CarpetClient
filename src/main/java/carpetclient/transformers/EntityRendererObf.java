package carpetclient.transformers;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Provide necessary obfuscation mapping at run time
 */

@Mixin(EntityRenderer.class)
@SuppressWarnings("unused")
public abstract class EntityRendererObf {
    private static EntityRenderer __TARGET;

    private void getMouseOverObf(float partialTicks) {
        __TARGET.getMouseOver(partialTicks);
    }

    private void updateRendererObf() {
        __TARGET.updateRenderer();
    }

    private void stopUseShaderObf() {
        __TARGET.stopUseShader();
    }
}

package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.rules.TickRate;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tick rate editing in Minecraft.java based on Cubitecks tick rate mod.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {
    /**
     * Reset logic for clipping through pistons.
     *
     * @param ci
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    public void fixingPistons(CallbackInfo ci) {
        PistonFix.resetBools();
    }

    /**
     * Modify constant in scroll mouse to fix the issue when slowing down.
     */
    @ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
    private long runTickMouseFix(long value) {
        if (TickRate.runTickRate) {
            return (long) Math.max(200F * (20.0f / Config.tickRate), 200L);
        } else {
            return 200L;
        }
    }
}

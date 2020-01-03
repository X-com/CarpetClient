package carpetclient.mixins;

import carpetclient.mixinInterface.AMixinTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tick rate magic
 */
@Mixin(Timer.class)
public abstract class MixinTimer implements AMixinTimer {
    @Shadow
    public int elapsedTicks;

    @Shadow
    public float renderPartialTicks;

    @Shadow
    public float elapsedPartialTicks;

    @Shadow
    private long lastSyncSysClock;

    @Shadow
    private float tickLength;

    public int elapsedTicksWorld;
    public float renderPartialTicksWorld;
    public float elapsedPartialTicksWorld;

    public int elapsedTicksPlayer;
    public float renderPartialTicksPlayer;
    public float elapsedPartialTicksPlayer;

    private static final float tickLengthPlayer = 1000.0F / 20.0F;

    @Override
    public void setWorldTickRate(float tps) {
        this.tickLength = 1000.0F / tps;
        this.renderPartialTicksPlayer = this.renderPartialTicksWorld;
    }

    @Inject(method = "updateTimer", at = @At("HEAD"), cancellable = true)
    public void updateTimer(CallbackInfo ci) {
        long i = Minecraft.getSystemTime();
        long old = this.lastSyncSysClock;
        this.lastSyncSysClock = i;

        this.elapsedPartialTicksWorld = (float)(i - old) / this.tickLength;
        this.renderPartialTicksWorld += this.elapsedPartialTicksWorld;
        this.elapsedTicksWorld = (int)this.renderPartialTicksWorld;
        this.renderPartialTicksWorld -= (float)this.elapsedTicksWorld;

        this.elapsedPartialTicksPlayer = (float)(i - old) / tickLengthPlayer;
        this.renderPartialTicksPlayer += this.elapsedPartialTicksPlayer;
        this.elapsedTicksPlayer = (int)this.renderPartialTicksPlayer;
        this.renderPartialTicksPlayer -= (float)this.elapsedTicksPlayer;

        // mostly used for EntityRenderer.updateCameraAndRender
        this.renderPartialTicks = this.renderPartialTicksWorld;
        // mostly used for GuiScreen,drawScreen
        this.elapsedPartialTicks = this.elapsedPartialTicksPlayer;
        // mostly used for Minecraft,runTick
        this.elapsedTicks = Math.max(this.elapsedTicksWorld, this.elapsedTicksPlayer);

        ci.cancel();
    }
}

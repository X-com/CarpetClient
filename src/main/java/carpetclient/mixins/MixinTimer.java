package carpetclient.mixins;

import carpetclient.mixinInterface.AMixinTimer;
import carpetclient.rules.TickRate;
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

    private final float tickLengthPlayer = 1000.0F / TickRate.NORMAL_RATE;

    @Override
    public int getElapsedTicksPlayer() {
        return this.elapsedTicksPlayer;
    }

    // @Override
    // public float getRenderPartialTicksWorld() {
    //     return this.renderPartialTicksWorld;
    // }
    @Override
    public float getRenderPartialTicksPlayer() {
        return this.renderPartialTicksPlayer;
    }
    @Override
    public void setRenderPartialTicksWorld(float value) {
        this.renderPartialTicksWorld = value;
        this.renderPartialTicks = this.renderPartialTicksWorld;
    }
    @Override
    public void setRenderPartialTicksPlayer(float value) {
        this.renderPartialTicksPlayer = value;
    }

    @Override
    public void setWorldTickRate(float tps) {
        this.tickLength = 1000.0F / tps;
        this.renderPartialTicksPlayer = this.renderPartialTicksWorld;
    }

    @Override
    public float getWorldTickRate() {
        return 1000.0F / this.tickLength;
    }

    @Override
    public float getPlayerTickRate() {
        return 1000.0F / this.tickLengthPlayer;
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

        this.elapsedPartialTicksPlayer = (float)(i - old) / this.tickLengthPlayer;
        this.renderPartialTicksPlayer += this.elapsedPartialTicksPlayer;
        this.elapsedTicksPlayer = (int)this.renderPartialTicksPlayer;
        this.renderPartialTicksPlayer -= (float)this.elapsedTicksPlayer;

        // mostly used for EntityRenderer.updateCameraAndRender, hooked now with Mixin
        this.renderPartialTicks = this.renderPartialTicksWorld;
        // mostly used for GuiScreen.drawScreen
        this.elapsedPartialTicks = this.elapsedPartialTicksPlayer;
        // mostly used for Minecraft.runTick
        this.elapsedTicks = Math.max(this.elapsedTicksWorld, this.elapsedTicksPlayer);

        ci.cancel();
    }
}

package carpetclient.mixins;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface IMixinTimer {

//    @Shadow
//    private float tickLength;
//    @Shadow
//    private long lastSyncSysClock;
//    
//    public void setTimer(float tps) {
//        this.tickLength = 1000.0F / tps;
//        this.lastSyncSysClock = Minecraft.getSystemTime();
//    }

    @Accessor("tickLength")
    void setTickLength(float tps);

    @Accessor("lastSyncSysClock")
    void setLastSyncSysClock(long time);
}

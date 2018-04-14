package carpetclient.mixins;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface IMixinTimer {

    @Accessor("tickLength")
    void setTickLength(float tps);

    @Accessor("lastSyncSysClock")
    void setLastSyncSysClock(long time);
}

package carpetclient.coders.Cubitect;

import carpetclient.Config;
import carpetclient.mixins.IMixinTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public class TickRate {
    public static boolean runTickRate = false;
    public static Timer timerWorld = new Timer(20.0f);

    public static void setTickRate(float setTick) {
        runTickRate = (20.0f != setTick);
        Config.tickRate = setTick;
        System.out.println("set tick rate " + setTick + " " + runTickRate);
        ((IMixinTimer) timerWorld).setTickLength(1000.0F / setTick);
        ((IMixinTimer) timerWorld).setLastSyncSysClock(Minecraft.getSystemTime());
    }
}

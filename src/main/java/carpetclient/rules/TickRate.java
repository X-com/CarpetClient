package carpetclient.rules;

import carpetclient.Config;
import carpetclient.mixins.IMixinMinecraft;
import carpetclient.mixins.IMixinTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Timer;

/**
 * Tick rate method to edit the clients tick rate changes based on the servers tick rate.
 */
public class TickRate {
    public static boolean runTickRate = false;
    public static Timer timerWorld = new Timer(20.0f);

    /**
     * Setter for the tick rate on the client based on a set tick speed.
     *
     * @param setTick Tick rate that is to be set on the client.
     */
    public static void setTickRate(float setTick) {
        runTickRate = (20.0f != setTick);
        Config.tickRate = setTick;
        ((IMixinTimer) timerWorld).setTickLength(1000.0F / setTick);
        ((IMixinTimer) timerWorld).setLastSyncSysClock(Minecraft.getSystemTime());
        ((IMixinTimer) ((IMixinMinecraft) Minecraft.getMinecraft()).getTimer()).setLastSyncSysClock(Minecraft.getSystemTime());
    }

    /**
     * A data packet handler for unpacking and setting the client tick rate.
     *
     * @param data Data from the server sent when tick rates are changed.
     */
    public static void setTickRate(PacketBuffer data) {
        Config.tickRate = data.readFloat();
        setTickRate(Config.tickRate);
    }
}

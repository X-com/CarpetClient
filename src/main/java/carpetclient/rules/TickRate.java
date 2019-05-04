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
     * Setter tick rate in the config files for later setting the client to the tick rate.
     *
     * @param setTick Tick rate that is to be set on the client.
     */
    public static void setTickRate(float setTick) {
        Config.tickRate = setTick;
        setTickClient();
    }

    /**
     * Sets the game tick after the values are set.
     */
    public static void setTickClient() {
        runTickRate = Config.setTickRate.getValue() && (20.0f != Config.tickRate);
        if(runTickRate) {
            ((IMixinTimer) timerWorld).setTickLength(1000.0F / Config.tickRate);
            ((IMixinTimer) timerWorld).setLastSyncSysClock(((IMixinTimer) ((IMixinMinecraft) Minecraft.getMinecraft()).getTimer()).getLastSyncSysClock());
//        ((IMixinTimer) ((IMixinMinecraft) Minecraft.getMinecraft()).getTimer()).setLastSyncSysClock(Minecraft.getSystemTime());
        }
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

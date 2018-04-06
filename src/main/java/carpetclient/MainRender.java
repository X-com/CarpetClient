package carpetclient;

import net.minecraft.client.Minecraft;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import carpetclient.coders.EDDxample.VillageMarker;

/*
 * Main render class to implement client render updates.
 */
public class MainRender {
    private static Lock mutex = new ReentrantLock();

    /**
     * Render update method to render client side rendering.
     *
     * @param partialTicks -- not sure what this does.
     */
    public static void mainRender(float partialTicks) {
        if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
            return;
        }

        mutex.lock();

        try {
            VillageMarker.RenderVillages(partialTicks);
        } finally {
            mutex.unlock();
        }
    }
}

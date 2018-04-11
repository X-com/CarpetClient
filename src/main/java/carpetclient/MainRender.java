package carpetclient;

import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        mutex.lock();

        try {
            if (Config.boundingBoxMarkers) {
                ShowBoundingBoxes.RenderStructures(partialTicks);
            }
            if (Config.villageMarkers) {
                VillageMarker.RenderVillages(partialTicks);
            }
        } finally {
            mutex.unlock();
        }
    }
}

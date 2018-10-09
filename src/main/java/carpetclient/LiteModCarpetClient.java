package carpetclient;

import java.io.File;
import java.util.List;

import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.gui.GuiChunkGrid;
import com.mumfrey.liteloader.*;
import carpetclient.pluginchannel.CarpetPluginChannel;
import carpetclient.rules.CarpetRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

public class LiteModCarpetClient implements Tickable, LiteMod, PluginChannelListener, PostRenderListener, HUDRenderListener {

    private boolean gameRunnin = false;
    private boolean loggedOut = false;

    @Override
    public String getVersion() {
        return "@VERSION@";
    }

    @Override
    public void init(File configPath) {
        GuiChunkGrid.instance = new GuiChunkGrid();
        Hotkeys.init();
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public String getName() {
        return "Carpet Client";
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        gameRunnin = minecraft.isIntegratedServerRunning() || minecraft.getCurrentServerData() != null;

        if (gameRunnin) {
            Hotkeys.onTick(minecraft, partialTicks, inGame, clock);
            loggedOut = true;
        } else if (loggedOut) {
            loggedOut = false;
            CarpetRules.resetToDefaults();
            VillageMarker.clearLists(0);
            ShowBoundingBoxes.clear();
            GuiChunkGrid.instance = new GuiChunkGrid();
        }
    }

    // Needed method for plugin channels. Data from the server.
    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        CarpetPluginChannel.packatReceiver(channel, data);
    }

    // Needed method for plugin channels. Adds the list of channels that the client will listen for.
    @Override
    public List<String> getChannels() {
        return CarpetPluginChannel.CARPET_PLUGIN_CHANNEL;
    }

    @Override
    public void onPostRenderEntities(float partialTicks) {
        if (gameRunnin) {
            MainRender.mainRender(partialTicks);
        }
    }

    @Override
    public void onPostRender(float partialTicks) {

    }

    @Override
    public void onPreRenderHUD(int screenWidth, int screenHeight) {

    }

    @Override
    public void onPostRenderHUD(int screenWidth, int screenHeight) {
        if (GuiChunkGrid.instance.isMinimapVisible()) {
            GuiChunkGrid.instance.renderMinimap(screenWidth, screenHeight);
        }
    }
}
package carpetclient;

import java.io.File;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.PluginChannelListener;
import carpetclient.pluginchannel.CarpetPluginChannel;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

public class LiteModCarpetClient implements Tickable, LiteMod, PluginChannelListener {

    @Override
    public String getVersion() {
        return "Pre-Alpha 0.4";
    }

    @Override
    public void init(File configPath) {
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
        Hotkeys.onTick(minecraft, partialTicks, inGame, clock);
    }

    // Needed method for plugin channels. Data from the server.
    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        CarpetPluginChannel.packageReceived(channel, data);
    }

    // Needed method for plugin channels. Adds the list of channels that the client will listen for.
    @Override
    public List<String> getChannels() {
        return CarpetPluginChannel.CARPET_PLUGIN_CHANNEL;
    }
}
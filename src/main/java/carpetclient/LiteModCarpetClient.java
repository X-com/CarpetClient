package carpetclient;

import java.io.File;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.Tickable;

import net.minecraft.client.Minecraft;

public class LiteModCarpetClient implements Tickable, LiteMod {

    @Override
    public String getVersion() {
        return "Pre-Alpha 0.3.1";
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
}
package carpetclient;

import java.io.File;
import com.mumfrey.liteloader.Tickable;
import net.minecraft.client.Minecraft;

public class LiteModCarpetClient implements Tickable{
    @Override
    public String getVersion() {
        return "Pre-Alpha 0.0";
    }

    @Override
    public void init(File configPath) {
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
    }

}
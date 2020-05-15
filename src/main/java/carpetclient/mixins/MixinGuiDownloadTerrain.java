package carpetclient.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketKeepAlive;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiDownloadTerrain.class)
public class MixinGuiDownloadTerrain {

    private int counter = 0;

    public void updateScreen()
    {
        ++counter;
        if (counter % 20 == 0)
        {
            Minecraft.getMinecraft().getConnection().sendPacket(new CPacketKeepAlive());
        }
    }
}

package carpetclient.gui.config;

import carpetclient.Config;
import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.config.ConfigBase;
import carpetclient.gui.ConfigGUI;
import carpetclient.gui.entry.ConfigBooleanEntry;
import net.minecraft.client.Minecraft;

public class BoundingBoxList extends GuiConfigList {
    public BoundingBoxList(Minecraft mcIn, int slotHeightIn) {
        super(mcIn, slotHeightIn);
    }

    @Override
    public void onClose()
    {
        Config.save();
    }

    @Override
    public void initGui() {
        for (ConfigBase opt : Config.BOUNDINGBOXES) {
            if (opt.getType() == ConfigBase.ConfigType.BOOLEAN)
                addEntry(new ConfigBooleanEntry((ConfigBase<Boolean>)opt, true) {
                    @Override
                    protected boolean isResetEnabled()
                    {
                        return opt.getValue() != opt.getDefaultValue();
                    }
                });
        }
    }
}

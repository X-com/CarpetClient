package carpetclient.gui.config;

import carpetclient.Config;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.config.ConfigBase;
import carpetclient.gui.ConfigGUI;
import carpetclient.gui.entry.ConfigBooleanEntry;
import carpetclient.gui.entry.ConfigIntegerEntry;
import net.minecraft.client.Minecraft;

public class VillageMarkersList extends GuiConfigList {
    public VillageMarkersList(Minecraft mcIn, int slotHeightIn) {
        super(mcIn, slotHeightIn);
    }

    @Override
    public void initGui() {
        for (ConfigBase opt : Config.VILLAGEMARKERS) {
            if (opt.getType() == ConfigBase.ConfigType.BOOLEAN)
                addEntry(new ConfigBooleanEntry((ConfigBase<Boolean>)opt, true));
        }

        addEntry(new ConfigIntegerEntry(Config.villageMarkerVillageRadius, true) {
            @Override
            protected String getDisplayString() {
                return VillageMarker.modes[Config.villageMarkerVillageRadius.getValue()];
            }
        }).onAction((source) -> Config.villageMarkerVillageRadius.setValue((Config.villageMarkerVillageRadius.getValue() + 1) % VillageMarker.modes.length ));
        addEntry(new ConfigIntegerEntry(Config.villageMarkerDoorRadius, true) {
            @Override
            protected String getDisplayString() {
                return VillageMarker.modes[Config.villageMarkerDoorRadius.getValue()];
            }
        }).onAction((source) -> Config.villageMarkerDoorRadius.setValue((Config.villageMarkerDoorRadius.getValue() + 1) % VillageMarker.modes.length ));
    }
}

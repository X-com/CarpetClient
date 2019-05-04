package carpetclient.gui.config;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.ConfigGUI;
import carpetclient.gui.entry.SimpleButtonEntry;
import net.minecraft.client.Minecraft;

public class ClientRootList extends GuiConfigList {
    private final ConfigGUI configGui;

    public ClientRootList(ConfigGUI configGui, Minecraft mcIn, int slotHeightIn) {
        super(mcIn, slotHeightIn);

        this.configGui = configGui;
    }

    public void initGui() {
        addEntry(new SimpleButtonEntry("Carpet Client Options").onAction((s) -> configGui.showList(new CarpetClientOptionsList(mc, getSlotHeight()))));
        addEntry(new SimpleButtonEntry("Carpet Server Options").onAction((s) -> configGui.showList(new CarpetServerList(mc, getSlotHeight()))));
        addEntry(new SimpleButtonEntry("Village Markers Options").onAction((s) -> configGui.showList(new VillageMarkersList(mc, getSlotHeight()))));
        addEntry(new SimpleButtonEntry("Bounding Box Options").onAction((s) -> configGui.showList(new BoundingBoxList(mc, getSlotHeight()))));
        addEntry(new SimpleButtonEntry("Chunk Debug Map Options").onAction((s) -> configGui.showList(new ChunkDebugMapList(mc, getSlotHeight()))));
    }
}

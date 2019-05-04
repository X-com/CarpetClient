package carpetclient.gui.config;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.chunkgrid.ChunkGridStyle;
import carpetclient.gui.chunkgrid.GuiChunkGrid;
import carpetclient.gui.entry.ButtonEntry;
import net.minecraft.client.Minecraft;

public class ChunkDebugMapList extends GuiConfigList {

    public ChunkDebugMapList(Minecraft mcIn, int slotHeightIn) {
        super(mcIn, slotHeightIn);
    }

    @Override
    public void initGui() {
        addEntry(new ButtonEntry("Chunk Grid Style", "", false, "") {
            @Override
            protected String getDisplayString() {
                return GuiChunkGrid.style.getName();
            }

            @Override
            protected String getTooltip() {
                return "Theme of the chunk grid" + (GuiChunkGrid.style.getDesc() == null ? "" : " (" + GuiChunkGrid.style.getDesc() + ")");
            }
        }).onAction((sender) -> ChunkGridStyle.changeStyle());
    }
}

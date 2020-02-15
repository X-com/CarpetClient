package carpetclient.gui.entry;

import net.minecraft.client.gui.GuiListExtended;

public interface ITooltipEntry extends GuiListExtended.IGuiListEntry {
    void drawTooltip(int slotIndex, int x, int y, int mouseX, int mouseY, int listWidth, int listHeight, int slotWidth, int slotHeight,  float partialTicks);
}

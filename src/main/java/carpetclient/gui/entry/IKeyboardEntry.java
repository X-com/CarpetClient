package carpetclient.gui.entry;

import net.minecraft.client.gui.GuiListExtended;

public interface IKeyboardEntry extends GuiListExtended.IGuiListEntry {
    void keyDown(char typedChar, int keyCode);

    void onUpdate();
}
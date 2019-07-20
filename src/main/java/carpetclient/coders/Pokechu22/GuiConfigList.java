package carpetclient.coders.Pokechu22;


import carpetclient.gui.entry.BaseEntry;
import carpetclient.gui.entry.IKeyboardEntry;
import carpetclient.gui.entry.ITooltipEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiConfigList extends GuiListExtended {
    private final List<IGuiListEntry> entries = new ArrayList<>();

    public GuiConfigList(Minecraft mcIn, int slotHeightIn) {
        super(mcIn, 0, 0, 0, 0, slotHeightIn);
    }

    public int getSelectedElement() { return selectedElement; }

    public abstract void initGui();

    public void onClose()
    {
    }

    public <T> BaseEntry<T> addEntry(BaseEntry<T> entry) {
        this.entries.add(entry);
        return entry;
    }

    @Override
    public int getListWidth() {
        return 180 * 2;
    }

    @Override
    protected int getScrollBarX() {
        return this.width / 2 + getListWidth() / 2 + 4;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return entries.get(index);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    public void update() {
        // Use a manual for loop to avoid concurrent modification exceptions
        for (int i = 0; i < getSize(); i++) {
            IGuiListEntry entry = getListEntry(i);
            if (entry instanceof IKeyboardEntry) {
                ((IKeyboardEntry) entry).onUpdate();
            }
        }
    }

    public void keyDown(char typedChar, int keyCode) {
        // Use a manual for loop to avoid concurrent modification exceptions
        for (int i = 0; i < getSize(); i++) {
            IGuiListEntry entry = getListEntry(i);
            if (entry instanceof IKeyboardEntry) {
                ((IKeyboardEntry) entry).keyDown(typedChar, keyCode);
            }
        }
    }

    public void drawTooltip(int mouseX, int mouseY, float partialTicks) {
        int insideLeft = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
        int insideTop = this.top + 4 - (int)this.amountScrolled;
        int l = this.slotHeight - 4;

        for (int i = 0; i < this.getSize(); i++) {
            int k = insideTop + i * this.slotHeight + this.headerPadding;

            IGuiListEntry entry = getListEntry(i);
            if (entry instanceof ITooltipEntry) {
                ((ITooltipEntry) entry).drawTooltip(i, insideLeft, k, mouseX, mouseY, this.getListWidth(), this.height, this.width, l, partialTicks);
            }
        }
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        if (getSelectedElement() != -1 && getSelectedElement() != slotIndex)
        {
            ((BaseEntry) this.getListEntry(getSelectedElement())).testFocused(mouseX, mouseY);
        }

        if (slotIndex != -1)
        {
            ((BaseEntry) this.getListEntry(slotIndex)).testFocused(mouseX, mouseY);
        }
    }
}

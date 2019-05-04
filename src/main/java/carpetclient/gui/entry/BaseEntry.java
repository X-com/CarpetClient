package carpetclient.gui.entry;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseEntry<T> implements GuiListExtended.IGuiListEntry {
    @Nullable
    protected String title;

    private boolean hasFocus;

    private List<Consumer<T>> actions;

    public BaseEntry(@Nullable String title) {
        this.title = title;
    }

    public String getTitle() { return title; }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        if (this.title != null) {
            Minecraft.getMinecraft().fontRenderer.drawString(this.title, x, y + 6, 0xFFFFFFFF);
        }
        this.draw(x, y, listWidth, slotHeight, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        return mouseDown(mouseX, mouseY, mouseEvent);
    }

    @Override
    public void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        mouseUp(mouseX, mouseY, mouseEvent);
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
    }

    public boolean isFocused() {
        return this.hasFocus;
    }

    public void setFocused(boolean focus) {
        this.hasFocus = focus;
        this.onFocusChanged();
    }

    protected void onFocusChanged() {}

    public T onAction(Consumer<T> action) {
        if (actions == null)
            actions = new ArrayList<>();

        actions.add(action);
        return (T)this;
    }

    protected abstract void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks);

    protected boolean mouseDown(int x, int y, int button) { return false; }

    protected void mouseUp(int x, int y, int button) {}

    @SuppressWarnings("unchecked")
    protected void performAction() {
        if (actions != null)
            actions.forEach((r) -> r.accept((T)this));
    }
}

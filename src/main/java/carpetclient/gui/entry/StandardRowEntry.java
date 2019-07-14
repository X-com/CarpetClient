package carpetclient.gui.entry;

import carpetclient.util.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class StandardRowEntry<T> extends BaseEntry<T> implements ITooltipEntry {
    protected String infoStr;

    protected GuiButton resetButton;
    protected GuiButton infoButton;

    protected boolean info;
    protected boolean reset;

    private List<Consumer<T>> infos;
    private List<Consumer<T>> resets;

    public StandardRowEntry(@Nonnull String title, boolean info, boolean reset, String infoStr) {
        super(title);

        this.info = info;
        this.reset = reset;

        this.infoStr = infoStr;

        if (this.reset) {
            this.resetButton = new GuiButton(0, 0, 0, 50, 20, "reset");
        }

        if (this.info) {
            this.infoButton = new GuiButton(0, 0, 0, 14, 15, "i");
        }
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);

        if (this.reset) {
            this.resetButton.x = x + listWidth / 2 + 110;
            this.resetButton.y = y;
            this.resetButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }

        if (this.info) {
            this.infoButton.x = x + listWidth / 2 - 17;
            this.infoButton.y = y + 2;
            this.infoButton.enabled = getTooltip().length() == 0;
            this.infoButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }
    }

    public void drawTooltip(int slotIndex, int x, int y, int mouseX, int mouseY, int listWidth, int listHeight, int slotWidth, int slotHeight,  float partialTicks) {
        if (this.info && getTooltip().length() > 0 && infoButton.isMouseOver()) {
            RenderHelper.drawGuiInfoBox(Minecraft.getMinecraft().fontRenderer, getTooltip(), mouseY + 5, listWidth, slotWidth, listHeight, 48);
        }
    }

    protected String getTooltip() {
        return infoStr;
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        Minecraft mc = Minecraft.getMinecraft();

        if (this.reset && this.resetButton.mousePressed(mc, mouseX, mouseY)) {
            this.resetButton.playPressSound(mc.getSoundHandler());
            this.performResetAction();
            return true;
        }
        if (this.info && this.infoButton.mousePressed(mc, mouseX, mouseY)) {
            this.infoButton.playPressSound(mc.getSoundHandler());
            this.performInfoAction();
            return true;
        }

        return mouseDown(mouseX, mouseY, mouseEvent);
    }

    @Override
    public void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (this.reset)
            this.resetButton.mouseReleased(mouseX, mouseY);

        mouseUp(mouseX, mouseY, mouseEvent);
    }

    public T onInfo(Consumer<T> action) {
        if (infos == null)
            infos = new ArrayList<>();

        infos.add(action);
        return (T)this;
    }

    public T onReset(Consumer<T> action) {
        if (resets == null)
            resets = new ArrayList<>();

        resets.add(action);
        return (T)this;
    }

    private void performInfoAction() {
        if (infos != null)
            infos.forEach((r) -> r.accept((T)this));
    }

    private void performResetAction() {
        if (resets != null)
            resets.forEach((r) -> r.accept((T)this));
    }
}

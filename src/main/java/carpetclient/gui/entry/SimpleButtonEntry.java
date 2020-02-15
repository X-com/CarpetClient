package carpetclient.gui.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class SimpleButtonEntry extends BaseEntry<SimpleButtonEntry> {
    private GuiButton button;

    public SimpleButtonEntry(String btnText) {
        super(null);
        this.button = new GuiButton(0, 0, 0, 150, 20, btnText);
    }

    public GuiButton getButton() { return button; }

    @Override
    protected void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
        this.button.x = x + listWidth / 2 - button.getButtonWidth() / 2;
        this.button.y = y;
        this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean mouseDown(int x, int y, int button) {
        if (this.button.mousePressed(Minecraft.getMinecraft(), x, y)) {
            this.button.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            this.performAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void mouseUp(int x, int y, int button) {
        this.button.mouseReleased(x, y);
    }
}

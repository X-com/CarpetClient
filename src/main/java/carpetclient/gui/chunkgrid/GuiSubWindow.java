package carpetclient.gui.chunkgrid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

/**
 * Window for displaying events selected by the user.
 */
public abstract class GuiSubWindow extends GuiScreen {

    protected String title;
    protected GuiScreen parentScreen;
    protected GuiScreen backgroundScreen;
    private List<String> stackTrace;

    public GuiSubWindow(String title, GuiScreen parentScreen, GuiScreen backgroundScreen, List<String> stackTrace) {
        this.title = title;
        this.parentScreen = parentScreen;
        this.backgroundScreen = backgroundScreen;
        this.stackTrace = stackTrace;
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        backgroundScreen.setWorldAndResolution(mc, width, height);
    }

    protected void layoutButtons(GuiButton... buttons) {
        int buttonX = getSubWindowLeft() + 5;
        int buttonY = getSubWindowBottom() - (20 + getFooterHeight()) / 2;
        int buttonWidth = (getSubWindowRight() - getSubWindowLeft() - 5 * (buttons.length + 1)) / buttons.length;

        for (GuiButton b : buttons) {
            b.x = buttonX;
            b.y = buttonY;
            b.setWidth(buttonWidth);
            buttonX += 5 + buttonWidth;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        backgroundScreen.drawScreen(-1, -1, partialTicks); // Spoof mouse coords so buttons don't highlight
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        Gui.drawRect(getSubWindowLeft(), getSubWindowTop(), getSubWindowRight(), getSubWindowBottom(), 0xa0d0d0ff);

        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(2);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(getSubWindowRight(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowLeft(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowLeft(), getSubWindowTop(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowRight(), getSubWindowTop(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowRight(), getSubWindowBottom(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowLeft(), getSubWindowBottom(), 0).color(255, 255, 255, 255).endVertex();
        buf.pos(getSubWindowLeft(), getSubWindowBottom() - getFooterHeight(), 0).color(255, 255, 255, 255).endVertex();
        tess.draw();
        GlStateManager.glLineWidth(1);
        GlStateManager.enableTexture2D();

        fontRenderer.drawString(title, (getSubWindowLeft() + getSubWindowRight()) / 2 - fontRenderer.getStringWidth(title) / 2, getSubWindowTop() + 6, 0);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (selectedButton == null) {
            if (mouseButton == 0) {
                mc.displayGuiScreen(parentScreen);
                if (parentScreen instanceof GuiChunkGrid) {
                    ((GuiChunkGrid) parentScreen).consumeLeftClick();
                }
            } else if (mouseButton == 1) {
                if (stackTrace != null && !(this instanceof GuiShowStackTrace)) {
                    mc.displayGuiScreen(new GuiShowStackTrace(this, backgroundScreen, stackTrace));
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(parentScreen);
        else
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return backgroundScreen.doesGuiPauseGame();
    }

    protected int getSubWindowLeft() {
        return (int) (width * 0.1);
    }

    protected int getSubWindowRight() {
        return (int) (width * 0.9);
    }

    protected int getSubWindowTop() {
        return (int) (height * 0.1);
    }

    protected int getSubWindowBottom() {
        return (int) (height * 0.9);
    }

    protected int getFooterHeight() {
        return 30;
    }
}

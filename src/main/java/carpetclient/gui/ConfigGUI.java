package carpetclient.gui;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.config.ClientRootList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class ConfigGUI extends GuiScreen {
    private static int slotHeight = 24;
    private static String carpetServerVersion;
    public static void setServerVersion(String version) { carpetServerVersion = version;}

    private final GuiScreen parent;
    private GuiConfigList list = null;

    public ConfigGUI(GuiScreen parent) {
        this.parent = parent;
    }

    public void showList(GuiConfigList list) {
        this.list = list;

        this.list.setDimensions(this.width, this.height, 39, this.height - 32);
        this.list.initGui();
    }

    public void initGui() {
        this.addButton(new GuiButton(100, this.width / 2 - 100, this.height - 29, "Back"));

        showList(new ClientRootList(this, mc, 24));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 100) {
            list.onClose();
            if (list instanceof ClientRootList) {
                this.mc.displayGuiScreen(this.parent);
            } else {
                this.showList(new ClientRootList(this, mc, slotHeight));
            }
        }
    }

    // ===== RENDERING ===== //
    //region rendering
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        this.drawTooltip(mouseX, mouseY, partialTicks);

        final int startY = 8;
        this.drawCenteredString(this.fontRenderer, "Carpet Client", width / 2, startY, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, String.format("Carpet server version: %s", carpetServerVersion), width / 2, startY + this.fontRenderer.FONT_HEIGHT, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawTooltip(int mouseX, int mouseY, float partialTicks) {
        list.drawTooltip(mouseX, mouseY, partialTicks);
    }
    //endregion

    // ===== EVENTS ===== //
    //region events
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        list.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        list.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        list.keyDown(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        list.handleMouseInput();
    }
    //endregion
}

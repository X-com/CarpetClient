package carpetclient.gui.chunkgrid;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.List;

/**
 * Display window for the events.
 */
public class GuiChunkGridChunk extends GuiSubWindow {

    private GuiButton doneButton;
    private GuiButton showStackTraceButton;

    private List<String> properties;
    private List<String> stackTrace;

    public GuiChunkGridChunk(String header, GuiScreen parentScreen, GuiScreen backgroundScreen, List<String> properties, List<String> stackTrace) {
        super(header, parentScreen, backgroundScreen, stackTrace);
        this.properties = properties;
        this.stackTrace = stackTrace;
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(doneButton = new GuiButton(0, 0, 0, I18n.format("gui.done")));
        addButton(showStackTraceButton = new GuiButton(1, 0, 0, "Show Stack Trace"));
        showStackTraceButton.enabled = stackTrace != null;
        layoutButtons(doneButton, showStackTraceButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(parentScreen);
                break;
            case 1:
                mc.displayGuiScreen(new GuiShowStackTrace(this, backgroundScreen, stackTrace));
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int x = (getSubWindowLeft() + getSubWindowRight()) / 2;
        int y = getSubWindowTop() + 25;

        for (String prop : properties) {
            fontRenderer.drawString(prop, x - fontRenderer.getStringWidth(prop) / 2, y, 0);
            y += fontRenderer.FONT_HEIGHT + 2;
        }
    }
}

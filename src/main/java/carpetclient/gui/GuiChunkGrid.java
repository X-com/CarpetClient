package carpetclient.gui;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import sun.security.provider.certpath.Vertex;

import java.io.IOException;
import java.nio.Buffer;

public class GuiChunkGrid extends GuiScreen {

    public static GuiChunkGrid instance;

    private Controller controller;
    private ChunkGrid chunkgrid = new ChunkGrid();

    private GuiButton startStopButton;
    private GuiCheckbox stackTracesCheckbox;
    private GuiButton loadButton;
    private GuiButton saveButton;
    private GuiButton backButton;
    private GuiButton forwardButton;
    private GuiButton currentButton;

    private GuiCheckbox minimapVisibleCheckbox;

    private int time;
    private int xText;
    private int zText;

    private static final String[] DIMENSION_NAMES = {"Overworld", "Nether", "End"};
    private int selectedDimension = 0;
    private GuiButton dimensionButton;

    public GuiChunkGrid() {
        this.controller = new Controller(this);
    }

    // GUI functions

    @Override
    public void initGui() {
        super.initGui();

        addButton(startStopButton = new GuiButton(0, getFooterX(0), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, controller.start ? "Stop" : "Start"));

        boolean stackTracesOn = stackTracesCheckbox != null && stackTracesCheckbox.checked;
        stackTracesCheckbox = new GuiCheckbox(1, getFooterX(1), getFooterY(0), "Stack Traces");
        stackTracesCheckbox.x += (getFooterColWidth() - stackTracesCheckbox.getButtonWidth()) / 2;
        stackTracesCheckbox.y += (FOOTER_ROW_HEIGHT - 8) / 2; // Hardcoded constant, no getter :(
        stackTracesCheckbox.checked = stackTracesOn;
        addButton(stackTracesCheckbox);

        addButton(loadButton = new GuiButton(2, getFooterX(2), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Load"));
        addButton(saveButton = new GuiButton(3, getFooterX(3), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Save"));
        loadButton.enabled = saveButton.enabled = !controller.start;

        addButton(backButton = new GuiButton(4, getFooterX(0), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Back"));
        addButton(forwardButton = new GuiButton(5, getFooterX(1), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Forward"));

        addButton(currentButton = new GuiButton(6, getFooterX(3), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Current"));

        addButton(new GuiButton(7, getFooterX(0), getFooterY(2), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Home"));
        addButton(dimensionButton = new GuiButton(8, getFooterX(1), getFooterY(2), getFooterColWidth(), FOOTER_ROW_HEIGHT, DIMENSION_NAMES[selectedDimension]));

        boolean minimapVisible = isMinimapVisible();
        minimapVisibleCheckbox = new GuiCheckbox(9, getFooterX(3), getFooterY(2), "Show Minimap");
        minimapVisibleCheckbox.x += (getFooterColWidth() - minimapVisibleCheckbox.getButtonWidth()) / 2;
        minimapVisibleCheckbox.y += (FOOTER_ROW_HEIGHT - 8) / 2; // Hardcoded constant, no getter :(
        minimapVisibleCheckbox.checked = minimapVisible;
        addButton(minimapVisibleCheckbox);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (controller.startStop()) {
                    startStopButton.displayString = "Stop";
                    loadButton.enabled = saveButton.enabled = false;
                    currentButton.enabled = true;
                } else {
                    startStopButton.displayString = "Start";
                    loadButton.enabled = saveButton.enabled = true;
                    currentButton.enabled = false;
                }
                break;
            case 1:
                stackTracesCheckbox.checked = !stackTracesCheckbox.checked;
                break;
            case 2:
                controller.load();
                break;
            case 3:
                controller.save();
                break;
            case 4:
                controller.back();
                break;
            case 5:
                controller.forward();
                break;
            case 6:
                controller.current();
                break;
            case 7:
                controller.home();
                break;
            case 8:
                selectedDimension = (selectedDimension + 1) % DIMENSION_NAMES.length;
                dimensionButton.displayString = DIMENSION_NAMES[selectedDimension];
                controller.comboBoxAction();
                break;
            case 9:
                setMinimapVisible(!isMinimapVisible());
                break;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) controller.scroll(scroll / 50);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT)
            controller.buttonDown(mouseX, mouseY - HEADER_HEIGHT, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT)
            controller.buttonUp(mouseX, mouseY - HEADER_HEIGHT, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT)
            controller.mouseDrag(mouseX, mouseY - HEADER_HEIGHT, clickedMouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableTexture2D();

        // Chunk grid
        chunkgrid.draw(0, HEADER_HEIGHT, width, height - HEADER_HEIGHT - FOOTER_HEIGHT);

        mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Draw 4 pixel high fading transitions
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0, HEADER_HEIGHT, 0).color(0, 0, 0, 255).endVertex();
        buf.pos(0, HEADER_HEIGHT + 4, 0).color(0, 0, 0, 0).endVertex();
        buf.pos(width, HEADER_HEIGHT + 4, 0).color(0, 0, 0, 0).endVertex();
        buf.pos(width, HEADER_HEIGHT, 0).color(0, 0, 0, 255).endVertex();
        tess.draw();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(0, height - FOOTER_HEIGHT - 4, 0).color(0, 0, 0, 0).endVertex();
        buf.pos(0, height - FOOTER_HEIGHT, 0).color(0, 0, 0, 255).endVertex();
        buf.pos(width, height - FOOTER_HEIGHT, 0).color(0, 0, 0, 255).endVertex();
        buf.pos(width, height - FOOTER_HEIGHT - 4, 0).color(0, 0, 0, 0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        // Draw header and footer background
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buf.pos(0, 0, 0).tex(0, 0).color(64, 64, 64, 255).endVertex();
        buf.pos(0, HEADER_HEIGHT, 0).tex(0, HEADER_HEIGHT / 32f).color(64, 64, 64, 255).endVertex();
        buf.pos(width, HEADER_HEIGHT, 0).tex(width / 32f, HEADER_HEIGHT / 32f).color(64, 64, 64, 255).endVertex();
        buf.pos(width, 0, 0).tex(width / 32f, 0).color(64, 64, 64, 255).endVertex();
        tess.draw();

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buf.pos(0, height - FOOTER_HEIGHT, 0).tex(0, (height - FOOTER_HEIGHT) / 32f).color(64, 64, 64, 255).endVertex();
        buf.pos(0, height, 0).tex(0, height / 32f).color(64, 64, 64, 255).endVertex();
        buf.pos(width, height, 0).tex(width / 32f, height / 32f).color(64, 64, 64, 255).endVertex();
        buf.pos(width, height - FOOTER_HEIGHT, 0).tex(width / 32f, (height - FOOTER_HEIGHT) / 32f).color(64, 64, 64, 255).endVertex();
        tess.draw();

        // Text
        drawCenteredString(fontRenderer, "Chunk Debug Map", width / 2, 0, 0xffffff);

        drawCenteredString(fontRenderer, "GT: " + time, getFooterX(2) + getFooterColWidth() / 2, getFooterY(1) + FOOTER_ROW_HEIGHT / 2, 0xffffff);

        String posText = "X: " + xText + ", Z: " + zText;
        drawCenteredString(fontRenderer, posText, getFooterX(2) + getFooterColWidth() / 2, getFooterY(2) + FOOTER_ROW_HEIGHT / 2, 0xffffff);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // Minimap


    public void renderMinimap(int screenWidth, int screenHeight) {
        int minimapX = (int) (screenWidth * MINIMAP_X);
        int minimapY = (int) (screenHeight * MINIMAP_Y);
        int minimapWidth = (int) (screenWidth * MINIMAP_WIDTH);
        int minimapHeight = (int) (screenHeight * MINIMAP_HEIGHT);

        // Minimap frame
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        final int COL_DARK = 0x30;
        final int COL_LIGHT = 0xc0;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minimapX - 5, minimapY - 5, 0).color(COL_DARK, COL_DARK, COL_DARK, 0xff).endVertex();
        buf.pos(minimapX - 5, minimapY + minimapHeight + 5, 0).color(COL_LIGHT, COL_LIGHT, COL_LIGHT, 0xff).endVertex();
        buf.pos(minimapX + minimapWidth + 5, minimapY + minimapHeight + 5, 0).color(COL_DARK, COL_DARK, COL_DARK, 0xff).endVertex();
        buf.pos(minimapX + minimapWidth + 5, minimapY - 5, 0).color(COL_LIGHT, COL_LIGHT, COL_LIGHT, 0xff).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();

        // Actual minimap content
        chunkgrid.draw(minimapX, minimapY, minimapWidth, minimapHeight);
    }

    // Footer is a grid, makes stuff easier
    private static final int HEADER_HEIGHT = 9;
    private static final int FOOTER_ROW_HEIGHT = 20;
    private static final int FOOTER_ROW_PADDING = 5;
    private static final int FOOTER_ROW_COUNT = 3;
    private static final int FOOTER_HEIGHT = FOOTER_ROW_HEIGHT * FOOTER_ROW_COUNT + FOOTER_ROW_PADDING * (FOOTER_ROW_COUNT + 1);
    private static final int FOOTER_COL_PADDING = 5;
    private static final int FOOTER_COL_COUNT = 4;
    private static final float MINIMAP_X = 0.7f;
    private static final float MINIMAP_Y = 0.05f;
    private static final float MINIMAP_WIDTH = 0.25f;
    private static final float MINIMAP_HEIGHT = 0.45f;

    private int getFooterColWidth() {
        return (width - FOOTER_ROW_PADDING * (FOOTER_COL_COUNT + 1)) / FOOTER_COL_COUNT;
    }

    private int getFooterX(int x) {
        return x * getFooterColWidth() + (x + 1) * FOOTER_COL_PADDING;
    }

    private int getFooterY(int y) {
        return height - FOOTER_HEIGHT + y * FOOTER_ROW_HEIGHT + (y + 1) * FOOTER_ROW_PADDING;
    }

    // Accessors

    public boolean areStackTracesEnabled() {
        return stackTracesCheckbox.checked;
    }

    public ChunkGrid getChunkGrid() {
        return chunkgrid;
    }

    public int getSelectedDimension() {
        return selectedDimension;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setXText(int xText) {
        this.xText = xText;
    }

    public void setZText(int zText) {
        this.zText = zText;
    }

    public void setBackButtonText(String text) {
        this.backButton.displayString = text;
    }

    public void setForwardButtonText(String text) {
        this.forwardButton.displayString = text;
    }

    public void liveUpdate(int time) {
        controller.liveUpdate(time);
    }

    public void setSelectedDimension(int dimension) {
        selectedDimension = dimension;
        dimensionButton.displayString = DIMENSION_NAMES[selectedDimension];
    }

    public boolean isMinimapVisible() {
        return minimapVisibleCheckbox != null && minimapVisibleCheckbox.checked;
    }

    public void setMinimapVisible(boolean minimapVisible) {
        this.minimapVisibleCheckbox.checked = minimapVisible;
    }
}

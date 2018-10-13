package carpetclient.gui.chunkgrid;

import carpetclient.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import carpetclient.coders.Pokechu22.GuiNumericIntTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Extensive GUI clas used for Debug Chunk Window
 * Minimap draw logic also included.
 */
public class GuiChunkGrid extends GuiScreen {

    public static GuiChunkGrid instance;

    public static ChunkGridStyle style = ChunkGridStyle.CHECKERBOARD;

    private Controller controller;
    private ChunkGrid chunkgrid = new ChunkGrid();

    private GuiButton startStopButton;
    private GuiButton loadButton;
    private GuiButton saveButton;
    private GuiButton backButton;
    private GuiButton forwardButton;
    private GuiButton currentButton;
    private GuiButton beginingButton;
    private GuiButton endButton;
    private GuiButton playButton;

    private GuiNumericIntTextField textFieldX;
    private GuiNumericIntTextField textFieldZ;
    private GuiNumericIntTextField textFieldGT;

    private int time;
    private int xText;
    private int zText;

    private static final String[] MINIMAP_NAMES = {"Minimap OFF", "Follow", "Static"};
    private int selectedMinimap = 0;
    private GuiButton minimapButton;

    private static final String[] DIMENSION_NAMES = {"Overworld", "Nether", "End"};
    private int selectedDimension = 0;
    private GuiButton dimensionButton;
    private boolean chunkSelection;
    private String chunkSelectionString;

    // Footer is a grid, makes stuff easier
    private static final int HEADER_HEIGHT = 9;
    private static final int FOOTER_ROW_HEIGHT = 20;
    private static final int FOOTER_ROW_PADDING = 5;
    private static final int FOOTER_ROW_COUNT = 3;
    private static final int FOOTER_HEIGHT = FOOTER_ROW_HEIGHT * FOOTER_ROW_COUNT + FOOTER_ROW_PADDING * (FOOTER_ROW_COUNT + 1);
    private static final int FOOTER_COL_PADDING = 5;
    private static final int FOOTER_COL_COUNT = 5;
    private static final float MINIMAP_X = 0.7f;
    private static final float MINIMAP_Y = 0.05f;
    private static final float MINIMAP_WIDTH = 0.25f;
    private static final float MINIMAP_HEIGHT = 0.45f;
    private int minimapWidth;
    private int minimapHeight;
    private boolean consumeLeftClickOnes = false;

    public GuiChunkGrid() {
        this.controller = new Controller(this);
    }

    // GUI functions

    @Override
    public void initGui() {
        super.initGui();

        addButton(startStopButton = new GuiButton(0, getFooterX(0), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, controller.start ? "Stop" : "Start"));

        addButton(minimapButton = new GuiButton(1, getFooterX(1), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, MINIMAP_NAMES[selectedMinimap]));

        addButton(loadButton = new GuiButton(2, getFooterX(2), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Load"));
        addButton(saveButton = new GuiButton(3, getFooterX(3), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Save"));
        addButton(currentButton = new GuiButton(4, getFooterX(4), getFooterY(0), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Current"));


        addButton(backButton = new GuiButton(5, getFooterX(1), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Back"));
        addButton(forwardButton = new GuiButton(6, getFooterX(2), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Forward"));

        addButton(beginingButton = new GuiButton(7, getFooterX(0), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Begining"));
        addButton(endButton = new GuiButton(8, getFooterX(3), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, "End"));
        addButton(playButton = new GuiButton(9, getFooterX(4), getFooterY(1), getFooterColWidth(), FOOTER_ROW_HEIGHT, controller.play ? "Pause" : "Play"));

        addButton(new GuiButton(10, getFooterX(0), getFooterY(2), getFooterColWidth(), FOOTER_ROW_HEIGHT, "Home"));
        addButton(dimensionButton = new GuiButton(11, getFooterX(1), getFooterY(2), getFooterColWidth(), FOOTER_ROW_HEIGHT, DIMENSION_NAMES[selectedDimension]));

        textFieldX = new GuiNumericIntTextField(12, mc.fontRenderer, getFooterXTextField(2), getFooterY(2), getFooterColWidthTextField(), FOOTER_ROW_HEIGHT) {
            @Override
            public void performTextAction() {
                controller.setX(getText());
            }
        };
        textFieldZ = new GuiNumericIntTextField(13, mc.fontRenderer, getFooterXTextField(3), getFooterY(2), getFooterColWidthTextField(), FOOTER_ROW_HEIGHT) {
            @Override
            public void performTextAction() {
                controller.setZ(getText());
            }
        };
        textFieldGT = new GuiNumericIntTextField(14, mc.fontRenderer, getFooterXTextField(4), getFooterY(2), getFooterColWidthTextField(), FOOTER_ROW_HEIGHT) {
            @Override
            public void performTextAction() {
                controller.setTime(getText());
            }
        };

        loadButton.enabled = saveButton.enabled = playButton.enabled = !controller.start;
        currentButton.enabled = controller.start;

        controller.updateGUI();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (controller.startStop()) {
                    startStopButton.displayString = "Stop";
                    loadButton.enabled = saveButton.enabled = playButton.enabled = false;
                    currentButton.enabled = true;
                } else {
                    startStopButton.displayString = "Start";
                    loadButton.enabled = saveButton.enabled = playButton.enabled = true;
                    currentButton.enabled = false;
                }
                break;
            case 1:
                selectedMinimap = (selectedMinimap + 1) % MINIMAP_NAMES.length;
                minimapButton.displayString = MINIMAP_NAMES[selectedMinimap];
                break;
            case 2:
                controller.load();
                break;
            case 3:
                controller.save();
                break;
            case 4:
                controller.current();
                break;
            case 5:
                controller.back();
                break;
            case 6:
                controller.forward();
                break;
            case 7:
                controller.begining();
                break;
            case 8:
                controller.end();
                break;
            case 9:
                controller.play();
                if (controller.play) {
                    playButton.displayString = "Pause";
                } else {
                    playButton.displayString = "Play";
                }
                break;
            case 10:
                controller.home();
                break;
            case 11:
                selectedDimension = (selectedDimension + 1) % DIMENSION_NAMES.length;
                dimensionButton.displayString = DIMENSION_NAMES[selectedDimension];
                controller.dimentionUpdate();
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
        textFieldGT.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldX.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldZ.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT)
            controller.buttonDown(mouseX, mouseY - HEADER_HEIGHT, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT && !consumeLeftClickOnes)
            controller.buttonUp(mouseX, mouseY - HEADER_HEIGHT, mouseButton);
        consumeLeftClickOnes = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (mouseY >= HEADER_HEIGHT && mouseY < height - FOOTER_HEIGHT)
            controller.mouseDrag(mouseX, mouseY - HEADER_HEIGHT, clickedMouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_F6) {
            mc.displayGuiScreen(null);
        }
        textFieldGT.keyDown(typedChar, keyCode);
        textFieldX.keyDown(typedChar, keyCode);
        textFieldZ.keyDown(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableTexture2D();

        // Chunk grid
        chunkgrid.draw(controller.getView(), 0, HEADER_HEIGHT, width, height - HEADER_HEIGHT - FOOTER_HEIGHT);

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

        if (chunkSelection) {
            drawCenteredString(fontRenderer, chunkSelectionString, width / 2, HEADER_HEIGHT + 10, 0xffffff); // possible indicator for chunk selected
        }

        drawCenteredString(fontRenderer, "X:", getFooterX(2) + 5, getFooterY(2) + FOOTER_ROW_HEIGHT / 2 - 4, 0xffffff);
        textFieldX.drawTextBox();
        drawCenteredString(fontRenderer, "Z:", getFooterX(3) + 5, getFooterY(2) + FOOTER_ROW_HEIGHT / 2 - 4, 0xffffff);
        textFieldZ.drawTextBox();
        drawCenteredString(fontRenderer, "GT:", getFooterX(4) + 4, getFooterY(2) + FOOTER_ROW_HEIGHT / 2 - 4, 0xffffff);
        textFieldGT.drawTextBox();

//        String posText = "X: " + xText + ", Z: " + zText;
//        drawCenteredString(fontRenderer, posText, getFooterX(2) + getFooterColWidth() / 2, getFooterY(2) + FOOTER_ROW_HEIGHT / 2, 0xffffff);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        controller.initMinimap();
    }

    // Minimap

    public void renderMinimap(int screenWidth, int screenHeight) {
        if (isChunkDebugWindowOpen()) return;
        int minimapX = (int) (screenWidth * MINIMAP_X);
        int minimapY = (int) (screenHeight * MINIMAP_Y);
        minimapWidth = (int) (screenWidth * MINIMAP_WIDTH);
        minimapHeight = (int) (screenHeight * MINIMAP_HEIGHT);

        // Actual minimap content
        chunkgrid.draw(controller.getMinimapView(), minimapX, minimapY, minimapWidth, minimapHeight);
    }

    private int getFooterColWidth() {
        return (width - FOOTER_ROW_PADDING * (FOOTER_COL_COUNT + 1)) / FOOTER_COL_COUNT;
    }

    private int getFooterX(int x) {
        return x * getFooterColWidth() + (x + 1) * FOOTER_COL_PADDING;
    }

    private int getFooterXTextField(int x) {
        return x * getFooterColWidth() + (x + 1) * FOOTER_COL_PADDING + 13;
    }

    private int getFooterY(int y) {
        return height - FOOTER_HEIGHT + y * FOOTER_ROW_HEIGHT + (y + 1) * FOOTER_ROW_PADDING;
    }

    private int getFooterColWidthTextField() {
        return (width - FOOTER_ROW_PADDING * (FOOTER_COL_COUNT + 1)) / FOOTER_COL_COUNT - 15;
    }

    // Accessors

    public boolean isChunkDebugWindowOpen() {
        return mc.currentScreen == this || mc.currentScreen instanceof GuiSubWindow;
    }

    public ChunkGrid getChunkGrid() {
        return chunkgrid;
    }

    public int getSelectedDimension() {
        return selectedDimension;
    }

    public void setTime(int time) {
        this.textFieldGT.setText(Integer.toString(time));
    }

    public void setXText(int xText) {
        this.textFieldX.setText(Integer.toString(xText));
    }

    public void setZText(int zText) {
        this.textFieldZ.setText(Integer.toString(zText));
    }

    public void setBackButtonText(String text) {
        this.backButton.displayString = text;
    }

    public void setForwardButtonText(String text) {
        this.forwardButton.displayString = text;
    }

    public void liveUpdate() {
        controller.liveUpdate();
    }

    public void setPlayButtonText(String text) {
        playButton.displayString = text;
    }

    public int getMinimapWidth() {
        return minimapWidth;
    }

    public int getMinimapHeight() {
        return minimapHeight;
    }

    public int windowWidth() {
        return width;
    }

    public int windowHeight() {
        return height - HEADER_HEIGHT - FOOTER_HEIGHT;
    }

    public void setSelectedDimension(int dimension) {
        selectedDimension = dimension;
        dimensionButton.displayString = DIMENSION_NAMES[selectedDimension];
    }

    public void consumeLeftClick() {
        consumeLeftClickOnes = true;
    }

    public int getMinimapType() {
        return selectedMinimap;
    }

    public void selectedChunk(boolean cs, int x, int y) {
        chunkSelection = cs;
        chunkSelectionString = "Chunk selected X: " + x + " Z: " + y;
    }

    public void disableDebugger() {
        startStopButton.displayString = "Start";
        loadButton.enabled = saveButton.enabled = playButton.enabled = true;
        currentButton.enabled = false;
        controller.setStart(false);
        Util.printToChat("Chunk Debug Tool disabled on the server, enable chunkDebugTool rule for use.");
    }

    public Controller getController() {
        return controller;
    }
}

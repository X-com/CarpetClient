package carpetclient.gui.chunkgrid;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

public class ChunkGrid {

    private int screenWidth = 0;
    private int screenHeight = 0;
    private int columnCount = 100;
    private int rowCount = 100;
    private int scale = 10;

    private Point selection = new Point(Integer.MAX_VALUE, 0);
    private Point playerLocation = new Point(Integer.MAX_VALUE, 0);

    public void draw(Chunkdata.MapView view, int thisX, int thisY, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        rowCount = (int) Math.ceil((float) height / scale);
        columnCount = (int) Math.ceil((float) width / scale);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.disableTexture2D();

        if (GuiChunkGrid.style.isGradient())
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

        int originx = view.getLowerX();
        int origonz = view.getLowerZ();

        if (view != null) {
            for (Chunkdata.ChunkView chunkdata : view) {
                int x = chunkdata.getX() - originx;
                int z = chunkdata.getZ() - origonz;
                int rx = x * scale;
                int ry = z * scale;
                int cellX = thisX + rx;
                int cellY = thisY + ry;

                int colors[] = chunkdata.getColors();
                int color = colors[colors.length - 1];
                drawBox(tess, buf, cellX, cellY, x, z, color, scale);
                if (colors.length > 2) {
                    int c = colors[colors.length - 2];
                    drawBox(tess, buf, cellX + scale / 4, cellY + scale / 4, x, z, c, scale / 2);
                }
            }
        }

        drawBox(tess, buf, playerLocation.getX() * scale + thisX, playerLocation.getY() * scale + thisY, 0, 0, 0xff804000, scale);
        if (selection.getX() != Integer.MAX_VALUE) {
            drawSelectionBox(tess, buf, thisX, thisY, 0xfff7f006);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    private void drawBox(Tessellator tess, BufferBuilder buf, int cellX, int cellY, int x, int z, int color, int scal) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0xff00) >> 8;
        int blue = (color & 0xff);
        float brightenFactor = GuiChunkGrid.style.isCheckerboard() ? 0.1f : 0.3f;
        if (red == 0 && green == 0 && blue == 0)
            brightenFactor = 0.2f;

        if (GuiChunkGrid.style.isCheckerboard() && (x + z) % 2 == 0)
            color = brighten(color, brightenFactor);

        int color1, color2;
        if (GuiChunkGrid.style.isGradient()) {
            color1 = brighten(color, brightenFactor);
            color2 = brighten(color1, brightenFactor);
        } else {
            color2 = color1 = color;
        }

        int alpha1 = (color1 & 0xff000000) >>> 24;
        int red1 = (color1 & 0xff0000) >> 16;
        int green1 = (color1 & 0xff00) >> 8;
        int blue1 = (color1 & 0xff);
        int alpha2 = (color2 & 0xff000000) >>> 24;
        int red2 = (color2 & 0xff0000) >> 16;
        int green2 = (color2 & 0xff00) >> 8;
        int blue2 = (color2 & 0xff);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(cellX, cellY, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX, cellY + scal, 0).color(red1, green1, blue1, alpha1).endVertex();
        buf.pos(cellX + scal, cellY + scal, 0).color(red2, green2, blue2, alpha2).endVertex();
        buf.pos(cellX + scal, cellY, 0).color(red1, green1, blue1, alpha1).endVertex();
        tess.draw();
    }

    private void drawSelectionBox(Tessellator tess, BufferBuilder buf, int thisX, int thisY, int color) {
        int alpha = (color & 0xff000000) >>> 24;
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0xff00) >> 8;
        int blue = (color & 0xff);

        int x = selection.getX();
        int z = selection.getY();
        int rx = x * scale;
        int ry = z * scale;
        int cellX = thisX + rx;
        int cellY = thisY + ry;

        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(cellX, cellY, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX, cellY + scale, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX, cellY + scale, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX + scale, cellY + scale, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX + scale, cellY + scale, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX + scale, cellY, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX + scale, cellY, 0).color(red, green, blue, alpha).endVertex();
        buf.pos(cellX, cellY, 0).color(red, green, blue, alpha).endVertex();
        tess.draw();
    }

    public int getGridX(int pixelX) {
        if (scale == 0)
            return 0;
        return pixelX / scale;
    }

    public int getGridY(int pixelY) {
        if (scale == 0)
            return 0;
        return pixelY / scale;
    }

    private static int brighten(int col, float factor) {
        int alpha = (col & 0xff000000) >>> 24;
        int red = (col & 0xff0000) >> 16;
        int green = (col & 0xff00) >> 8;
        int blue = col & 0xff;

        int mix = (int) ((red + green + blue) * factor/3);
        red += mix;
        green += mix;
        blue += mix;
        int redOverflow = Integer.max(red-255,0);
        int greenOverflow = Integer.max(green-255,0);
        int blueOverflow = Integer.max(blue-255,0);
        int overflow = Integer.max(redOverflow,Integer.max(greenOverflow,blueOverflow))/2;
        red = Integer.min(red + overflow,255);
        green = Integer.min(green + overflow,255);
        blue = Integer.min(blue + overflow,255);
        return (alpha << 24)
                | (red << 16)
                | (green << 8)
                | (blue);
    }

    public int sizeX() {
        return columnCount;
    }

    public int sizeZ() {
        return rowCount;
    }

    public void setScale(int width, int height, int value) {
        scale += value;
        if (scale < 3 && !GuiScreen.isCtrlKeyDown()) {
            scale = 3;
        } else if (scale < 1) {
            scale = 1;
        } else if (scale > 50) {
            scale = 50;
        }
        rowCount = height / scale;
        columnCount = width / scale;
    }

    public void setSelectionBox(int x, int y) {
        selection.setLocation(x, y);
    }

    public void playerChunk(int x, int y) {
        playerLocation.setLocation(x, y);
    }

    public int height() {
        return screenHeight;
    }

    public int width() {
        return screenWidth;
    }

    public int size(int window) {
        return (int) Math.ceil((float) window / scale);
    }
}

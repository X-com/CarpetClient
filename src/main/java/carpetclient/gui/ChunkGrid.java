package carpetclient.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

import java.util.HashMap;
import java.util.Map;

public class ChunkGrid {

    private int columnCount = 100;
    private int rowCount = 100;
    //private double cellSize = 0;
    private int scale = 10;

    private int selectionX = -1;
    private int selectionY = -1;

    private Map<Point, Integer> colors = new HashMap<>();

    public void draw(int thisX, int thisY, int width, int height) {

        /*
        System.out.println("===================");
        colors.entrySet().stream().limit(10).forEach(entry -> System.out.println("(" + entry.getKey().getX() + ", " + entry.getKey().getY() + "): " + entry.getValue()));
        */


        rowCount = (int) Math.ceil((float) height / scale);
        columnCount = (int) Math.ceil((float) width / scale);

        double cellHeight = (double) height / rowCount;
        double cellWidth = (double) width / columnCount;

        //cellSize = Math.min(cellHeight, cellWidth);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        for (int z = 0; z < rowCount; ++z) {
            for (int x = 0; x < columnCount; ++x) {
                int rx = x * scale;
                int ry = z * scale;
                int cellX = thisX + rx;
                int cellY = thisY + ry;
                int color = getGridColor(x, z);
                int alpha = (color & 0xff000000) >>> 24;
                int red = (color & 0xff0000) >> 16;
                int green = (color & 0xff00) >> 8;
                int blue = (color & 0xff);
                int color1 = brighten(color);
                int alpha1 = (color1 & 0xff000000) >>> 24;
                int red1 = (color1 & 0xff0000) >> 16;
                int green1 = (color1 & 0xff00) >> 8;
                int blue1 = (color1 & 0xff);
                int color2 = brighten(color1);
                int alpha2 = (color2 & 0xff000000) >>> 24;
                int red2 = (color2 & 0xff0000) >> 16;
                int green2 = (color2 & 0xff00) >> 8;
                int blue2 = (color2 & 0xff);

                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(cellX, cellY, 0).color(red, green, blue, alpha).endVertex();
                buf.pos(cellX, cellY + scale, 0).color(red1, green1, blue1, alpha1).endVertex();
                buf.pos(cellX + scale, cellY + scale, 0).color(red2, green2, blue2, alpha2).endVertex();
                buf.pos(cellX + scale, cellY, 0).color(red1, green1, blue1, alpha1).endVertex();
                tess.draw();
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
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

    public void showSelection(int x, int y) {
        selectionX = x;
        selectionY = y;
    }

    public void setGrid(int scaledWidth, int scaledHeight, Map<Point, Integer> colors) {
        this.colors.clear();
        this.colors.putAll(colors);
        this.columnCount = scaledWidth;
        this.rowCount = scaledHeight;
    }

    public void setGridColor(int x, int z, int color) {
        colors.put(new Point(x, z), color);
    }

    public int getGridColor(int x, int z) {
        Integer col = colors.get(new Point(x, z));
        if (col == null) return 0xff000000;
        //if ((x + z) % 2 == 0) return brighten(col);
        return col;
    }

    private static int brighten(int col) {
        int alpha = (col & 0xff000000) >>> 24;
        int red = (col & 0xff0000) >> 16;
        int green = (col & 0xff00) >> 8;
        int blue = col & 0xff;

        // redn't green't and bluen't are technical terms
        int rednt = 255 - red, greent = 255 - green, bluent = 255 - blue;
        rednt = (int) (rednt * 0.9);
        greent = (int) (greent * 0.9);
        bluent = (int) (bluent * 0.9);
        red = 255 - rednt;
        green = 255 - greent;
        blue = 255 - bluent;

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
        if (scale < 5) {
            scale = 5;
        } else if (scale > 50) {
            scale = 50;
        }
        rowCount = height / scale;
        columnCount = width / scale;
    }

    public void clearColors() {
        colors.clear();
    }
}

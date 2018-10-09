package carpetclient.gui;

import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ChunkGrid {

    private int screenWidth = 0;
    private int screenHeight = 0;
    private int gridWidth = 100;
    private int gridHeight = 100;
    private int cellSize = 0;
    private int scale = 10;

    Point selection = new Point(Integer.MAX_VALUE, 0);

    private Map<Point, Integer> colors = new HashMap<>();

    public void draw(int thisX, int thisY, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        gridHeight = height / scale;
        gridWidth = width / scale;

        int cellHeight = height / gridHeight;
        int cellWidth = width / gridWidth;

        cellSize = Math.min(cellHeight, cellWidth);
//        double frame = 0;

//        drawRect(thisX, thisY, thisX + width - 1, thisY + height - 1, 0xff808080);

        for (int z = 0; z <= gridHeight; ++z) {
            for (int x = 0; x <= gridWidth; ++x) {
                int rx = x * cellSize;
                int ry = z * cellSize;
                Gui.drawRect(thisX + rx,
                        thisY + ry,
                        thisX + rx + cellSize,
                        thisY + ry + cellSize,
                        getGridColor(x, z));
            }
        }

        if (selection.x != Integer.MAX_VALUE) {
            int rx = selection.x * cellSize;
            int ry = selection.y * cellSize;
            Gui.drawRect(thisX + rx,
                    thisY + ry,
                    thisX + rx + cellSize,
                    thisY + ry + cellSize,
                    0xff646245);
        }
    }

    public int getGridX(int pixelX) {
        if (cellSize == 0)
            return 0;
        return pixelX / cellSize;
    }

    public int getGridY(int pixelY) {
        if (cellSize == 0)
            return 0;
        return pixelY / cellSize;
    }

    public void setGridColor(int x, int z, int color) {
        colors.put(new Point(x, z), color);
    }

    public int getGridColor(int x, int z) {
        Integer col = colors.get(new Point(x, z));
        if (col == null) return 0xff000000;
        if ((x + z) % 2 == 0) return brighten(col);
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
        return gridWidth;
    }

    public int sizeZ() {
        return gridHeight;
    }

    public void setScale(int width, int height, int value) {
        scale += value;
        if (scale < 5) {
            scale = 5;
        } else if (scale > 50) {
            scale = 50;
        }
        gridHeight = height / scale;
        gridWidth = width / scale;
    }

    public void clearColors() {
        colors.clear();
    }

    public void setSelectionBox(int x, int y) {
        selection.setLocation(x, y);
    }

    public int height() {
        return screenHeight;
    }

    public int width() {
        return screenWidth;
    }
}

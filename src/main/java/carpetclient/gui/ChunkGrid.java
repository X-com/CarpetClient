package carpetclient.gui;

import net.minecraft.client.gui.Gui;
import org.lwjgl.util.Point;

import java.util.HashMap;
import java.util.Map;

public class ChunkGrid {

    private int scaledWidth = 100;
    private int scaledHeight = 100;
    private int cellSize = 0;
    private int scale = 10;

    private int selectionX = -1;
    private int selectionY = -1;

    private Map<Point, Integer> colors = new HashMap<>();

    public void draw(int thisX, int thisY, int width, int height) {
        scaledHeight = height / scale;
        scaledWidth = width / scale;

        int cellHeight = height / scaledHeight;
        int cellWidth = width / scaledWidth;

        cellSize = Math.min(cellHeight, cellWidth);
        int frame = 1;

        Gui.drawRect(thisX, thisY, thisX + width - 1, thisY + height - 1, 0xffffffff);
        Gui.drawRect(thisX + selectionX * cellSize,
                thisY + selectionY * cellSize,
                thisX + selectionX * cellSize + cellSize - 1,
                thisY + selectionY * cellSize + cellSize - 1,
                0xff000000);

        for (int z = 0; z < scaledHeight; ++z) {
            for (int x = 0; x < scaledWidth; ++x) {
                int rx = x * cellSize;
                int ry = z * cellSize;
                Gui.drawRect(thisX + rx + frame,
                        thisY + ry + frame,
                        thisX + rx + frame + cellSize - 2 * frame - 1,
                        thisY + ry + frame + cellSize - 2 * frame - 1,
                        getGridColor(x, z));
            }
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

    public void showSelection(int x, int y) {
        selectionX = x;
        selectionY = y;
    }

    public void setGrid(int scaledWidth, int scaledHeight, Map<Point, Integer> colors) {
        this.colors.clear();
        this.colors.putAll(colors);
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
    }

    public void setGridColor(int x, int z, int color) {
        colors.put(new Point(x, z), color);
    }

    public int getGridColor(int x, int z) {
        Integer col = colors.get(new Point(x, z));
        return col == null ? 0xff000000 : col;
    }

    public int sizeX() {
        return scaledWidth;
    }

    public int sizeZ() {
        return scaledHeight;
    }

    public void setScale(int width, int height, int value) {
        scale += value;
        if (scale < 5) {
            scale = 5;
        } else if (scale > 50) {
            scale = 50;
        }
        scaledHeight = height / scale;
        scaledWidth = width / scale;
    }

    public void clearColors() {
        colors.clear();
    }
}

package carpetclient.gui;

import net.minecraft.client.gui.Gui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Chunkgrid {

    int gridX = 100;
    int gridZ = 100;
    int grid = 0;
    int scale = 10;

    int activex = -1;
    int activey = -1;

    int[] colors = new int[1000000];

    public Chunkgrid() {
    }

    public void draw(int thisX, int thisY, int width, int height) {
        gridZ = height / scale;
        gridX = width / scale;

        int gridH = height / gridZ;
        int gridW = width / gridX;

        grid = Math.min(gridH, gridW);
        int frame = 1;

        Gui.drawRect(thisX, thisY, thisX + width - 1, thisY + height - 1, 0xffffffff);
        Gui.drawRect(thisX + activex * grid, thisY + activey * grid, thisX + activex * grid + grid - 1, thisY + activey * grid + grid - 1, 0xff000000);

        for (int z = 0; z < gridZ; ++z) {
            for (int x = 0; x < gridX; ++x) {
                int rx = x * grid;
                int ry = z * grid;
                int i = x + gridX * z;
                Gui.drawRect(thisX + rx + frame,
                        thisY + ry + frame,
                        thisX + rx + frame + grid - 2 * frame - 1,
                        thisY + ry + frame + grid - 2 * frame - 1,
                        colors[i]);
            }
        }
    }

    public int getGridX(int pixelX) {
        if (grid == 0)
            return 0;
        return pixelX / grid;
    }

    public int getGridY(int pixelY) {
        if (grid == 0)
            return 0;
        return pixelY / grid;
    }

    public void showSelection(int x, int y) {
        activex = x;
        activey = y;
    }

    public void setGrid(int sx, int sy, int[] colors) {
        this.colors = colors;
        this.gridX = sx;
        this.gridZ = sy;
    }

    public void setGridColor(int x, int z, int color) {
        colors[x + gridX * z] = color;
    }

    public int sizeX() {
        return gridX;
    }

    public int sizeZ() {
        return gridZ;
    }

    public void setScale(int width, int height, int value) {
        scale += value;
        if (scale < 5) {
            scale = 5;
        } else if (scale > 50) {
            scale = 50;
        }
        gridZ = height / scale;
        gridX = width / scale;
    }

    public void clearColors() {
        Arrays.fill(colors, 0xff000000);
    }
}

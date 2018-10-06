package carpetclient.gui;

import javax.swing.*;
import java.awt.*;

public class Chunkgrid extends JPanel {

    int gridX = 100;
    int gridZ = 100;
    int grid = 0;
    int scale = 10;

    int activex = -1;
    int activey = -1;

    Color[] colors = new Color[1000000];

    public Chunkgrid() {
    }

    @Override
    public void paintComponent(Graphics g) {
        gridZ = getHeight() / scale;
        gridX = getWidth() / scale;

        int gridH = getHeight() / gridZ;
        int gridW = getWidth() / gridX;

        grid = Math.min(gridH, gridW);
        int frame = 1;

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.fillRect(activex * grid, activey * grid, grid, grid);

        for (int z = 0; z < gridZ; ++z) {
            for (int x = 0; x < gridX; ++x) {
                int rx = x * grid;
                int ry = z * grid;
                int i = x + gridX * z;
                g.setColor(colors[i]);
                g.fillRect(rx + frame, ry + frame, grid - 2 * frame, grid - 2 * frame);
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

    public void setGrid(int sx, int sy, Color[] colors) {
        this.colors = colors;
        this.gridX = sx;
        this.gridZ = sy;
    }

    public void setGridColor(int x, int z, Color color) {
        colors[x + gridX * z] = color;
    }

    public int sizeX() {
        return gridX;
    }

    public int sizeZ() {
        return gridZ;
    }

    public void setScale(int value) {
        scale += value;
        if (scale < 5) {
            scale = 5;
        } else if (scale > 50) {
            scale = 50;
        }
        gridZ = getHeight() / scale;
        gridX = getWidth() / scale;
    }

    public void clearColors() {
        for (int i = 0; i < 1000000; i++) {
            colors[i] = Color.BLACK;
        }
    }
}

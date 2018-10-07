package carpetclient.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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
        double frame = 0.13;

        drawRect(thisX, thisY, thisX + width - 1, thisY + height - 1, 0xff808080);
        drawRect(thisX + activex * grid, thisY + activey * grid, thisX + activex * grid + grid - 1, thisY + activey * grid + grid - 1, 0xff000000);

        for (int z = 0; z < gridZ; ++z) {
            for (int x = 0; x < gridX; ++x) {
                int rx = x * grid;
                int ry = z * grid;
                int i = x + gridX * z;
                drawRect(thisX + rx + frame,
                        thisY + ry + frame,
                        thisX + rx + frame + grid - frame,
                        thisY + ry + frame + grid - frame,
                        colors[i]);
            }
        }
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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
        int index = x + gridX * z;
        if (index < 0 || index >= colors.length) {
            return;
        }
        colors[index] = color;
    }

    public int sizeX() {
        return gridX;
    }

    public int sizeZ() {
        return gridZ;
    }

    public void setScale(int width, int height, int value) {
        scale += value;
        System.out.println("scroll " + value);
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

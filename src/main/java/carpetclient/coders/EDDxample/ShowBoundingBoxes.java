package carpetclient.coders.EDDxample;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import carpetclient.gui.ClientGUI;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

/*
Code inspeiration from EDDxample.

A class to show bounding boxes of different structures on the server.
 */
public class ShowBoundingBoxes {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static final int OUTER_BOUNDING_BOX = 0;
    public static final int END_CITY = 1;
    public static final int FORTRESS = 2;
    public static final int TEMPLE = 3;
    public static final int VILLAGE = 4;
    public static final int STRONGHOLD = 5;
    public static final int MINESHAFT = 6;
    public static final int MONUMENT = 7;
    public static final int MANTION = 8;
    public static final int SLIME_CHUNKS = 9;

    public static boolean[] show = {
            true,
            true,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false
    };

    private static ArrayList<StructureBoundingBox>[] group = new ArrayList[9];
    private static Color[] colors = {
            new Color(0xFFFF00), //0
            new Color(0xFF0000), //1
            new Color(0xFF0000), //2
            new Color(0x00FF00), //3
            new Color(0xFFFFFF), //4
            new Color(0xFFFFFF), //5
            new Color(0xFFFFFF), //6
            new Color(0x0000FF), //7
            new Color(0x00FF00), //8
    };

    public static final int renderDist = 160;
    public static long seed = 0;
    public static int dimension = -2;

    static {
        for (int i = 0; i < group.length; i++) {
            group[i] = new ArrayList<StructureBoundingBox>();
        }
    }

    /**
     * Main render method to render the bounding boxes
     *
     * @param partialTicks
     */
    public static void RenderStructures(float partialTicks) {
        if (group == null) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        RenderUtils.prepareOpenGL(true);

        if (show[SLIME_CHUNKS]) {
            int cnkX = (int) player.posX / 16;
            int cnkZ = (int) player.posZ / 16;

            for (int ChunkX = cnkX - 10; ChunkX < cnkX + 10; ChunkX++) {
                for (int ChunkZ = cnkZ - 10; ChunkZ < cnkZ + 10; ChunkZ++) {
                    if (mc.world.provider.getDimensionType() == DimensionType.OVERWORLD && new Chunk(player.world, ChunkX, ChunkZ).getRandomWithSeed(seed).nextInt(10) == 0) {
                        RenderUtils.drawBox(d0, d1, d2, (ChunkX << 4), 0, (ChunkZ << 4), (ChunkX << 4) + 16, 40, (ChunkZ << 4) + 16, new Color(0x00FF00));
                    }
                }
            }
        }

        if (player.dimension == dimension) {
            for (int i = 0; i < group.length; i++) {
                if (!show[i]) continue;
                ArrayList<StructureBoundingBox> array = group[i];
                if (array == null) return;
                for (StructureBoundingBox box : array) {
                    if (insideRenderDistance(box, player)) {
                        RenderUtils.drawBox(d0, d1, d2, box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1, colors[i]);
                    }
                }
            }
        }

        RenderUtils.prepareOpenGL(false);
    }

    /**
     * Calculates if the bounding box is within render distance to the player.
     *
     * @param box    The bounding box that is to be displayed.
     * @param player Relation to the player that the boxes should be displayed.
     * @return If within range returns true.
     */
    private static boolean insideRenderDistance(StructureBoundingBox box, EntityPlayerSP player) {
        int minX = (int) player.posX - renderDist;
        int maxX = (int) player.posX + renderDist;

        if (box.maxX < minX || box.minX > maxX) {
            return false;
        }

        int minZ = (int) player.posZ - renderDist;
        int maxZ = (int) player.posZ + renderDist;

        if (box.maxZ < minZ || box.minZ > maxZ) {
            return false;
        }

        return true;
    }

    /**
     * The reciever for the data that is being sent from the server
     *
     * @param data Data from the server.
     */
    public static void getStructureComponent(PacketBuffer data) {
        NBTTagCompound nbt = null;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        World worldIn = Minecraft.getMinecraft().world;
        if (nbt != null && worldIn != null) {
            for (int i = 0; i < group.length; i++) {
                group[i].clear();
            }

            NBTTagList nbttaglist = nbt.getTagList("Boxes", 9);
            dimension = nbt.getInteger("Dimention");
            seed = nbt.getLong("Seed");

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagList boxList = (NBTTagList) nbttaglist.get(i);
                for (int j = 0; j < boxList.tagCount(); ++j) {
                    NBTTagCompound compound = boxList.getCompoundTagAt(j);
                    int type = compound.getInteger("type");
                    StructureBoundingBox boundingBox = new StructureBoundingBox(compound.getIntArray("bb"));
                    group[type].add(boundingBox);
                }
            }
        }
    }

    /**
     * Display options method that the GUI uses to update what types of structures should be displayed.
     *
     * @param buttonID The id of the sturcture to be toggled.
     */
    public static void guiBoudingBoxOptions(int buttonID) {
        show[buttonID] = !show[buttonID];
        ClientGUI.display();
    }
}

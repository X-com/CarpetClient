package carpetclient.coders.EDDxample;

import carpetclient.gui.ClientGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/*
Code inspeiration from EDDxample.

A class to show bounding boxes of different structures on the server.
 */
public class ShowBoundingBoxes {
    private static Random randy = new Random();
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

    private static ArrayList<StructureBoundingBox>[] group = new ArrayList[10];
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
            new Color(0x00FF00), //9
    };

    public static final int renderDist = 160;
    public static long seed = 0;
    public static int dimension = -2;
    private static int expectedStructureCount = 0;
    private static int structureCount = 0;

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
            ArrayList<StructureBoundingBox> array = group[SLIME_CHUNKS];
            if (array == null) return;
            for (StructureBoundingBox box : array) {
                if (insideRenderDistance(box, player)) {
                    RenderUtils.drawBox(d0, d1, d2, box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1, colors[SLIME_CHUNKS]);
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
            NBTTagList nbttaglist = nbt.getTagList("Boxes", 9);
            
            ArrayList<NBTTagCompound> allBoxes = new ArrayList<>();
            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                NBTTagList boxList = (NBTTagList) nbttaglist.get(i);
                for (int j = 0; j < boxList.tagCount(); j++) {
                    allBoxes.add(boxList.getCompoundTagAt(j));
                }
            }
            
            structureComponentInitialSettings(nbt, allBoxes.size());
            
            for (NBTTagCompound box : allBoxes)
                addStructure(box);
        }
    }
    
    public static void largeBoundingBoxStructuresStart(PacketBuffer data) {
        NBTTagCompound nbt = null;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        if (nbt != null && Minecraft.getMinecraft().world != null) {
            int expected = data.readVarInt();
            if (expected >= 100000)
                expected = 100000;
            structureComponentInitialSettings(nbt, expected);
        }
    }
    
    public static void largeBoundingBoxStructures(PacketBuffer data) {
        int count = data.readUnsignedByte() + 1;
        for (int i = 0; i < count; i++) {
            NBTTagCompound nbt = null;
            try {
                nbt = data.readCompoundTag();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (nbt != null && Minecraft.getMinecraft().world != null) {
                addStructure(nbt);
            }
        }
    }
    
    public static void structureComponentInitialSettings(NBTTagCompound nbt, int expectedStructureCount_) {
        structureCount = 0;
        expectedStructureCount = expectedStructureCount_;
        
        dimension = nbt.getInteger("Dimention");
        seed = nbt.getLong("Seed");
        
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        int cnkX = (int) player.posX / 16;
        int cnkZ = (int) player.posZ / 16;

        for (int ChunkX = cnkX - 20; ChunkX < cnkX + 20; ChunkX++) {
            for (int ChunkZ = cnkZ - 20; ChunkZ < cnkZ + 20; ChunkZ++) {
                if (mc.world.provider.getDimensionType() == DimensionType.OVERWORLD && isSlimeChunk(ChunkX, ChunkZ, seed)) {
                    StructureBoundingBox boundingBox = new StructureBoundingBox(ChunkX << 4, 0, ChunkZ << 4, (ChunkX << 4) + 16, 40, (ChunkZ << 4) + 16);
                    group[SLIME_CHUNKS].add(boundingBox);
                }
            }
        }
    }
    
    public static void addStructure(NBTTagCompound compound) {
        if (structureCount >= expectedStructureCount)
            return;
        
        int type = compound.getInteger("type");
        StructureBoundingBox boundingBox = new StructureBoundingBox(compound.getIntArray("bb"));
        group[type].add(boundingBox);
        
        structureCount++;
    }

    private static boolean isSlimeChunk(int x, int z, long seed) {
        randy.setSeed(seed + (long)(x * x * 4987142) + (long)(x * 5947611) + (long)(z * z) * 4392871L + (long)(z * 389711) ^ 987234911L);
        return randy.nextInt(10) == 0;
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
    
    public static void clear(){
        for (ArrayList<StructureBoundingBox> l : group)
            l.clear();
        structureCount = 0;
        expectedStructureCount = 0;
    }
}

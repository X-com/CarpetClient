package carpetclient.coders.EDDxample;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import carpetclient.gui.ClientGUI;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

/*
Code from EDDxample used to render Village markers.
 */
public class VillageMarker {

    public static final Color[] colors = {new Color(0x00ffff), new Color(0xff00ff), new Color(0xffff00), new Color(0x0000ff), new Color(0x00ff00), new Color(0xff0000)};

    public static int lastTick = 0;
    static HashMap<BlockPos, Color> centers = new HashMap<BlockPos, Color>();
    static HashMap<List<Integer>, Color> radii = new HashMap<List<Integer>, Color>();
    static List<List<Integer>> doors = new ArrayList<List<Integer>>();
    static int expectedVillageCount = 0;
    static int villageCount = 0;
    
	/* ===== SETTINGS ===== */

    public static final int sphereDensity = 80;
    public static boolean population = true,
            golem = true,
            lines = true;
    public static int village_radius = 1, // 0 = OFF, 1 = DOTS, 2 = LINES, 3 = CIRCLE
            door_radius = 0;
    public static final String[] modes = {"OFF", "DOTS", "LINES", "CIRCLE"};

    /**
     * Draws the villages every frame
     */
    public static void RenderVillages(float partialTicks) {
        final boolean _radius = population || village_radius != 0 || door_radius != 0;

        if (!golem && !_radius && !lines) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        Color color = null;
        BlockPos center = null;
        
        /* ===== ENABLE OPENGL STUFF ===== */

        RenderUtils.prepareOpenGL(true);
        
		/* ===== GOLEM CAGE ===== */

        if (golem && !centers.isEmpty()) {
            for (Map.Entry<BlockPos, Color> entry : centers.entrySet()) {
                center = entry.getKey();
                color = entry.getValue();
                RenderUtils.drawBox(d0, d1, d2, center.getX() - 8, center.getY() - 3, center.getZ() - 8, center.getX() + 8, center.getY() + 3, center.getZ() + 8, color);
            }
        }
        
		/* ===== RADII STUFF ==== */

        if (_radius && !radii.isEmpty()) {
            for (Map.Entry<List<Integer>, Color> entry : radii.entrySet()) {
                List<Integer> ints = entry.getKey();
                center = new BlockPos(ints.get(0), ints.get(1), ints.get(2));
                int r = ints.get(3);
                color = entry.getValue();
                
				/* === POPULATION CAGE === */

                if (population)
                    RenderUtils.drawBox(d0, d1, d2, center.getX() - r, center.getY() - 4, center.getZ() - r, center.getX() + r, center.getY() + 4, center.getZ() + r, color);
                
				/* === SPHERES === */

                if (village_radius != 0)
                    RenderUtils.drawSphere(r, sphereDensity * 2, d0, d1, d2, center.getX(), center.getY(), center.getZ(), color, village_radius);
                if (door_radius != 0)
                    RenderUtils.drawSphere(r + 32, sphereDensity * 2, d0, d1, d2, center.getX(), center.getY(), center.getZ(), color, door_radius);
            }
        }

		/* ===== DOORS ==== */

        if (lines) {
            for (List<Integer> l : doors) {
                color = new Color(l.get(0));
                center = new BlockPos(l.get(1), l.get(2), l.get(3));
                for (int j = 4; j + 2 < l.size(); j += 3) {
                    RenderUtils.drawline(d0, d1, d2, l.get(j), l.get(j + 1) + 0.01, l.get(j + 2), center.getX(), center.getY() + 0.01, center.getZ(), color);
                }
            }
        }
        
        /* ===== DISABLE OPENGL STUFF ===== */

        RenderUtils.prepareOpenGL(false);
    }

    /**
     * Sets the villages to draw once per tick (better than once per frame I guess :P)
     */
    public static void genLists(List<Village> villages) {
        clearLists(villages.size());
        boolean _golem = golem, _radius = population || village_radius != 0 || door_radius != 0, _lines = lines;
        if (_golem || _radius || _lines) {
            for (int i = 0; i < villages.size(); i++) {
                addVillageToList(villages.get(i));
            }
        }
    }
    
    public static void addVillageToList(Village village) {
        if (villageCount >= expectedVillageCount)
            return;
        
        boolean _golem = golem, _radius = population || village_radius != 0 || door_radius != 0, _lines = lines;

        if (_golem || _radius || _lines) {
            BlockPos center = village.getCenter();
            int r = village.getVillageRadius();
            Color color = colors[villageCount % colors.length];

            if (_golem) centers.put(center, color);

            List<Integer> l = new ArrayList<Integer>();
            addPos(l, center);
            l.add(r);

            if (_radius) radii.put(l, color);

            if (_lines) {
                List<Integer> list = new ArrayList<Integer>(64);
                doors.add(list);
                list.add(color.getRGB());
                addPos(list, center);
                for (VillageDoorInfo d : village.getVillageDoorInfoList()) {
                    addPos(list, d.getDoorBlockPos());
                }
            }
            
            villageCount++;
        }
    }

    public static List<Integer> addPos(List<Integer> list, BlockPos pos) {
        list.add(pos.getX());
        list.add(pos.getY());
        list.add(pos.getZ());
        return list;
    }

    public static void villageUpdate(PacketBuffer data) {
        List<Village> villageList = Lists.<Village>newArrayList();
        NBTTagCompound nbt = null;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nbt != null) {
            NBTTagList nbttaglist = nbt.getTagList("Villages", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                Village village = new Village();
                village.readVillageDataFromNBT(nbttagcompound);
                villageList.add(village);
            }

            genLists(villageList);
        }
    }
    
    public static void largeVillageUpdate(PacketBuffer data) {
        int count = data.readUnsignedByte() + 1;
        for (int i = 0; i < count; i++) {
            NBTTagCompound villageTag;
            try {
                villageTag = data.readCompoundTag();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (villageTag != null) {
                Village village = new Village();
                village.readVillageDataFromNBT(villageTag);
                VillageMarker.addVillageToList(village);
            }
        }
    }

    public static void guiVillageOptions(int buttonID) {
        switch (buttonID) {
            case 0:
                lines = !lines;
                break;
            case 1:
                golem = !golem;
                break;
            case 2:
                population = !population;
                break;
            case 3:
                village_radius = (village_radius + 1) % modes.length;
                break;
            case 4:
                door_radius = (door_radius + 1) % modes.length;
                break;
        }
        ClientGUI.display();
    }

    public static void clearLists(int expectedCount) {
        centers.clear();
        radii.clear();
        doors.clear();
        expectedVillageCount = expectedCount;
        villageCount = 0;
    }
}

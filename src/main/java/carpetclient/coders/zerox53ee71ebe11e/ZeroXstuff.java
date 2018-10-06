package carpetclient.coders.zerox53ee71ebe11e;

import carpetclient.gui.DebugWindow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

// temp stuff
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import net.minecraft.world.World;
import com.google.gson.Gson;
// temp stuff end


public class ZeroXstuff {

    public static final int PACKET_EVENTS = 0;
    public static final int PACKET_STACKTRACE = 1;
    public static Chunkdata data = new Chunkdata();

    public static void chunkLogger(PacketBuffer data) {
        int type = data.readInt();

        NBTTagCompound nbt;
        try {
            nbt = data.readCompoundTag();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nbt == null) return;

        if (PACKET_EVENTS == type) {
            unpackNBT(nbt);
        } else if (PACKET_STACKTRACE == type) {
            unpackNBTStackTrace(nbt);
        }
    }

    private static void unpackNBTStackTrace(NBTTagCompound nbt) {
        NBTTagList nbttaglist = nbt.getTagList("stackList", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            addStackTrace(nbttagcompound);
        }
    }

    private static void addStackTrace(NBTTagCompound nbttagcompound) {
        int id = nbttagcompound.getInteger("id");
        String stack = nbttagcompound.getString("stack");
        data.addStacktrace(stack);
//        System.out.println("stack " + id + " " + stack);
    }

    private static void unpackNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("data", 10);
        int time = nbt.getInteger("time");
//        ChunkLogData.TimeIndex timeIndex = data.addData(time);
        for(int index = 0; index < list.tagCount(); ++index) {
            NBTTagCompound chunk = list.getCompoundTagAt(index);
            
            int x = chunk.getInteger("x");
            int z = chunk.getInteger("z");
            int dimention = chunk.getInteger("d");
            int event = chunk.getInteger("event");
            int stacktrace = chunk.getInteger("trace");

//            System.out.println("X: " + x + " Z: " + z + " D: " + dimention + " E: "+ event + " S: " + stacktrace);
//            timeIndex.addToDimention(dimention, x, z, event, stacktrace, index);
            data.addData(time, index, x, z, dimention, event, stacktrace);
        }

        DebugWindow.debug.updateCanvas(time);
    }
    
    /////////// temp stuff ///////////////

    static Gson gson = new Gson();
    static final String logdir = "chunklog";
    String name = "Tempname";

    void dumpLog(World w) {
        int gameticks = w.getMinecraftServer().getTickCounter();
        String s = gson.toJson(this);

        Path file = Paths.get(".",logdir, name , "new.json");
        try {
            File f = Files.createFile(file).toFile();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f,false), StandardCharsets.UTF_8);
            writer.write(s);
            writer.flush();
            writer.close();
            Path file2 = Paths.get(".",logdir, name, ""+ gameticks + ".json");
            Files.move(file, file2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // temp stuff end
}

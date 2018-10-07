package carpetclient.gui;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import carpetclient.coders.zerox53ee71ebe11e.ZeroXstuff;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Map;
import java.util.SortedMap;

public class Controller {
    GuiChunkGrid debug;
    boolean start = false;
    private boolean live = false;
    private int lastGametick;
    private int viewX;
    private int viewZ;
    private Chunkdata.MapView chunkData;

    public Controller(GuiChunkGrid d) {
        debug = d;
        lastGametick = 0;
        chunkData = ZeroXstuff.data.getChunkData();
    }

    public boolean startStop() {
        start = !start;

        live = true;
        if (start) {
            home();
            ZeroXstuff.data.clear();
        }
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.CHUNK_LOGGER);
        sender.writeBoolean(start); // this is the bit to turn on or off
//        sender.writeBoolean(debug.areStackTracesEnabled()); // this is the bit to send stacktraces
        sender.writeBoolean(true);

        CarpetPluginChannel.packatSender(sender);

        return start;
    }

    public void load() {
        JFrame frame = new JFrame();
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rval = fc.showOpenDialog(frame);
        if (rval == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getPath();
            try {
                ZeroXstuff.data.readObject(new ObjectInputStream(new FileInputStream(path)));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        setTick(ZeroXstuff.data.getFirstGametick());
    }

    public void save() {
        JFrame frame = new JFrame();
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int rval = fc.showSaveDialog(frame);
        if (rval == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getPath();
            try {
                ZeroXstuff.data.writeObject(new ObjectOutputStream(new FileOutputStream(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void back() {
        live = false;
        setTick(ZeroXstuff.data.getPrevGametick(lastGametick));
    }

    public void forward() {
        live = false;
        setTick(ZeroXstuff.data.getNextGametick(lastGametick));
    }

    public void current() {
        live = true;
        setTick(ZeroXstuff.data.getLastGametick());
    }

    public void comboBoxAction() {
        setTick(lastGametick);
    }

    public void setTime(KeyEvent e) {
    }

    public void home() {
        BlockPos pos = Minecraft.getMinecraft().player.getPosition();
        viewX = pos.getX() >> 4;
        viewZ = pos.getZ() >> 4;

        debug.setXText(viewX);
        debug.setZText(viewZ);

        changeDimentionTo(Minecraft.getMinecraft().player.dimension);

        setMapViewData();
        setTick(lastGametick);
    }

    private void changeDimentionTo(int dimension) {
        if (dimension == -1) {
            debug.setSelectedDimension(1);
        } else if (dimension == 1) {
            debug.setSelectedDimension(2);
        } else if (dimension == 0) {
            debug.setSelectedDimension(0);
        }
    }

    public void setX(KeyEvent e, JTextArea textX) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        viewX = integerInputs(e, textX, viewX);
        textX.setText(Integer.toString(viewX));
        setMapViewData();
        setTick(lastGametick);
    }

    public void setZ(KeyEvent e, JTextArea textZ) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        viewZ = integerInputs(e, textZ, viewZ);
        textZ.setText(Integer.toString(viewZ));
        setMapViewData();
        setTick(lastGametick);
    }

    private int integerInputs(KeyEvent e, JTextArea box, int oldNum) {
        e.consume();
        String text = box.getText();
        int newNum;
        try {
            newNum = Integer.parseInt(text);
        } catch (Exception exception) {
            return oldNum;
        }
        return newNum;
    }

    public void liveUpdate(int time) {
        if (!live) return;
        setTick(time);
    }

    private void setMapViewData() {
        ChunkGrid canvas = debug.getChunkGrid();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int minX = viewX - sizeX / 2;
        int maxX = viewX + sizeX / 2;
        int minZ = viewZ - sizeZ / 2;
        int maxZ = viewZ + sizeZ / 2;

        int dimention = debug.getSelectedDimension();

        chunkData.seekSpace(dimention, minX, maxX, minZ, maxZ);
    }

    void setTick(int gametick) {
        int dimention = debug.getSelectedDimension();
        ChunkGrid canvas = debug.getChunkGrid();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int minX = viewX - sizeX / 2;
        int maxX = viewX + sizeX / 2;
        int minZ = viewZ - sizeZ / 2;
        int maxZ = viewZ + sizeZ / 2;

        canvas.clearColors();

        chunkData.seekTime(gametick);

        SortedMap<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> list;

        if (debug.areStackTracesEnabled()) { // temporary hackfix to display 2 data types
            list = chunkData.getDisplayArea();
        } else {
            list = ZeroXstuff.data.getAllLogsForDisplayArea(gametick, dimention, minX, maxX, minZ, maxZ);
        }
        for (Map.Entry<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> entry : list.entrySet()) {
            Chunkdata.ChunkLogCoords chunk = entry.getKey();
            if (chunk == null) continue;
            Chunkdata.ChunkLogEvent event = list.get(chunk);
            if (event == null) {
                canvas.setGridColor(chunk.space.x - minX, chunk.space.z - minZ, getColor(Chunkdata.Event.MISSED_EVENT_ERROR));
                continue;
            }
            canvas.setGridColor(chunk.space.x - minX, chunk.space.z - minZ, getColor(event.event));
        }

        lastGametick = gametick;

        debug.setTime(gametick);
    }

    private int getOffsetX(int x, ChunkGrid canvas) {
        return x + viewX - canvas.sizeX() / 2;
    }

    private int getOffsetZ(int z, ChunkGrid canvas) {
        return z + viewZ - canvas.sizeZ() / 2;
    }

    public void selectchunk(int x, int y) {
        ChunkGrid canvas = debug.getChunkGrid();
        int dimention = debug.getSelectedDimension();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int cx = canvas.getGridX(x);
        int cz = canvas.getGridY(y);

        int minX = viewX - sizeX / 2;
        int maxX = viewX + sizeX / 2;
        int minZ = viewZ - sizeZ / 2;
        int maxZ = viewZ + sizeZ / 2;

        System.out.println("Selected: " + cx + " " + cz + " " + (cx + minX) + " " + (cz + minZ));
        SortedMap<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> list = ZeroXstuff.data.getAllLogsForDisplayArea(lastGametick, dimention, minX, maxX, minZ, maxZ);
        for (Map.Entry<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> entry : list.entrySet()) {
            Chunkdata.ChunkLogCoords chunk = entry.getKey();
            if (chunk == null) continue;
            Chunkdata.ChunkLogEvent event = list.get(chunk);
            if (event == null) {
                continue;
            }
            if (chunk.space.x == (cx + minX) && chunk.space.z == (cz + minZ)) {
                System.out.println("Event: " + event.event.toString() + "\nStackTrace:\n" + ZeroXstuff.data.getStackTraceString(event.stackTraceId));
            }
        }
    }

    // retard color system
    final int cunloaded = 0xffc8c8c8;
    final int cplayerloaded = 0xff3232c8;
    final int cloaded = 0xff32c832;
    final int cunloadqueued = 0xffc8c832;
    final int cunloadqueueing = 0xffffff00;
    final int cunloading = 0xffff0000;
    final int cunloadingcanceled = 0xff0000ff;
    final int cloading = 0xff00ff00;

    int getColor(Chunkdata.Event event) {
        int color = 0xffffff;
        switch (event) {
            case MISSED_EVENT_ERROR:
                color = cunloaded;
                break;
            case UNLOADING:
                color = cunloaded;
                break;
            case LOADING:
                color = cloaded;
                break;
            case PLAYER_ENTERS:
                color = cunloadqueued;
                break;
            case PLAYER_LEAVES:
                color = cunloadqueueing;
                break;
            case QUEUE_UNLOAD:
                color = cunloading;
                break;
            case CANCEL_UNLOAD:
                color = cunloadingcanceled;
                break;
            case UNQUEUE_UNLOAD:
                color = cloading;
                break;
            case GENERATING:
                color = cloading;
                break;
            case POPULATING:
                color = cloading;
                break;
            case GENERATING_STRUCTURES:
                color = cloading;
                break;
        }

        return color;
    }

    public void scroll(int scrollAmount) {
        ChunkGrid canvas = debug.getChunkGrid();
        canvas.setScale(debug.width, debug.height, scrollAmount);
        setTick(lastGametick);
    }
}
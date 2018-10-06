package carpetclient.gui;

import carpetclient.coders.zerox53ee71ebe11e.ChunkLogData;
import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import carpetclient.coders.zerox53ee71ebe11e.ZeroXstuff;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.SortedMap;

import static carpetclient.coders.zerox53ee71ebe11e.Chunkdata.Event.*;

public class Controller {
    DebugWindow debug;
    boolean start = false;
    private boolean live = false;
    private int lastGametick;
    private int viewX;
    private int viewZ;
    private Chunkdata.MapView chunkData;

    public Controller(DebugWindow d) {
        debug = d;
        lastGametick = 0;
        chunkData = ZeroXstuff.data.getChunkData();
    }

    public boolean startStop() {
        start = !start;

        debug.disableSaveLoadButtons(start);

        live = true;
        if (start) {
            home();
        } else {
            ZeroXstuff.data.clear();
        }
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.CHUNK_LOGGER);
        sender.writeBoolean(start); // this is the bit to turn on or off
        sender.writeBoolean(debug.getStackTrace()); // this is the bit to send stacktraces

        CarpetPluginChannel.packatSender(sender);

        return start;
    }

    public void load() {
//        JFileChooser fc = new JFileChooser();
//        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        int rval = fc.showOpenDialog(frame);
//        if(rval == JFileChooser.APPROVE_OPTION) {
//            String path = fc.getSelectedFile().getPath();
//            dirTextField.setText(path);
//            ChunkWatch.this.updateFileList(path);
//        }

        //TODO: Load stuff
    }

    public void save() {
        //TODO: Save stuff
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

        debug.setXTest(Integer.toString(viewX));
        debug.setZText(Integer.toString(viewZ));

        //TODO: fix dimention swap for player home

        setMapViewData();
        setTick(lastGametick);
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
        Chunkgrid canvas = debug.getCanvas();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int minX = viewX - sizeX / 2;
        int maxX = viewX + sizeX / 2;
        int minZ = viewZ - sizeZ / 2;
        int maxZ = viewZ + sizeZ / 2;

        int dimention = debug.getComboBoxValue();

        chunkData.seekSpace(dimention, minX, maxX, minZ, maxZ);
    }

    void setTick(int gametick) {
        int dimention = debug.getComboBoxValue();
        Chunkgrid canvas = debug.getCanvas();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int minX = viewX - sizeX / 2;
        int maxX = viewX + sizeX / 2;
        int minZ = viewZ - sizeZ / 2;
        int maxZ = viewZ + sizeZ / 2;

        canvas.clearColors();

        chunkData.seekTime(gametick);

        SortedMap<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> list = chunkData.getDisplayArea();

//        SortedMap<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> list = ZeroXstuff.data.getAllLogsForDisplayArea(gametick, dimention, minX, maxX, minZ, maxZ);
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

        debug.setTimeTextField(Integer.toString(gametick));

        canvas.invalidate();
        canvas.repaint();
    }

    private int getOffsetX(int x, Chunkgrid canvas) {
        return x + viewX - canvas.sizeX() / 2;
    }

    private int getOffsetZ(int z, Chunkgrid canvas) {
        return z + viewZ - canvas.sizeZ() / 2;
    }

    public void selectchunk(int x, int y) {
        Chunkgrid canvas = debug.getCanvas();
        int cx = canvas.getGridX(x);
        int cz = canvas.getGridY(y);
        System.out.println("Selected: " + cx + " " + cz);
//        canvas.showSelection(cx, cz);
//        setTick(lastGametick);
    }

    // retard color system
    final Color cunloaded = new Color(200, 200, 200);
    final Color cplayerloaded = new Color(50, 50, 200);
    final Color cloaded = new Color(50, 200, 50);
    final Color cunloadqueued = new Color(200, 200, 50);
    final Color cunloadqueueing = new Color(255, 255, 0);
    final Color cunloading = new Color(255, 0, 0);
    final Color cunloadingcanceled = new Color(0, 0, 255);
    final Color cloading = new Color(0, 255, 0);

    Color getColor(Chunkdata.Event event) {
        Color color = new Color(255, 255, 255);
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
        Chunkgrid canvas = debug.getCanvas();
        canvas.setScale(scrollAmount);
        setTick(lastGametick);
    }
}

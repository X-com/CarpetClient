package carpetclient.gui;

import carpetclient.coders.zerox53ee71ebe11e.ChunkLogData;
import carpetclient.coders.zerox53ee71ebe11e.ZeroXstuff;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import javax.swing.*;
import java.awt.event.KeyEvent;

public class Controller {
    DebugWindow debug;
    boolean start = false;
    private boolean live = false;
    private int lastIndex;
    private int viewX;
    private int viewZ;

    public Controller(DebugWindow d) {
        debug = d;
        lastIndex = 0;
    }

    public boolean startStop() {
        start = !start;

        debug.disableSaveLoadButtons(start);

        live = true;
        if (start) {
            home();
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
        setTick(lastIndex - 1);
    }

    public void forward() {
        live = false;
        setTick(lastIndex + 1);
    }

    public void current() {
        live = true;
        setTick(ZeroXstuff.data.latestIndex());
    }

    public void comboBoxAction() {
        setTick(lastIndex);
    }

    public void setTime(KeyEvent e) {
    }

    public void home() {
        BlockPos pos = Minecraft.getMinecraft().player.getPosition();
        viewX = pos.getX() >> 4;
        viewZ = pos.getZ() >> 4;

        debug.setXTest(Integer.toString(viewX));
        debug.setZText(Integer.toString(viewZ));

        //TODO: fix dimention selection

        setTick(lastIndex);
    }

    public void setX(KeyEvent e, JTextArea textX) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        viewX = integerInputs(e, textX, viewX);
        textX.setText(Integer.toString(viewX));
        setTick(lastIndex);
    }

    public void setZ(KeyEvent e, JTextArea textZ) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        viewZ = integerInputs(e, textZ, viewZ);
        textZ.setText(Integer.toString(viewZ));
        setTick(lastIndex);
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

    public void liveUpdate(int index) {
        if (!live) return;
        setTick(index);
    }

    void setTick(int index) {
        ChunkLogData.TimeIndex timeIndex = ZeroXstuff.data.timeIndexByIndex(index);
        if (timeIndex == null) return;
        lastIndex = index;
        int dimention = debug.getComboBoxValue();

        int time = timeIndex.getTime();
        debug.setTimeTextField("Index: " + Integer.toString(index) + " Time: " + Integer.toString(time));
        Chunkgrid canvas = debug.getCanvas();

        for (int z = 0; z < canvas.sizeZ(); z++) {
            for (int x = 0; x < canvas.sizeX(); x++) {
                int event = timeIndex.getChunkEvent(getOffsetX(x, canvas), getOffsetZ(z, canvas), dimention);
                canvas.setGridColor(x, z, getColor(event));
            }
        }
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
        canvas.showSelection(cx, cz);
        setTick(lastIndex);
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

    Color getColor(int event) {
        Color color = new Color(255, 255, 255);
        switch (event) {
            case 0:
                color = cunloaded;
                break;
            case 1:
                color = cplayerloaded;
                break;
            case 2:
                color = cloaded;
                break;
            case 3:
                color = cunloadqueued;
                break;
            case 4:
                color = cunloadqueueing;
                break;
            case 5:
                color = cunloading;
                break;
            case 6:
                color = cunloadingcanceled;
                break;
            case 7:
                color = cloading;
                break;
        }

        return color;
    }

    public void scroll(int scrollAmount) {
        Chunkgrid canvas = debug.getCanvas();
        canvas.setScale(scrollAmount);
        setTick(lastIndex);
    }
}

package carpetclient.gui;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import carpetclient.coders.zerox53ee71ebe11e.ZeroXstuff;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Map;
import java.util.SortedMap;

public class Controller {
    GuiChunkGrid debug;
    boolean start = false;
    private boolean live = false;
    private int lastGametick;
    private Point view = new Point();
    private Point dragView = new Point();
    private Point selectionBox;
    private int selectionDimention;

    private Chunkdata.MapView chunkData;
    private Point mouseDown = new Point();
    private boolean panning = false;


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
            selectionBox = null;
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
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
                ZeroXstuff.data.readObject(in);
                view.x = in.readInt();
                view.y = in.readInt();
                in.close();
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
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
                ZeroXstuff.data.writeObject(out);
                out.writeInt(view.x);
                out.writeInt(view.y);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void back() {
        live = false;
        if (selectionBox != null) {
            setTick(ZeroXstuff.data.getPreviousGametickForChunk(lastGametick, selectionBox.x, selectionBox.y, selectionDimention));
        } else {
            setTick(ZeroXstuff.data.getPrevGametick(lastGametick));
        }
    }

    public void forward() {
        live = false;
        if (selectionBox != null) {
            setTick(ZeroXstuff.data.getNextGametickForChunk(lastGametick, selectionBox.x, selectionBox.y, selectionDimention));
        } else {
            setTick(ZeroXstuff.data.getNextGametick(lastGametick));
        }
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
        view.x = pos.getX() >> 4;
        view.y = pos.getZ() >> 4;

        debug.setXText(view.x);
        debug.setZText(view.y);

        changeDimentionTo(Minecraft.getMinecraft().player.dimension);

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
        view.x = integerInputs(e, textX, view.x);
        textX.setText(Integer.toString(view.x));
        setTick(lastGametick);
    }

    public void setZ(KeyEvent e, JTextArea textZ) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
        view.y = integerInputs(e, textZ, view.y);
        textZ.setText(Integer.toString(view.y));
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

    void setTick(int gametick) {
        int dimention = debug.getSelectedDimension();
        ChunkGrid canvas = debug.getChunkGrid();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int minX = view.x - sizeX / 2;
        int maxX = view.x + sizeX / 2;
        int minZ = view.y - sizeZ / 2;
        int maxZ = view.y + sizeZ / 2;

        chunkData.seekSpace(dimention, minX, maxX + 2, minZ, maxZ + 2);
        chunkData.seekTime(gametick);

        canvas.clearColors();

        for (Chunkdata.ChunkView cv : chunkData) {
            int color = 0;
            for (int c : cv.getColors()) {
                color = c;
            }

            canvas.setGridColor(cv.getX() - minX, cv.getZ() - minZ, color);
        }

        if (selectionBox != null && selectionDimention == dimention) {
            debug.getChunkGrid().setSelectionBox(selectionBox.x - minX, selectionBox.y - minZ);
        } else {
            debug.getChunkGrid().setSelectionBox(Integer.MAX_VALUE, 0);
        }

        debug.setTime(gametick);

        lastGametick = gametick;
    }

    public void buttonDown(int x, int y, int button) {
        ChunkGrid canvas = debug.getChunkGrid();
        int dimention = debug.getSelectedDimension();
        int sizeX = canvas.sizeX();
        int sizeZ = canvas.sizeZ();

        int cx = canvas.getGridX(x);
        int cz = canvas.getGridY(y);

        int minX = view.x - sizeX / 2;
        int maxX = view.x + sizeX / 2;
        int minZ = view.y - sizeZ / 2;
        int maxZ = view.y + sizeZ / 2;

        if (button == 0) {
            mouseDown.setLocation(x, y);
            dragView.setLocation(view);
        } else if (button == 1) {
            SortedMap<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> list = ZeroXstuff.data.getAllLogsForDisplayArea(lastGametick, dimention, minX, maxX, minZ, maxZ);
            for (Map.Entry<Chunkdata.ChunkLogCoords, Chunkdata.ChunkLogEvent> entry : list.entrySet()) {
                Chunkdata.ChunkLogCoords chunk = entry.getKey();
                if (chunk == null) continue;
                Chunkdata.ChunkLogEvent event = list.get(chunk);
                if (event == null) {
                    continue;
                }
                if (chunk.space.x == (cx + minX) && chunk.space.z == (cz + minZ)) {
                    System.out.println("Event: " + event.event.toString());
                    String s = ZeroXstuff.data.getStackTraceString(event.stackTraceId);
                    if (s.length() != 0)
                        System.out.println("StackTrace:\n" + ZeroXstuff.data.getStackTraceString(event.stackTraceId));
                }
            }
        }
    }

    public void buttonUp(int x, int y, int mouseButton) {
        if (mouseButton == 0 && !panning) {
            int cx = debug.getChunkGrid().getGridX(x) + view.x - debug.getChunkGrid().sizeX() / 2;
            int cz = debug.getChunkGrid().getGridY(y) + view.y - debug.getChunkGrid().sizeZ() / 2;

            if (selectionBox != null && selectionBox.x == cx && selectionBox.y == cz) {
                selectionBox = null;
                debug.setBackButtonText("Back");
                debug.setForwardButtonText("Forward");
            } else {
                selectionBox = new Point(cx, cz);
                selectionDimention = debug.getSelectedDimension();
                debug.setBackButtonText("Back(chunk)");
                debug.setForwardButtonText("Forward(chunk)");
            }

            setTick(lastGametick);
        }
        panning = false;
    }

    public void mouseDrag(int x, int y, int button) {
        if (!panning && mouseDown.distance(x, y) > 5) {
            panning = true;
        } else if (button == 0 && panning) {
            int dx = x - mouseDown.x;
            int dy = y - mouseDown.y;
            view.setLocation(dragView.x - dx, dragView.y - dy);
            debug.setXText(view.x);
            debug.setZText(view.y);
            setTick(lastGametick);
        }
    }

    public void scroll(int scrollAmount) {
        ChunkGrid canvas = debug.getChunkGrid();
        canvas.setScale(canvas.width(), canvas.height(), scrollAmount);
        setTick(lastGametick);
    }
}
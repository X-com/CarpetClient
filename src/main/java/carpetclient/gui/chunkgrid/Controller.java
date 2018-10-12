package carpetclient.gui.chunkgrid;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
import carpetclient.coders.zerox53ee71ebe11e.ZeroXstuff;
import carpetclient.pluginchannel.CarpetPluginChannel;
import com.google.common.base.Splitter;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.Point;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    private GuiChunkGrid debug;
    boolean start = false;
    boolean play = false;
    private boolean live = false;
    private int lastGametick;
    private Point view = new Point();
    private Point dragView = new Point();
    private Point selectionBox;
    private int selectionDimention;
    private Chunkdata chunkData;
    private Chunkdata.MapView mapView;
    private Chunkdata.MapView mapViewMinimap;

    //    private Chunkdata.MapView chunkData;
    private Point mouseDown = new Point();
    private boolean panning = false;

    public Controller(GuiChunkGrid d) {
        debug = d;
        lastGametick = 0;
        chunkData = ZeroXstuff.data;
        mapView = chunkData.getChunkData();
        mapViewMinimap = chunkData.getChunkData();
    }

    public boolean startStop() {
        start = !start;

        live = true;
        if (start) {
            home();
            chunkData.clear();
            mapView = chunkData.getChunkData();
            mapViewMinimap = chunkData.getChunkData();
            selectionBox = null;
            play = false;
        }
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.CHUNK_LOGGER);
        sender.writeBoolean(start); // this is the bit to turn on or off
//        sender.writeBoolean(debug.areStackTracesEnabled()); // this is the bit to send stacktraces
        sender.writeBoolean(true);

        CarpetPluginChannel.packatSender(sender);

        return start;
    }

    public void setStart(boolean s) {
        start = s;
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
                chunkData = (Chunkdata) in.readObject();
                mapView = chunkData.getChunkData();
                mapViewMinimap = chunkData.getChunkData();
                view.setX(in.readInt());
                view.setY(in.readInt());
                debug.setXText(view.getX());
                debug.setZText(view.getY());
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        setTick(chunkData.getFirstGametick());
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
                out.writeObject(chunkData);
                out.writeInt(view.getX());
                out.writeInt(view.getY());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void back() {
        live = false;
        if (selectionBox != null) {
            setTick(chunkData.getPreviousGametickForChunk(lastGametick, selectionBox.getX(), selectionBox.getY(), selectionDimention));
        } else {
            setTick(chunkData.getPrevGametick(lastGametick));
        }
    }

    public void forward() {
        live = false;
        if (selectionBox != null) {
            setTick(chunkData.getNextGametickForChunk(lastGametick, selectionBox.getX(), selectionBox.getY(), selectionDimention));
        } else {
            setTick(chunkData.getNextGametick(lastGametick));
        }
    }

    public void current() {
        live = true;
        setTick(chunkData.getLastGametick());
    }

    public void comboBoxAction() {
        setTick(lastGametick);
    }

    public void begining() {
        setTick(chunkData.getFirstGametick());
    }

    public void end() {
        setTick(chunkData.getLastGametick());
    }

    public void play() {
        play = !play;

        if (play) {
            new Timer().start();
        }
    }

    public void home() {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().player == null) return;
        BlockPos pos = Minecraft.getMinecraft().player.getPosition();
        view.setX(pos.getX() >> 4);
        view.setY(pos.getZ() >> 4);

        debug.setXText(view.getX());
        debug.setZText(view.getY());

        int dimention = minecraftDimentionToIndex(Minecraft.getMinecraft().player.dimension);
        debug.setSelectedDimension(dimention);

        setTick(lastGametick);
    }

    private int minecraftDimentionToIndex(int dimension) {
        if (dimension == -1) {
            return 1;
        } else if (dimension == 1) {
            return 2;
        }
        return 0;
    }

    public void setTime(String text) {
        try {
            int gt = Integer.parseInt(text);
            int first = chunkData.getFirstGametick();
            int last = chunkData.getLastGametick();
            if (gt < first) {
                gt = first;
            } else if (gt > last) {
                gt = last;
            }
            setTick(gt);
        } catch (NumberFormatException e) {
            return;
        }
    }

    public void setX(String text) {
        try {
            int x = Integer.parseInt(text);
            view.setX(x);
        } catch (NumberFormatException e) {
            return;
        }
        setTick(lastGametick);
    }

    public void setZ(String text) {
        try {
            int z = Integer.parseInt(text);
            view.setY(z);
        } catch (NumberFormatException e) {
            return;
        }
        setTick(lastGametick);
    }

    public void liveUpdate(int time) {
        if (debug.isMinimapVisible() && !debug.isChunkDebugWindowOpen()) {
            setMinimap(time);
        }
        if (live && debug.isChunkDebugWindowOpen()) {
            setTick(time);
        }
    }

    public void updateGUI() {
        if (debug.isChunkDebugWindowOpen()) {
            setTick(lastGametick);
        } else if (debug.isMinimapVisible()) {
            setMinimap(lastGametick);
        }
    }

    public void initMinimap() {
        setMinimap(lastGametick);
    }

    private void setMinimap(int time) {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().player == null) return;
        BlockPos pos = Minecraft.getMinecraft().player.getPosition();
        ChunkGrid canvas = debug.getChunkGrid();
        int playerX = pos.getX() >> 4;
        int playerY = pos.getZ() >> 4;

        int dimention = minecraftDimentionToIndex(Minecraft.getMinecraft().player.dimension);

        int sizeX = canvas.size(debug.getMinimapWidth());
        int sizeZ = canvas.size(debug.getMinimapHeight());

        int minX = playerX - sizeX / 2;
        int maxX = playerX + sizeX / 2;
        int minZ = playerY - sizeZ / 2;
        int maxZ = playerY + sizeZ / 2;

        mapViewMinimap.seekSpace(dimention, minX, maxX, minZ, maxZ);
        mapViewMinimap.seekTime(time);
        canvas.setView(mapViewMinimap);

        if (selectionBox != null && selectionDimention == dimention) {
            debug.getChunkGrid().setSelectionBox(selectionBox.getX() - minX, selectionBox.getY() - minZ);
        } else {
            debug.getChunkGrid().setSelectionBox(Integer.MAX_VALUE, 0);
        }

        lastGametick = time;
    }

    private void setTick(int gametick) {
        int dimention = debug.getSelectedDimension();
        ChunkGrid canvas = debug.getChunkGrid();
        int sizeX = canvas.size(debug.windowWidth());
        int sizeZ = canvas.size(debug.windowHeight());

        int minX = view.getX() - sizeX / 2;
        int maxX = view.getX() + sizeX / 2;
        int minZ = view.getY() - sizeZ / 2;
        int maxZ = view.getY() + sizeZ / 2;

        mapView.seekSpace(dimention, minX, maxX + 2, minZ, maxZ + 2);
        mapView.seekTime(gametick);
        canvas.setView(mapView);

        if (selectionBox != null && selectionDimention == dimention) {
            debug.getChunkGrid().setSelectionBox(selectionBox.getX() - minX, selectionBox.getY() - minZ);
            debug.setBackButtonText("Back*");
            debug.setForwardButtonText("Forward*");
            debug.selectedChunk(true, selectionBox.getX(), selectionBox.getY());
        } else {
            debug.getChunkGrid().setSelectionBox(Integer.MAX_VALUE, 0);
            debug.setBackButtonText("Back");
            debug.setForwardButtonText("Forward");
            debug.selectedChunk(false, 0, 0);
        }

        debug.setXText(view.getX());
        debug.setZText(view.getY());

        debug.setTime(gametick);

        lastGametick = gametick;
    }

    public void buttonDown(int x, int y, int button) {
        if (button == 0) {
            mouseDown.setLocation(x, y);
            dragView.setLocation(view);
        } else if (button == 1) {
            int cx = debug.getChunkGrid().getGridX(x) + view.getX() - debug.getChunkGrid().sizeX() / 2;
            int cz = debug.getChunkGrid().getGridY(y) + view.getY() - debug.getChunkGrid().sizeZ() / 2;
            List<String> props = new ArrayList<>();
            List<String> stacktrace = new ArrayList<>();

            Chunkdata.ChunkView chunk = mapView.pickChunk(cx, cz);
            if (chunk != null) {
                for (Chunkdata.EventView ev : chunk) {
                    String eventString = "Event: " + ev.getType().toString() + " Order: " + ev.getOrder() + " GT: " + ev.getGametick();
                    props.add(eventString);
                    String s = ev.getStacktrace();
                    if (s != null && s.length() != 0) {
                        stacktrace.add("");
                        stacktrace.add(eventString);
                        stacktrace.addAll(Splitter.onPattern("\\r?\\n").splitToList(s));
                    }
                }
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiChunkGridChunk(cx, cz, debug, debug, props, stacktrace.size() != 0 ? stacktrace : null));
        }
    }

    public void buttonUp(int x, int y, int mouseButton) {
        if (mouseButton == 0 && !panning) {
            int cx = debug.getChunkGrid().getGridX(x) + view.getX() - debug.getChunkGrid().sizeX() / 2;
            int cz = debug.getChunkGrid().getGridY(y) + view.getY() - debug.getChunkGrid().sizeZ() / 2;

            if (selectionBox != null && selectionBox.getX() == cx && selectionBox.getY() == cz) {
                selectionBox = null;
            } else {
                selectionBox = new Point(cx, cz);
                selectionDimention = debug.getSelectedDimension();
            }

            setTick(lastGametick);
        }
        panning = false;
    }

    public void mouseDrag(int x, int y, int button) {
        int dx = x - mouseDown.getX();
        int dy = y - mouseDown.getY();
        if (!panning && dx * dx + dy * dy > 5 * 5) {
            panning = true;
        } else if (button == 0 && panning) {
            int dragX = 0;
            int dragY = 0;
            if (GuiScreen.isCtrlKeyDown()) {
                dragX = dragView.getX() - dx;
                dragY = dragView.getY() - dy;
            } else {
                dragX = dragView.getX() - debug.getChunkGrid().getGridY(dx);
                dragY = dragView.getY() - debug.getChunkGrid().getGridY(dy);
            }
            view.setLocation(dragX, dragY);
            setTick(lastGametick);
        }
    }

    public void scroll(int scrollAmount) {
        ChunkGrid canvas = debug.getChunkGrid();
        canvas.setScale(canvas.width(), canvas.height(), scrollAmount);
        setTick(lastGametick);
    }

    private class Timer extends Thread {
        public void run() {
            int last = ZeroXstuff.data.getLastGametick();
            while (play) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int next = lastGametick + 1;
                if (next >= last) break;
                setTick(next);
            }
            play = false;
            debug.setPlayButtonText("Play");
        }
    }
}
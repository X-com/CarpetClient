package carpetclient.gui.chunkgrid;

import carpetclient.coders.zerox53ee71ebe11e.Chunkdata;
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

/**
 * Controller class for the chunk debug tool in carpet client. Controls all logic for GUI options.
 */
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

    private Point mouseDown = new Point();
    private boolean panning = false;

    /**
     * Main constroctor of the class.
     *
     * @param d Chunk Grid GUI for drawing the window after entering it with default F6 hotkey.
     */
    public Controller(GuiChunkGrid d) {
        debug = d;
        lastGametick = 0;
        chunkData = Chunkdata.restartRecording();
        mapView = chunkData.getChunkData();
        mapViewMinimap = chunkData.getChunkData();
    }

    /**
     * Start stop logic for starting a recording from the server.
     *
     * @return returns true if retording is started.
     */
    public boolean startStop() {
        start = !start;

        live = true;
        if (start) {
            home();
            chunkData = Chunkdata.restartRecording();
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

    /**
     * Sets the start or stop.
     *
     * @param s boolean setter for setting start.
     */
    public void setStart(boolean s) {
        start = s;
    }

    /**
     * Loading a saved option from disk. Opens a GUI window to navigate too.
     */
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

    /**
     * Saves a current recording to disk. Opens a window to navigate to for saving.
     */
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

    /**
     * For going backward in time in the events captured from the server.
     */
    public void back() {
        live = false;
        if (selectionBox != null) {
            setTick(chunkData.getPreviousGametickForChunk(lastGametick, selectionBox.getX(), selectionBox.getY(), selectionDimention));
        } else {
            setTick(chunkData.getPrevGametick(lastGametick));
        }
    }

    /**
     * For going forward in time in the events captured from the server.
     */
    public void forward() {
        live = false;
        if (selectionBox != null) {
            setTick(chunkData.getNextGametickForChunk(lastGametick, selectionBox.getX(), selectionBox.getY(), selectionDimention));
        } else {
            setTick(chunkData.getNextGametick(lastGametick));
        }
    }

    /**
     * Sets the display to the most recent events captured from the server.
     */
    public void current() {
        live = true;
        setTick(chunkData.getLastGametick());
    }

    /**
     * Used after toggling the dimention button for showing different dimentions.
     */
    public void dimentionUpdate() {
        setTick(lastGametick);
    }

    /**
     * For going to the begining in time in the events captured from the server.
     */
    public void begining() {
        setTick(chunkData.getFirstGametick());
    }

    /**
     * For going to the end in time in the events captured from the server.
     */
    public void end() {
        setTick(chunkData.getLastGametick());
    }

    /**
     * Play button used to enable or disable a gametick display of the events captured from the server.
     */
    public void play() {
        play = !play;

        if (play) {
            new Timer().start();
        }
    }

    /**
     * Home button to home back to the player location in the screen.
     */
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

    /**
     * Converts minecraft dimention index to index used for dimentions in orders as follows.
     * Overworld - 0
     * Nether - 1
     * End - 2
     *
     * @param dimension Minecraft dimetnion index.
     * @return index of dimention listed above.
     */
    private int minecraftDimentionToIndex(int dimension) {
        if (dimension == -1) {
            return 1;
        } else if (dimension == 1) {
            return 2;
        }
        return 0;
    }

    /**
     * Sets the time on the textbox displaying gameticks.
     *
     * @param text String representation of the gametick.
     */
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

    /**
     * Sets the X value the view is at.
     *
     * @param text String representation of the X value the view is at.
     */
    public void setX(String text) {
        try {
            int x = Integer.parseInt(text);
            view.setX(x);
        } catch (NumberFormatException e) {
            return;
        }
        setTick(lastGametick);
    }

    /**
     * Sets the Z value the view is at.
     *
     * @param text String representation of the Z value the view is at.
     */
    public void setZ(String text) {
        try {
            int z = Integer.parseInt(text);
            view.setY(z);
        } catch (NumberFormatException e) {
            return;
        }
        setTick(lastGametick);
    }

    /**
     * Method used by the server to update when live updates is set and data is being recieved.
     */
    public void liveUpdate() {
        int time = chunkData.getLastGametick();
        if (debug.getMinimapType() != 0 && !debug.isChunkDebugWindowOpen()) {
            setMinimap(time);
        }
        if (live && debug.isChunkDebugWindowOpen()) {
            setTick(time);
        }
    }

    /**
     * Updates the draw calculations with the last game tick.
     */
    public void updateGUI() {
        if (debug.isChunkDebugWindowOpen()) {
            setTick(lastGametick);
        } else if (debug.getMinimapType() != 0) {
            setMinimap(lastGametick);
        }
    }

    /**
     * Update minimap calculations with the last game tick.
     */
    public void initMinimap() {
        setMinimap(lastGametick);
    }

    /**
     * Sets the minimap to the specific game tick.
     *
     * @param time gametick in integer.
     */
    private void setMinimap(int time) {
        ChunkGrid canvas = debug.getChunkGrid();
        int x = 0;
        int y = 0;
        int playerX = 0;
        int playerY = 0;
        int dimention = 0;
        boolean playerDrawn = false;

        if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().player != null) {
            BlockPos pos = Minecraft.getMinecraft().player.getPosition();
            playerX = x = pos.getX() >> 4;
            playerY = y = pos.getZ() >> 4;
            playerDrawn = true;
        }

        if (debug.getMinimapType() == 1) {
            dimention = minecraftDimentionToIndex(Minecraft.getMinecraft().player.dimension);
        } else if (debug.getMinimapType() == 2) {
            x = view.getX();
            y = view.getY();
            dimention = debug.getSelectedDimension();
        } else {
            return;
        }

        int sizeX = canvas.size(debug.getMinimapWidth());
        int sizeZ = canvas.size(debug.getMinimapHeight());

        int minX = x - sizeX / 2;
        int maxX = x + sizeX / 2;
        int minZ = y - sizeZ / 2;
        int maxZ = y + sizeZ / 2;

        mapViewMinimap.seekSpace(dimention, minX, maxX, minZ, maxZ);
        mapViewMinimap.seekTime(time);

        if (playerDrawn) {
            debug.getChunkGrid().playerChunk(playerX - minX, playerY - minZ);
        } else {
            debug.getChunkGrid().playerChunk(Integer.MAX_VALUE, 0);
        }

        if (selectionBox != null && selectionDimention == dimention) {
            debug.getChunkGrid().setSelectionBox(selectionBox.getX() - minX, selectionBox.getY() - minZ);
        } else {
            debug.getChunkGrid().setSelectionBox(Integer.MAX_VALUE, 0);
        }

        lastGametick = time;
    }

    /**
     * Sets the main window to the specific game tick.
     *
     * @param gametick gametick in integer.
     */
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

        debug.getChunkGrid().playerChunk(Integer.MAX_VALUE, 0);

        debug.setXText(view.getX());
        debug.setZText(view.getY());

        debug.setTime(gametick);

        lastGametick = gametick;
    }

    /**
     * Button logic for pressing down.
     *
     * @param x      X pixel the mouse is pressing down at.
     * @param y      Y pixel the mouse is pressing down at.
     * @param button button type the mouse is pressing down with.
     */
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
                String lastChunkString = getLastChunkState(chunk);
                props.add(lastChunkString);
                props.add("");
                for (Chunkdata.EventView ev : chunk) {
                    String eventString = "Event: " + ev.getType().toString() + " Order: " + ev.getOrder() + " GT: " + ev.getGametick();
                    props.add(eventString);
                    String stacktracestring = ev.getStacktrace();
                    if (stacktracestring != null && stacktracestring.length() != 0) {
                        stacktrace.add("");
                        stacktrace.add(eventString);
                        stacktrace.addAll(Splitter.onPattern("\\r?\\n").splitToList(stacktracestring));
                    }
                }
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiChunkGridChunk(cx, cz, debug, debug, props, stacktrace.size() != 0 ? stacktrace : null));
        }
    }

    /**
     * Gets the String descriptor of the last event in the chunk.
     *
     * @param chunk Chunk that is being used to get its last event descriptor.
     * @return the string descriptor for the chunks status.
     */
    private String getLastChunkState(Chunkdata.ChunkView chunk) {
        String s = "";
        int tag = 0;
        if (chunk.wasPlayerLoaded()) {
            tag++;
            s += "Player-Loaded";
        }
        if (chunk.wasLoaded()) {
            if (tag > 0) s += " : ";
            tag++;
            s += "Loaded";
        } else if (chunk.wasLoadedInThePast()) {
            if (tag > 0) s += " : ";
            tag++;
            s += "Recently Unloaded";
        } else {
            if (tag > 0) s += " : ";
            tag++;
            s += "Unloaded";
        }
        if (chunk.wasUnloadQueued()) {
            if (tag > 0) s += " : ";
            s += "Unloading Queued";
        }

        return s;
    }

    /**
     * Button logic for releasing a button.
     *
     * @param x           X pixel the mouse is releasing at.
     * @param y           Y pixel the mouse is releasing at.
     * @param mouseButton button type the mouse is releasing with.
     */
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

    /**
     * Mouse draw logic.
     *
     * @param x      X pixel the mouse is at.
     * @param y      Y pixel the mouse is at.
     * @param button button type the mouse is draging with.
     */
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

    /**
     * Scroll logic for when mouse is scrolling.
     *
     * @param scrollAmount scroll amount by the mouse.
     */
    public void scroll(int scrollAmount) {
        ChunkGrid canvas = debug.getChunkGrid();
        canvas.setScale(canvas.width(), canvas.height(), scrollAmount);
        setTick(lastGametick);
    }

    /**
     * Map data that is used to draw squares on the debug chunk window.
     *
     * @return
     */
    public Chunkdata.MapView getView() {
        return this.mapView;
    }

    /**
     * Minimap data that is used for drawing to the minimap.
     *
     * @return
     */
    public Chunkdata.MapView getMinimapView() {
        return this.mapViewMinimap;
    }

    /**
     * Thread class used to update the ingame tick by tick representation of the events..
     */
    private class Timer extends Thread {
        public void run() {
            int last = chunkData.getLastGametick();
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
package carpetclient.coders.zerox53ee71ebe11e;

import carpetclient.gui.chunkgrid.GuiChunkGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.io.IOException;
import java.util.Map.Entry;

/**
 * Class made by 0x53ee71ebe11e for the Chunk Debug tool. Don't ask me how it works.
 * Contact 0x53ee71ebe11e for use of this class.
 */
public class Chunkdata implements Serializable {

    public static enum Event {
        NONE,
        UNLOADING,
        LOADING,
        PLAYER_ENTERS,
        PLAYER_LEAVES,
        QUEUE_UNLOAD,
        CANCEL_UNLOAD,
        GENERATING,
        POPULATING,
        GENERATING_STRUCTURES;

        public boolean isPlayerEvent() {
            switch (this) {
                case PLAYER_ENTERS:
                case PLAYER_LEAVES:
                    return true;
                default:
                    return false;
            }
        }

        public int categorizeEvent() {
            if (this.isPlayerEvent()) {
                return 1;
            } else {
                return 0;
            }
        }

        static final int eventColors[] = {
                0xff000000,//MISSED_EVENT_ERROR
                0xffff0000,//UNLOADING
                0xff00ffff,//LOADING
                0xff00D200,//PLAYER_ENTERS
                0xffff8028,//PLAYER_LEAVES
                0xffdddd00,//QUEUE_UNLOAD
                0xff0000ff,//CANCEL_UNLOAD
                0xff804000,//GENERATING
                0xffdd00dd,//POPULATING
                0xff770077//GENERATING_STRUCTURES
        };

        public int getColor() {
            return eventColors[this.ordinal()];
        }
    }

    private static final int STATE_WAS_UNLOAD_QUEUED = 0xff999900;
    private static final int STATE_WAS_PLAYER_LOADED = 0xff007000;
    private static final int STATE_PLAYER_REQUESTED = 0xff002200;
    private static final int STATE_LOADED = 0xff2C6798;
    private static final int STATE_PAST_LOADED = 0xff222222;

    private static final int categoryCount = 2;

    public MapView getChunkData() {
        return new MapView();
    }

    public class EventView {

        FullEvent event;

        EventView(FullEvent e) {
            this.event = e;
        }

        public int getGametick() {
            return event.t;
        }

        public int getOrder() {
            return event.o;
        }

        public Event getType() {
            return event.e;
        }

        public String getStacktrace() {
            return allStacktraces.get(event.s);
        }
    }

    public class ChunkView implements Iterable<EventView> {

        FullEvent oldEvents[] = null;
        FullEvent currentEvents[] = null;
        int colors[];
        int x = 0;
        int z = 0;
        int d = -1;

        private void updateColors() {
            int length = 1;
            if (currentEvents != null) {
                length += currentEvents.length;
            }
            if ((colors == null) || (colors.length != length)) {
                colors = new int[length];
            }
            if (currentEvents != null) {
                for (int i = 0; i < currentEvents.length; ++i) {
                    colors[i + 1] = currentEvents[i].e.getColor();
                }
            }
            if (wasLoaded()) {
                if (wasUnloadQueued()) {
                    colors[0] = STATE_WAS_UNLOAD_QUEUED;
                } else if (wasPlayerLoaded()) {
                    colors[0] = STATE_WAS_PLAYER_LOADED;
                } else {
                    colors[0] = STATE_LOADED;
                }
            }
            else if(wasPlayerLoaded()) {
                colors[0] = STATE_PLAYER_REQUESTED;
            }
            else if (wasLoadedInThePast()) {
                colors[0] = STATE_PAST_LOADED;
            } else {
                colors[0] = GuiChunkGrid.style.getBackgroundColor();
            }
        }

        private void checkEvents(FullEvent events[], boolean allownull) {
            if (events != null) {
                for (FullEvent e : events) {
                    if ((e != null) && ((e.x != x) || (e.z != z) || (e.d != d))) {
                        throw new IllegalArgumentException();
                    } else if (!allownull && (e == null)) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }

        private void reset(int x, int z, int d, FullEvent oldEvents[], FullEvent currentEvents[]) {
            this.x = x;
            this.z = z;
            this.d = d;
            if (oldEvents == null) throw new IllegalArgumentException();
            checkEvents(oldEvents, true);
            checkEvents(currentEvents, false);
            this.oldEvents = oldEvents;
            this.currentEvents = currentEvents;
            updateColors();
        }

        private void update(FullEvent newGametick[]) {
            checkEvents(newGametick, false);
            if (currentEvents != null) {
                for (FullEvent event : currentEvents) {
                    int category = event.e.categorizeEvent();
                    oldEvents[category] = event;
                }
            }
            currentEvents = newGametick;
            updateColors();
        }

        private boolean isDowngradable(int gametick) {
            for (FullEvent event : oldEvents) {
                if ((event != null) && (event.t >= gametick)) {
                    return false;
                }
            }
            return true;
        }

        private void downgrade(FullEvent previousGametick[]) {
            checkEvents(previousGametick, false);
            this.currentEvents = previousGametick;
            updateColors();
        }

        @Override
        public Iterator<EventView> iterator() {
            return new Iterator<EventView>() {
                int i;

                @Override
                public boolean hasNext() {
                    return (currentEvents != null) && (i < currentEvents.length);
                }

                @Override
                public EventView next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return new EventView(currentEvents[i++]);
                }
            };
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public int getDimension() {
            return d;
        }

        // previous state of the chunks
        public boolean wasLoadedInThePast() {
            return (oldEvents[0] != null);
        }

        // previous state of the chunks
        public boolean wasLoaded() {
            if (oldEvents[0] != null) {
                return oldEvents[0].e != Event.UNLOADING;
            } else return false;
        }

        // previous state of the chunks
        public boolean wasPlayerLoaded() {
            if (oldEvents[1] != null) {
                return oldEvents[1].e != Event.PLAYER_LEAVES;
            }
            return false;
        }

        // previous state of the chunks
        public boolean wasUnloadQueued() {
            if (oldEvents[0] != null) {
                return oldEvents[0].e == Event.QUEUE_UNLOAD;
            }
            return false;
        }

        // Color of the initial state at the start of the gametick is return in index 0
        // All other entries are the colors for the events that happen this gametick
        public int[] getColors() {
            return colors;
        }
    }

    public class MapView implements Iterable<ChunkView> {

        private int clearCount = Chunkdata.this.clearCount;
        private int gametick;
        private int minx = 0;
        private int maxx = 0;
        private int minz = 0;
        private int maxz = 0;
        private int dimension = 0;

        ChunkView chunkViews[];

        private MapView() {
            chunkViews = new ChunkView[0];
        }

        public int getLowerX() {
            return minx;
        }

        public int getLowerZ() {
            return minz;
        }

        public void seekSpace(int dimension, int minx, int maxx, int minz, int maxz) {
            if ((minx > maxx) || (minz > maxz)) {
                throw new IllegalArgumentException("Inverted range");
            }
            if ((this.dimension == dimension) &&
                    (this.minx == minx) &&
                    (this.maxx == maxx) &&
                    (this.minz == minz) &&
                    (this.maxz == maxz)) {
                return;
            }

            FullEvent thisGametick[][] = getAllChunksForGametick(gametick, dimension, minx, maxx, minz, maxz);

            int oldxsize = this.maxx - this.minx;
            int oldzsize = this.maxz - this.minz;
            int xsize = maxx - minx;
            int zsize = maxz - minz;

            ChunkView[] newViews = new ChunkView[xsize * zsize];

            for (int zi = 0; zi < zsize; ++zi) {
                int z = zi + minz;
                int oldzi = z - this.minz;
                boolean zok = (oldzi >= 0) && (oldzi < oldzsize);
                for (int xi = 0; xi < xsize; ++xi) {
                    int x = xi + minx;
                    int oldxi = x - this.minx;
                    boolean xok = (oldxi >= 0) && (oldxi < oldxsize);
                    int i = xi + zi * xsize;
                    if (xok && zok && (this.dimension == dimension)) {
                        int oldi = oldxi + oldzi * oldxsize;
                        ChunkView view = this.chunkViews[oldi];
                        if ((view.x != x) || (view.z != z) || (view.d != dimension)) {
                            throw new IllegalStateException();
                        }
                        newViews[i] = view;
                    } else {
                        newViews[i] = new ChunkView();
                        FullEvent old[] = getOldEventsForChunk(x, z, dimension, gametick);
                        newViews[i].reset(x, z, dimension, old, thisGametick[i]);
                    }
                }
            }
            this.chunkViews = newViews;
            this.minx = minx;
            this.maxx = maxx;
            this.minz = minz;
            this.maxz = maxz;
            this.dimension = dimension;
        }

        public void seekTime(int gametick) {
            if (gametick == this.gametick) {
                return;
            }
            int xsize = maxx - minx;
            int zsize = maxz - minz;
            FullEvent seekGametick[][] = getAllChunksForGametick(gametick, dimension, minx, maxx, minz, maxz);
            // seek one step forward
            if ((gametick > this.gametick) && (gametick <= getNextGametick(this.gametick))) {
                for (int zi = 0; zi < zsize; ++zi) {
                    int z = zi + minz;
                    for (int xi = 0; xi < xsize; ++xi) {
                        int x = xi + minx;
                        int i = xi + zi * xsize;
                        chunkViews[i].update(seekGametick[i]);
                    }
                }
            }
            // seek one step backward
            else if ((gametick < this.gametick) && (gametick >= getPrevGametick(this.gametick))) {
                for (int zi = 0; zi < zsize; ++zi) {
                    int z = zi + minz;
                    for (int xi = 0; xi < xsize; ++xi) {
                        int x = xi + minx;
                        int i = xi + zi * xsize;
                        if (chunkViews[i].isDowngradable(gametick)) {
                            chunkViews[i].downgrade(seekGametick[i]);
                        } else {
                            FullEvent oldEvents[] = getOldEventsForChunk(x, z, dimension, gametick);
                            chunkViews[i].reset(x, z, dimension, oldEvents, seekGametick[i]);
                        }
                    }
                }
            }
            // seek to an arbitrary time
            else {
                for (int zi = 0; zi < zsize; ++zi) {
                    int z = zi + minz;
                    for (int xi = 0; xi < xsize; ++xi) {
                        int x = xi + minx;
                        int i = xi + zi * xsize;
                        FullEvent old[] = getOldEventsForChunk(x, z, dimension, gametick);
                        chunkViews[i].reset(x, z, dimension, old, seekGametick[i]);
                    }
                }
            }
            this.gametick = gametick;
        }

        @Override
        public Iterator<ChunkView> iterator() {
            return new Iterator<ChunkView>() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < chunkViews.length;
                }

                @Override
                public ChunkView next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return chunkViews[i++];
                }
            };
        }

        public ChunkView pickChunk(int x, int z) {
            int xi = x - minx;
            int zi = z - minz;
            int i = xi + zi * (maxx - minx);
            if ((xi < 0) || (zi < 0) || (i >= chunkViews.length)) {
                return null;
            }
            return chunkViews[i];
        }
    }

    public Chunkdata() {
        init();
    }

    // called for each received stacktrace in order
    public void addStacktrace(String s, int id) {
        allStacktraces.put(id, s);
    }

    // called for each event received in order
    public void addData(int gametick, int eventNumber, int x, int z, int d, int eventcode, int traceid) {
        try {
            if ((allStacktraces.size() != 0) && (traceid >= allStacktraces.size())) {
                //throw new IllegalArgumentException();
                System.err.println(String.format("Warning: Referenced non-existant Stacktrace %d (All Stacktraces: %d)", traceid, allStacktraces.size()));
            }
            FullEvent event = new FullEvent(x, z, d, eventcode, traceid, gametick, eventNumber);
            this.receiveBuffer.add(event);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void completeData() {
        try {
            for (FullEvent event : receiveBuffer) {
                //System.out.println("received event:" + event.toString());
                allEvents[event.e.categorizeEvent()].addEvent(event);
            }
            receiveBuffer.clear();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public int getFirstGametick() {
        int first = Integer.MAX_VALUE;
        for (EventCollection c : allEvents) {
            if (!c.eventsForGametick.isEmpty()) {
                first = Integer.min(first, c.eventsForGametick.firstKey());
            }
        }
        if (first == Integer.MAX_VALUE) {
            return -1;
        }
        return first >= 0 ? first : -first;
    }

    public int getLastGametick() {
        int last = Integer.MIN_VALUE;
        for (EventCollection c : allEvents) {
            if (!c.eventsForGametick.isEmpty()) {
                last = Integer.max(last, c.eventsForGametick.lastKey());
            }
        }
        return last >= 0 ? last : -last;
    }

    public int getNextGametick(int gametick) {
        int nextGametick = getLastGametick();
        for (EventCollection c : allEvents) {
            Integer gt = c.eventsForGametick.higherKey(gametick);
            if (gt != null) {
                nextGametick = Integer.min(nextGametick, gt);
            }
        }
        return nextGametick;
    }

    public int getPrevGametick(int gametick) {
        int prevGametick = getFirstGametick();
        for (EventCollection c : allEvents) {
            Integer gt = c.eventsForGametick.lowerKey(gametick);
            if (gt != null) {
                prevGametick = Integer.max(prevGametick, gt);
            }
        }
        return prevGametick;
    }

    public int getPreviousGametickForChunk(int gametick, int x, int z, int d) {
        int gametickMax = getFirstGametick();
        FullEvent compare = new FullEvent(0, 0, 0, 0, 0, gametick, 0);
        for (EventCollection c : allEvents) {
            FullEvent[] events = c.getAllEventsForChunk(x, z, d);
            if (events != null) {
                int i = findPreviousInArray(events, compare, temporalSorted);
                if (i >= 0) {
                    gametickMax = Integer.max(gametickMax, events[i].t);
                }
            }
        }
        return gametickMax;
    }

    public int getNextGametickForChunk(int gametick, int x, int z, int d) {
        int gametickMin = getLastGametick();
        FullEvent compare = new FullEvent(0, 0, 0, 0, 0, gametick, Integer.MAX_VALUE);
        for (EventCollection c : allEvents) {
            FullEvent[] events = c.getAllEventsForChunk(x, z, d);
            if (events != null) {
                int i = findNextInArray(events, compare, temporalSorted);
                if (i >= 0) {
                    gametickMin = Integer.min(gametickMin, events[i].t);
                }
            }
        }
        return gametickMin;
    }

    private int clearCount = 0;
    private EventCollection allEvents[];
    private TreeMap<Integer, String> allStacktraces;
    private ArrayList<FullEvent> receiveBuffer = new ArrayList<>();

    private class EventCollection {
        TreeMap<Integer, FullEvent[]> eventsForGametick = new TreeMap<Integer, FullEvent[]>();
        TreeMap<FullEvent, FullEvent[]> eventsForChunk;

        EventCollection() {
            eventsForChunk = new TreeMap<FullEvent, FullEvent[]>(spacialSorted);
        }

        void addEvent(FullEvent e) {
            FullEvent[] gtEvents = eventsForGametick.getOrDefault(e.t, null);
            FullEvent[] chEvents = eventsForChunk.getOrDefault(e, null);
            if ((gtEvents != null) && temporalSorted.compare(gtEvents[gtEvents.length - 1], e) >= 0) {
                throw new IllegalArgumentException("Events added need to be strictly temporally ordered");
            }
            if ((chEvents != null) && temporalSorted.compare(chEvents[chEvents.length - 1], e) >= 0) {
                throw new IllegalArgumentException("Events added need to be strictly temporally ordered");
            }
            gtEvents = addToArray(gtEvents, e);
            chEvents = addToArray(chEvents, e);
            eventsForGametick.put(e.t, gtEvents);
            eventsForChunk.put(chEvents[0], chEvents);
        }

        FullEvent[] getAllEventsForChunk(int x, int z, int d) {
            FullEvent compare = new FullEvent(x, z, d, 0, 0, 0, 0);
            return eventsForChunk.getOrDefault(compare, null);
        }

        FullEvent[] getAllEventsForGametick(int t) {
            return eventsForGametick.getOrDefault(t, null);
        }
    }

    private FullEvent[] sortedEventsForGametick(int gametick) {
        FullEvent[][] events = new FullEvent[categoryCount][];
        for (int i = 0; i < categoryCount; ++i) {
            events[i] = allEvents[i].getAllEventsForGametick(gametick);
        }
        FullEvent merged[] = mergeArrays(events);
        Arrays.sort(merged, temporalSorted);
        Arrays.sort(merged, spacialSorted);
        return merged;
    }

    private FullEvent[][] getAllChunksForGametick(int gametick, int dimension, int minx, int maxx, int minz, int maxz) {
        int xsize = maxx - minx;
        int zsize = maxz - minz;
        FullEvent[][] allChunks = new FullEvent[xsize * zsize][];
        FullEvent[] thisGametick = sortedEventsForGametick(gametick);
        for (int zi = 0; zi < zsize; ++zi) {
            int z = zi + minz;
            FullEvent min = new FullEvent(minx, z, dimension, 0, 0, 0, 0);
            FullEvent max = new FullEvent(maxx, z, dimension, 0, 0, 0, 0);
            int startindex = findPreviousInArray(thisGametick, min, spacialSorted) + 1;
            while (startindex < thisGametick.length) {
                FullEvent event = thisGametick[startindex];
                if (spacialSorted.compare(event, max) >= 0) {
                    break;
                }
                int endindex;
                for (endindex = startindex + 1; (endindex < thisGametick.length) && (spacialSorted.compare(thisGametick[endindex], event) == 0); ++endindex);

                int x = event.x;
                int xi = x - minx;
                int i = xi + zi * xsize;
                allChunks[i] = Arrays.copyOfRange(thisGametick, startindex, endindex);
                startindex = endindex;

                for (FullEvent e : allChunks[i]) {
                    if ((e.x != x) || (e.z != z) || (e.d != dimension) || (e.t != gametick)) {
                        throw new IllegalStateException();
                    }
                }
            }
        }
        return allChunks;
    }

    private FullEvent[] getOldEventsForChunk(int x, int z, int d, int gametick) {
        FullEvent[] oldEvents = new FullEvent[categoryCount];
        FullEvent compare = new FullEvent(0, 0, 0, 0, 0, gametick, 0);
        for (int i = 0; i < categoryCount; ++i) {
            FullEvent events[] = allEvents[i].getAllEventsForChunk(x, z, d);
            if (events == null) {
                continue;
            }
            int arrayIndex = findPreviousInArray(events, compare, temporalSorted);
            if (arrayIndex >= 0) {
                oldEvents[i] = events[arrayIndex];
            }
        }
        return oldEvents;
    }

    private static Comparator<FullEvent> spacialSorted = new Comparator<FullEvent>() {
        @Override
        public int compare(FullEvent e1, FullEvent e2) {
            if (e1.d != e2.d) {
                return e1.d > e2.d ? 1 : -1;
            } else if (e1.z != e2.z) {
                return e1.z > e2.z ? 1 : -1;
            } else if (e1.x != e2.x) {
                return e1.x > e2.x ? 1 : -1;
            }
            return 0;
        }
    };

    private static Comparator<FullEvent> temporalSorted = new Comparator<FullEvent>() {
        @Override
        public int compare(FullEvent e1, FullEvent e2) {
            if (e1.t != e2.t) {
                return e1.t > e2.t ? 1 : -1;
            } else if (e1.o != e2.o) {
                return e1.o > e2.o ? 1 : -1;
            }
            return 0;
        }
    };

    private static FullEvent[] addToArray(FullEvent[] a, FullEvent e) {
        if (a == null) {
            FullEvent[] x = new FullEvent[1];
            x[0] = e;
            return x;

        } else {
            FullEvent[] x = Arrays.copyOf(a, a.length + 1);
            x[a.length] = e;
            return x;
        }
    }

    private static FullEvent[] mergeArrays(FullEvent[]... arrays) {
        int totallength = 0;
        int firstnonull = -1;
        for (int i = 0; i < arrays.length; ++i) {
            if (arrays[i] != null) {
                totallength += arrays[i].length;
                if (firstnonull == -1) {
                    firstnonull = i;
                }
            }
        }
        if (firstnonull == -1) {
            return new FullEvent[0];
        }
        FullEvent mergedArray[] = Arrays.copyOf(arrays[firstnonull], totallength);
        int insertindex = arrays[firstnonull].length;
        for (int i = firstnonull + 1; i < arrays.length; ++i) {
            if (arrays[i] != null) {
                System.arraycopy(arrays[i], 0, mergedArray, insertindex, arrays[i].length);
                insertindex += arrays[i].length;
            }
        }

        return mergedArray;
    }

    private static int findPreviousInArray(FullEvent[] a, FullEvent compare, Comparator<FullEvent> c) {
        int i = Arrays.binarySearch(a, compare, c);
        if (i >= 0) {
            while ((i >= 0) && (c.compare(a[i], compare) == 0)) {
                i = i - 1;
            }
        } else {
            i = -(i + 2);
        }
        // I dunno what shit I'm programming, so let's insert a sanity check
        if ((i < -1) || (i >= a.length)) {
            throw new IllegalStateException();
        }
        if (i >= 0) {
            // check next value must be larger or equal
            if ((i != a.length - 1) && (c.compare(a[i + 1], compare) < 0)) {
                throw new IllegalStateException();
            }
            //check actual value must be smaller
            if (c.compare(a[i], compare) >= 0) {
                throw new IllegalStateException();
            }
        }
        return i;
    }

    private static int findNextInArray(FullEvent[] a, FullEvent compare, Comparator<FullEvent> c) {
        int i = Arrays.binarySearch(a, compare, c);
        if (i >= 0) {
            while ((i < a.length) && c.compare(a[i], compare) == 0) {
                i = i + 1;
            }
            if (i == a.length) {
                i = -1;
            }
        } else {
            i = -(i + 1);
            i = i == a.length ? -1 : i;
        }
        // I dunno what shit I'm programming, so let's insert a sanity check
        if ((i < -1) || (i >= a.length)) {
            throw new IllegalStateException();
        }
        if (i >= 0) {
            // check previous value needs to be smaller or equal
            if ((i != 0) && (c.compare(a[i - 1], compare) > 0)) {
                throw new IllegalStateException();
            }
            // check actual value needs to be larger
            if (c.compare(a[i], compare) <= 0) {
                throw new IllegalStateException();
            }
        }
        return i;
    }

    private static class FullEvent {
        final int x;
        final int z;
        final int d;
        final Event e;
        final int s;
        final int t;
        final int o;

        FullEvent(int x, int z, int d, int e, int s, int t, int o) {
            if (o < 0) {
                throw new IllegalArgumentException();
            }
            this.x = x;
            this.z = z;
            this.d = d;
            this.e = Event.values()[e];
            this.s = s;
            this.t = t;
            this.o = o;
        }

        @Override
        public String toString() {
            return String.format("X: %d Z: %d D: %d t: %d O: %d event: %s stack: %d", x, z, d, t, o, e.toString(), s);
        }
    }

    private void init() {
        if (this.allEvents == null) {
            this.allEvents = new EventCollection[categoryCount];
        }
        for (int i = 0; i < categoryCount; ++i) {
            if (this.allEvents[i] == null) {
                this.allEvents[i] = new EventCollection();
            }
        }
        if (this.allStacktraces == null) {
            this.allStacktraces = new TreeMap();
        }
        if (this.receiveBuffer == null) {
            this.receiveBuffer = new ArrayList<>();
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

        out.writeInt(allStacktraces.size());

        for (String s : allStacktraces.values()) {
            out.writeObject(s);
        }
        int count = 0;
        for (EventCollection c : allEvents) {
            for (Entry<Integer, FullEvent[]> entry : c.eventsForGametick.entrySet()) {
                count += entry.getValue().length;
            }
        }

        out.writeInt(count);

        for (EventCollection c : allEvents) {
            for (Entry<Integer, FullEvent[]> entry : c.eventsForGametick.entrySet()) {
                for (FullEvent event : entry.getValue()) {
                    out.writeInt(event.x);
                    out.writeInt(event.z);
                    out.writeInt(event.d);
                    out.writeInt(event.t);
                    out.writeInt(event.o);
                    out.writeInt(event.e.ordinal());
                    out.writeInt(event.s);
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        init();
        int stcount = in.readInt();
        for (int i = 0; i < stcount; ++i) {
            this.addStacktrace((String) in.readObject(), i);
        }
        int eventCount = in.readInt();
        for (int i = 0; i < eventCount; ++i) {
            int x = in.readInt();
            int z = in.readInt();
            int d = in.readInt();
            int tick = in.readInt();
            int evnum = in.readInt();
            int event = in.readInt();
            int traceid = in.readInt();
            this.addData(tick, evnum, x, z, d, event, traceid);
        }
        this.completeData();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }

    private boolean unpackNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("data", 10);
        int time = nbt.getInteger("time");
        int offset = nbt.getInteger("offset");
        boolean complete = nbt.getBoolean("complete");
        for (int index = 0; index < list.tagCount(); ++index) {
            NBTTagCompound chunk = list.getCompoundTagAt(index);
            int x = chunk.getInteger("x");
            int z = chunk.getInteger("z");
            int dimension = chunk.getInteger("d");
            int event = chunk.getInteger("event");
            int stacktrace = chunk.getInteger("trace");
            addData(time, index + offset, x, z, dimension, event, stacktrace);
        }
        if(complete) {
            completeData();
            return true;
        }
        return false;
    }

    private void unpackNBTStackTrace(NBTTagCompound nbt) {
        NBTTagList nbttaglist = nbt.getTagList("stackList", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int id = nbttagcompound.getInteger("id");
            String stack = nbttagcompound.getString("stack");
            addStacktrace(stack,id);
        }
    }

    private static Chunkdata instance;

    public static Chunkdata restartRecording() {
        instance = new Chunkdata();
        return instance;
    }

    private static final int PACKET_EVENTS = 0;
    private static final int PACKET_STACKTRACE = 1;
    private static final int PACKET_ACCESS_DENIED = 2;

    public static void processPacket(PacketBuffer data) {
        int type = data.readInt();

        NBTTagCompound nbt;
        try {
            nbt = data.readCompoundTag();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nbt == null) return;

        if (PACKET_EVENTS == type) {
            if(instance != null) {
                if(instance.unpackNBT(nbt)) {
                    GuiChunkGrid.instance.liveUpdate();
                }
            }
        }
        else if (PACKET_STACKTRACE == type) {
            if (instance != null) {
                instance.unpackNBTStackTrace(nbt);
            }
        }
        else if (PACKET_ACCESS_DENIED == type) {
            GuiChunkGrid.instance.disableDebugger();
            instance = null;
        }
    }
}

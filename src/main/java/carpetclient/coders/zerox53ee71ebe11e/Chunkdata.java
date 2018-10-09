package carpetclient.coders.zerox53ee71ebe11e;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.io.IOException;

public class Chunkdata implements Serializable {

    public static enum Event {
        MISSED_EVENT_ERROR,
        UNLOADING,
        LOADING,
        PLAYER_ENTERS,
        PLAYER_LEAVES,
        QUEUE_UNLOAD,
        CANCEL_UNLOAD,
        UNQUEUE_UNLOAD,
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

        public boolean isQueueEvent() {
            switch (this) {
                case QUEUE_UNLOAD:
                case CANCEL_UNLOAD:
                case UNQUEUE_UNLOAD:
                    return true;
                default:
                    return false;
            }
        }

        static final int eventColors[] = {
                0xffff0000,//MISSED_EVENT_ERROR
                0xffaa0000,//UNLOADING
                0xff00aa00,//LOADING
                0xff00aaaa,//PLAYER_ENTERS
                0xffaa00aa,//PLAYER_LEAVES
                0xffaaaa00,//QUEUE_UNLOAD
                0xff66aa00,//CANCEL_UNLOAD
                0xff33aa00,//UNQUEUE_UNLOAD
                0xff00aa66,//GENERATING
                0xff00aa33,//POPULATING
                0xff00aa11//GENERATING_STRUCTURES
        };

        public int getColor() {
            return eventColors[this.ordinal()];
        }
    }

    public static final class ChunkLogChunkCoords {

        public final int x;
        public final int z;
        public final int d;

        ChunkLogChunkCoords(int x, int z, int d) {
            this.x = x;
            this.z = z;
            this.d = d;
        }

        public int compareTo(ChunkLogChunkCoords other) {
            if (this.d != other.d) {
                return this.d > other.d ? 1 : -1;
            } else if (this.z != other.z) {
                return this.z > other.z ? 1 : -1;
            } else if (this.x != other.x) {
                return this.x > other.x ? 1 : -1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object oo) {
            if (oo instanceof ChunkLogChunkCoords) {
                ChunkLogChunkCoords o = (ChunkLogChunkCoords) oo;
                return this.compareTo(o) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (x * 1281773681) + (z * 1298815619) + (d * 2022620329);
        }

    }

    public static class ChunkLogTimeCoords {

        public final int gametick;
        public final int eventNumber;

        ChunkLogTimeCoords(int gametick, int eventNumber) {
            this.gametick = gametick;
            this.eventNumber = eventNumber;
        }

        public int compareTo(ChunkLogTimeCoords other) {
            if (this.gametick != other.gametick) {
                return this.gametick > other.gametick ? 1 : -1;
            } else if (this.eventNumber != other.eventNumber) {
                return this.eventNumber > other.eventNumber ? 1 : -1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object oo) {
            if (oo instanceof ChunkLogTimeCoords) {
                ChunkLogTimeCoords o = (ChunkLogTimeCoords) oo;
                return this.compareTo(o) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (gametick * 2145814481) + (eventNumber * 1247900611);
        }

    }

    public static class ChunkLogEvent {

        public final Event event;
        public final int stackTraceId;

        ChunkLogEvent(Event event, int traceid) {
            this.event = event;
            this.stackTraceId = traceid;
        }

        @Override
        public boolean equals(Object oo) {
            if (oo instanceof ChunkLogEvent) {
                ChunkLogEvent o = (ChunkLogEvent) oo;
                return this.event == o.event && this.stackTraceId == o.stackTraceId;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (event.ordinal() * 1369892371) + (stackTraceId * 1713470669);
        }
    }

    public static class ChunkLogCoords {

        public final ChunkLogChunkCoords space;
        public final ChunkLogTimeCoords time;

        ChunkLogCoords(ChunkLogChunkCoords space, ChunkLogTimeCoords time) {
            this.space = space;
            this.time = time;
        }

        ChunkLogCoords(int x, int z, int d, int gametick, int eventNumber) {
            space = new ChunkLogChunkCoords(x, z, d);
            time = new ChunkLogTimeCoords(gametick, eventNumber);
        }

        ChunkLogCoords(ChunkLogChunkCoords space, int gametick, int eventNumber) {
            this.space = space;
            time = new ChunkLogTimeCoords(gametick, eventNumber);
        }

        ChunkLogCoords(int x, int z, int d, ChunkLogTimeCoords time) {
            space = new ChunkLogChunkCoords(x, z, d);
            this.time = time;
        }

        @Override
        public boolean equals(Object oo) {
            if (oo instanceof ChunkLogCoords) {
                ChunkLogCoords o = (ChunkLogCoords) oo;
                return this.space.equals(o.space) && this.time.equals(o.time);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return time.hashCode() + space.hashCode();
        }

        @Override
        public String toString() {
            return String.format("gametick: %d order: %d x: %d z: %d d: %d", time.gametick, time.eventNumber, space.x, space.z, space.d);
        }

    }

    int latestGametick;

    static final Comparator<ChunkLogCoords> compareGroupTime = new Comparator<ChunkLogCoords>() {
        @Override
        public int compare(ChunkLogCoords a, ChunkLogCoords b) {
            int time = a.time.compareTo(b.time);
            if (time != 0) {
                return time;
            } else {
                return a.space.compareTo(b.space);
            }
        }
    };

    static final Comparator<ChunkLogCoords> compareGroupChunks = new Comparator<ChunkLogCoords>() {
        @Override
        public int compare(ChunkLogCoords a, ChunkLogCoords b) {
            int space = a.space.compareTo(b.space);
            if (space != 0) {
                return space;
            } else {
                return a.time.compareTo(b.time);
            }
        }
    };

    static final ChunkLogChunkCoords spaceMin = new ChunkLogChunkCoords(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    static final ChunkLogChunkCoords spaceMax = new ChunkLogChunkCoords(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    static final ChunkLogTimeCoords timeMin = new ChunkLogTimeCoords(Integer.MIN_VALUE , Integer.MIN_VALUE);
    static final ChunkLogTimeCoords timeMax = new ChunkLogTimeCoords(Integer.MAX_VALUE, Integer.MAX_VALUE);

    ArrayList<String> allStackTraces = new ArrayList();
    public SortedLogs playerLogs = new SortedLogs();
    public SortedLogs chunkLogs = new SortedLogs();
    HashMap<ChunkLogChunkCoords, ChunkLogChunkCoords> allChunks = new HashMap();
    HashMap<ChunkLogEvent, ChunkLogEvent> allEvents = new HashMap();

    public class SortedLogs {

        TreeMap<ChunkLogCoords, ChunkLogEvent> logsGroupedByTime = new TreeMap(compareGroupTime);
        TreeMap<ChunkLogCoords, ChunkLogEvent> logsGroupedByChunk = new TreeMap(compareGroupChunks);

        public SortedMap<ChunkLogCoords, ChunkLogEvent> getLogsForGametick(int gametick) {
            ChunkLogCoords low = new ChunkLogCoords(spaceMin, gametick, 0);
            ChunkLogCoords high = new ChunkLogCoords(spaceMax, gametick, Integer.MAX_VALUE);
            return logsGroupedByTime.subMap(low, true, high, true);
        }

        public SortedMap<ChunkLogCoords, ChunkLogEvent> getLogsForChunk(ChunkLogChunkCoords coords) {
            ChunkLogCoords low = new ChunkLogCoords(coords, timeMin);
            ChunkLogCoords high = new ChunkLogCoords(coords, timeMax);
            return logsGroupedByChunk.subMap(low, true, high, true);
        }

        @Deprecated
        public SortedMap<ChunkLogCoords, ChunkLogEvent> getLogsForDisplayArea(
                int gametick,
                int dimension,
                int minx, int maxx, int minz, int maxz) {
            TreeMap<ChunkLogCoords, ChunkLogEvent> logs = new TreeMap(compareGroupChunks);
            logs.putAll(getPreviousLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz));
            logs.putAll(getCurrentLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz));
            return logs;
        }

        void checkChunkRange(ChunkLogCoords c, int xmin, int xmax, int zmin, int zmax, int dim, int tmin, int tmax) {
            boolean xok = (c.space.x >= xmin) && (c.space.x < xmax);
            boolean zok = (c.space.z >= zmin) && (c.space.z < zmax);
            boolean dok = (c.space.d == dim);
            boolean tok = (c.time.gametick >= tmin) && (c.time.gametick < tmax);
            if (xok && zok && dok && tok) {
                return;
            } else {
                String bad = new String();
                if (!xok) bad += String.format("x = %d, should be in range [%d, %d] ", c.space.x, xmin, xmax - 1);
                if (!zok) bad += String.format("z = %d, should be in range [%d, %d] ", c.space.z, zmin, zmax - 1);
                if (!dok) bad += String.format("d = %d, should be %d ", c.space.d, dim);
                if (!tok) bad += String.format("t = %d, should be in range [%d, %d] ", c.time.gametick, tmin, tmax - 1);
                throw new IllegalStateException(bad);
            }
        }

        SortedMap<ChunkLogCoords, ChunkLogEvent> getCurrentLogsForDisplayArea(
                int gametick,
                int dimension,
                int minx, int maxx, int minz, int maxz) {
            TreeMap<ChunkLogCoords, ChunkLogEvent> currentEvents = new TreeMap(compareGroupChunks);
            SortedMap<ChunkLogCoords, ChunkLogEvent> currentAreaEvents = new TreeMap(compareGroupChunks);
            currentAreaEvents.putAll(getLogsForGametick(gametick));
            for (int z = minz; z < maxz; ++z) {
                ChunkLogCoords chunkMin = new ChunkLogCoords(minx, z, dimension, timeMin);
                ChunkLogCoords chunkMax = new ChunkLogCoords(maxx, z, dimension, timeMin);
                SortedMap<ChunkLogCoords, ChunkLogEvent> chunks = currentEvents.subMap(chunkMin, chunkMax);
                for (ChunkLogCoords coords : chunks.keySet()) {
                    checkChunkRange(coords, minx, maxx, minz, maxz, dimension, gametick, gametick + 1);
                }
                currentAreaEvents.putAll(chunks);
            }
            return currentAreaEvents;
        }

        SortedMap<ChunkLogCoords, ChunkLogEvent> getPreviousLogsForDisplayArea(
                int gametick,
                int dimension,
                int minx, int maxx, int minz, int maxz
        ) {
            TreeMap<ChunkLogCoords, ChunkLogEvent> previousEvents = new TreeMap(compareGroupChunks);
            for (int z = minz; z < maxz; ++z) {
                ChunkLogCoords minSpace = new ChunkLogCoords(minx, z, dimension, timeMin);
                ChunkLogCoords maxSpace = new ChunkLogCoords(maxx, z, dimension, timeMin);
                SortedMap<ChunkLogCoords, ChunkLogEvent> rest = this.logsGroupedByChunk.tailMap(minSpace);
                while (!rest.isEmpty()) {
                    ChunkLogCoords first = rest.firstKey();
                    if (compareGroupChunks.compare(first, maxSpace) >= 0) {
                        break;
                    }
                    ChunkLogCoords chunkMaxTime = new ChunkLogCoords(first.space.x, z, dimension, gametick, 0);
                    System.err.println("foo1: " + first);
                    System.err.println("foo2: " + chunkMaxTime);
                    SortedMap<ChunkLogCoords, ChunkLogEvent> uptoGametick = rest.headMap(chunkMaxTime);
                    if (!uptoGametick.isEmpty()) {
                        ChunkLogCoords key = uptoGametick.firstKey();
                        ChunkLogEvent event = uptoGametick.get(key);
                        checkChunkRange(key, minx, maxx, minz, maxz, dimension, Integer.MIN_VALUE, gametick);
                        previousEvents.put(key, event);
                    }
                    ChunkLogCoords nextChunk = new ChunkLogCoords(first.space.x + 1, z, dimension, 0, 0);
                    rest = rest.tailMap(nextChunk);
                }
            }
            return previousEvents;
        }

        public void put(ChunkLogCoords coords, ChunkLogEvent event) {
            this.logsGroupedByChunk.put(coords, event);
            this.logsGroupedByTime.put(coords, event);
        }

        public void clear() {
            logsGroupedByTime.clear();
            logsGroupedByChunk.clear();
        }

    }

    public MapView getChunkData() {
        return new MapView();
    }

    public class EventView {
        int order;
        ChunkLogEvent event;

        EventView(int order, ChunkLogEvent event) {
            this.order = order;
            this.event = event;
        }

        int getOrder() {
            return this.order;
        }

        public Event getType() {
            return event.event;
        }

        public String getStacktrace() {
            if (this.event.stackTraceId < allStackTraces.size()) {
                return allStackTraces.get(this.event.stackTraceId);
            } else {
                return null;
            }
        }
    }

    public class ChunkView implements Iterable<EventView> {
        SortedMap<ChunkLogCoords, ChunkLogEvent> events;
        int gametick;

        ChunkView(SortedMap<ChunkLogCoords, ChunkLogEvent> events, int gametick) {
            this.events = events;
            this.gametick = gametick;
        }

        @Override
        public Iterator<EventView> iterator() {
            return new Iterator<EventView>() {
                Iterator<Entry<ChunkLogCoords, ChunkLogEvent>> i = events.entrySet().iterator();
                Entry<ChunkLogCoords, ChunkLogEvent> n = null;

                @Override
                public boolean hasNext() {
                    while (n == null || (n.getKey().time.gametick != gametick)) {
                        if (i.hasNext()) {
                            n = i.next();
                        } else {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public EventView next() {
                    if (hasNext()) {
                        Entry<ChunkLogCoords, ChunkLogEvent> n = this.n;
                        this.n = null;
                        return new EventView(n.getKey().time.eventNumber, n.getValue());
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        public int getX() {
            return events.firstKey().space.x;
        }

        public int getZ() {
            return events.firstKey().space.z;
        }

        public int getDimension() {
            return events.firstKey().space.d;
        }

        public boolean isPlayerLoaded() {
            boolean x = false;
            int count = 0;
            for (ChunkLogEvent event : events.values()) {
                if (event.event.isPlayerEvent()) {
                    count++;
                    if (count > 1) {
                        System.err.println("Your crappy data structure is completely broken");
                    }
                    x = event.event == Event.PLAYER_ENTERS;
                }
            }
            return x;
        }

        public boolean isLoaded() {
            boolean loaded = false;
            for (ChunkLogEvent event : events.values()) {
                if (!event.event.isPlayerEvent()) {
                    loaded = event.event != Event.UNLOADING;
                }
            }
            return loaded;
        }

        // Color of the initial state at the start of the gametick is return in index 0
        // All other entries are the colors for the events that happen this gametick
        public int[] getColors() {
            int countofevents = 0;
            int currentTickColor = 0;
            for (Entry<ChunkLogCoords, ChunkLogEvent> entry : this.events.entrySet()) {
                ChunkLogCoords coords = entry.getKey();
                ChunkLogEvent event = entry.getValue();
                if (coords.time.gametick < gametick) {
                    currentTickColor = event.event.getColor();
                } else if (coords.time.gametick == gametick) {
                    countofevents++;
                } else {
                    throw new IllegalStateException("Found an event for a future gametick");
                }
            }
            int[] colors = new int[countofevents + 1];
            int i = 1;
            colors[0] = currentTickColor;
            for (Entry<ChunkLogCoords, ChunkLogEvent> entry : this.events.entrySet()) {
                ChunkLogCoords coords = entry.getKey();
                ChunkLogEvent event = entry.getValue();
                if (coords.time.gametick == gametick) {
                    colors[i++] = event.event.getColor();
                }
            }
            return colors;
        }
    }

    public class MapView implements Iterable<ChunkView> {

        TreeMap<ChunkLogCoords, ChunkLogEvent> currentMap = new TreeMap(compareGroupChunks);
        int dimension = -1;
        int gametick = -1;
        int minx = 0;
        int minz = 0;
        int maxx = 0;
        int maxz = 0;

        /*
         * Sets the area to be displayed
         * min coordinates are inclusive
         * max coordinates are exclusive
         */
        public void seekSpace(int dimension, int minx, int maxx, int minz, int maxz) {
            if ((minx > maxx) || (minz > maxz)) {
                throw new IllegalArgumentException("Illegal range: min>max");
            }
            // Display area changed completely, get the whole thing
            if ((dimension != this.dimension) ||
                    (minx >= this.maxx) || (minz >= this.maxz) ||
                    (maxx <= this.minx) || (maxz <= this.minz)) {
                currentMap.clear();
                currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz));
            }
            // Display area just slightly adjusted, just get updates
            else {
                if ((minx > this.minx) || (maxx < this.maxx) || (minz > this.minz) || (maxz < this.maxz)) {
                    currentMap.keySet().removeIf(e ->
                            (e.space.x < minx) || (e.space.x >= maxx) || (e.space.z < minz) || (e.space.z >= maxz));
                }
                if (minx < this.minx) {
                    // area that got added to the left
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, this.minx, Integer.max(minz, this.minz), Integer.min(maxz, this.maxz)));
                }
                if (maxx > this.maxx) {
                    // area that got added to the right
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, this.maxx, maxx, Integer.max(minz, this.minz), Integer.min(maxz, this.maxz)));
                }
                if (minz < this.minz) {
                    // area that got added to the top
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, maxx, minz, this.minz));
                }
                if (maxz > this.maxz) {
                    // area that got added to the buttom
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, maxx, this.maxz, maxz));
                }
            }
            this.dimension = dimension;
            this.minx = minx;
            this.minz = minz;
            this.maxx = maxx;
            this.maxz = maxz;
        }

        private boolean isNextGametick(int gametick) {
            if (gametick < this.gametick) {
                return false;
            }
            if (gametick == this.gametick + 1) {
                return true;
            }
            int next = getNextGametick(this.gametick);
            return gametick <= next;
        }

        /* 
         * Seek display area to a given gametick.
         * Should be more efficient when going forward in time to the next gametick 
         */
        public void seekTime(int gametick) {
            if (!isNextGametick(gametick)) {
                // can't update efficiently, just seek
                currentMap.clear();
                currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz));
            } else {
                // update the set

                TreeMap<ChunkLogCoords, ChunkLogEvent> newEvents = new TreeMap(compareGroupChunks);
                newEvents.putAll(getAllLogsForGametick(gametick));
                for (int z = minz; z < maxz; ++z) {
                    ChunkLogCoords minChunk = new ChunkLogCoords(minx, z, dimension, gametick, 0);
                    ChunkLogCoords maxChunk = new ChunkLogCoords(maxx, z, dimension, gametick, 0);
                    // remove superseded entries
                    SortedMap<ChunkLogCoords, ChunkLogEvent> nowZ = newEvents.subMap(minChunk, maxChunk);
                    for (Entry<ChunkLogCoords, ChunkLogEvent> entry : nowZ.entrySet()) {
                        ChunkLogChunkCoords coords = entry.getKey().space;
                        ChunkLogCoords minEntry = new ChunkLogCoords(coords, timeMin);
                        ChunkLogCoords maxEntry = new ChunkLogCoords(coords, timeMax);
                        ChunkLogEvent event = entry.getValue();
                        SortedMap<ChunkLogCoords, ChunkLogEvent> currentEntries = currentMap.subMap(minEntry, true, maxEntry, true);
                        Iterator<Entry<ChunkLogCoords, ChunkLogEvent>> iter = currentEntries.entrySet().iterator();
                        while (iter.hasNext()) {
                            Entry<ChunkLogCoords, ChunkLogEvent> x = iter.next();
                            if (x.getValue().event.isPlayerEvent() == event.event.isPlayerEvent()) {
                                iter.remove();
                            }
                        }
                    }
                    // add new entries
                    currentMap.putAll(nowZ);
                }
            }
            this.gametick = gametick;
        }

        @Override
        public Iterator<ChunkView> iterator() {
            return new Iterator<ChunkView>() {
                SortedMap<ChunkLogCoords, ChunkLogEvent> rest = currentMap;


                @Override
                public boolean hasNext() {
                    return !rest.isEmpty();
                }

                @Override
                public ChunkView next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    ChunkLogCoords nextChunk = rest.firstKey();
                    ChunkLogCoords max = new ChunkLogCoords(nextChunk.space.x + 1, nextChunk.space.z, dimension, timeMin);
                    SortedMap<ChunkLogCoords, ChunkLogEvent> mapChunk = rest.headMap(max);
                    rest = rest.tailMap(max);
                    return new ChunkView(mapChunk, gametick);
                }
            };
        }

        public ChunkView pickChunk(int x, int z) {
            ChunkLogCoords min = new ChunkLogCoords(x, z, dimension, timeMin);
            ChunkLogCoords max = new ChunkLogCoords(x, z, dimension, timeMax);
            return new ChunkView(this.currentMap.subMap(min, true, max, true), gametick);
        }

        public SortedMap<ChunkLogCoords, ChunkLogEvent> getDisplayArea() {
            return currentMap;
        }
    }

    // called for each received stacktrace in order
    public void addStacktrace(String s) {
        this.allStackTraces.add(s);
    }

    // called for each event received in order
    public void addData(int gametick, int eventNumber, int x, int z, int d, int eventcode, int traceid) {

        if((gametick < 0) || (eventNumber<0)){
            throw new IllegalArgumentException();
        }

        ChunkLogChunkCoords chunk = new ChunkLogChunkCoords(x, z, d);
        ChunkLogTimeCoords time = new ChunkLogTimeCoords(gametick, eventNumber);
        if (eventcode < 0 || eventcode >= Event.values().length) {
            throw new IllegalArgumentException("Unknown chunk-event code received");
        }
        Event ec = Event.values()[eventcode];
        ChunkLogEvent event = new ChunkLogEvent(ec, traceid);

        // optimization to not waste memory
        if (allChunks.containsKey(chunk)) {
            chunk = allChunks.get(chunk);
        } else {
            allChunks.put(chunk, chunk);
        }
        if (allEvents.containsKey(event)) {
            event = allEvents.get(event);
        } else {
            allEvents.put(event, event);
        }
        ChunkLogCoords coords = new ChunkLogCoords(chunk, time);
        if (ec.isPlayerEvent()) {
            this.playerLogs.put(coords, event);
        } else {
            this.chunkLogs.put(coords, event);
        }
    }

    // Returns every event that happened in a given gametick
    @Deprecated
    public SortedMap<ChunkLogCoords, ChunkLogEvent> getAllLogsForGametick(int gametick) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map1 = playerLogs.getLogsForGametick(gametick);
        SortedMap<ChunkLogCoords, ChunkLogEvent> map2 = chunkLogs.getLogsForGametick(gametick);
        TreeMap map = new TreeMap(compareGroupChunks);
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    // Returns every event that happened to a given chunk
    @Deprecated
    public SortedMap<ChunkLogCoords, ChunkLogEvent> getAllLogsForChunk(ChunkLogChunkCoords coords) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map1 = playerLogs.getLogsForChunk(coords);
        SortedMap<ChunkLogCoords, ChunkLogEvent> map2 = chunkLogs.getLogsForChunk(coords);
        TreeMap map = new TreeMap(compareGroupTime);
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    /* 
     * Returns all relevant events to show on the map.
     * For each chunk it either returns the events that happened this gametick,
     * or the last event that happened in an earlier gametick.
     * If there was never any event for a particular chunk, it is not included.
     * min coordinates are inclusive,
     * max coordinates are exclusive
     */
    @Deprecated
    public SortedMap<ChunkLogCoords, ChunkLogEvent> getAllLogsForDisplayArea(
            int gametick,
            int dimension,
            int minx, int maxx, int minz, int maxz) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map1 = playerLogs.getLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz);
        SortedMap<ChunkLogCoords, ChunkLogEvent> map2 = chunkLogs.getLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz);
        TreeMap<ChunkLogCoords, ChunkLogEvent> map = new TreeMap(compareGroupChunks);
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    public int getFirstGametick() {
        return Integer.min(
                this.playerLogs.logsGroupedByTime.isEmpty() ?
                        0 : this.playerLogs.logsGroupedByTime.firstKey().time.gametick,
                this.chunkLogs.logsGroupedByTime.isEmpty() ?
                        0 : this.chunkLogs.logsGroupedByTime.firstKey().time.gametick);
    }

    public int getLastGametick() {
        return Integer.max(
                this.playerLogs.logsGroupedByTime.isEmpty() ?
                        0 : this.playerLogs.logsGroupedByTime.lastKey().time.gametick,
                this.chunkLogs.logsGroupedByTime.isEmpty() ?
                        0 : this.chunkLogs.logsGroupedByTime.lastKey().time.gametick);
    }

    public int getNextGametick(int gametick) {
        ChunkLogCoords min = new ChunkLogCoords(spaceMin, gametick + 1, 0);
        ChunkLogCoords next1 = this.playerLogs.logsGroupedByTime.ceilingKey(min);
        ChunkLogCoords next2 = this.chunkLogs.logsGroupedByTime.ceilingKey(min);
        int tick1 = next1 != null ? next1.time.gametick : getLastGametick();
        int tick2 = next2 != null ? next2.time.gametick : getLastGametick();
        int newtick = Integer.min(tick1, tick2);
        return newtick;
    }

    public int getPrevGametick(int gametick) {
        ChunkLogCoords min = new ChunkLogCoords(spaceMin, gametick, 0);
        ChunkLogCoords next1 = this.playerLogs.logsGroupedByTime.lowerKey(min);
        ChunkLogCoords next2 = this.chunkLogs.logsGroupedByTime.lowerKey(min);
        int tick1 = next1 != null ? next1.time.gametick : getFirstGametick();
        int tick2 = next2 != null ? next2.time.gametick : getFirstGametick();
        int newtick = Integer.max(tick1, tick2);
        return newtick;
    }

    public int getPreviousGametickForChunk(int gametick, int x, int z, int d) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map = getAllLogsForChunk(new ChunkLogChunkCoords(x, z, d));
        ChunkLogCoords coords = new ChunkLogCoords(x, z, d, gametick, 0);
        SortedMap<ChunkLogCoords, ChunkLogEvent> head = map.headMap(coords);
        if (head.isEmpty()) {
            return gametick;
        }
        return head.lastKey().time.gametick;
    }

    public int getNextGametickForChunk(int gametick, int x, int z, int d) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map = getAllLogsForChunk(new ChunkLogChunkCoords(x, z, d));
        ChunkLogCoords coords = new ChunkLogCoords(x, z, d, gametick + 1, 0);
        SortedMap<ChunkLogCoords, ChunkLogEvent> tail = map.tailMap(coords);
        if (tail.isEmpty()) {
            return gametick;
        }
        return tail.firstKey().time.gametick;
    }

    public void clear() {
        allChunks.clear();
        playerLogs.clear();
        chunkLogs.clear();
        allChunks.clear();
        allEvents.clear();
    }

    @Deprecated
    public String getStackTraceString(int stackTraceId) {
        if (stackTraceId < 0 || stackTraceId >= allStackTraces.size()) {
            return "No stack trace found by that stack trace id: " + stackTraceId;
        }
        return allStackTraces.get(stackTraceId);
    }

    public void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        int stcount = allStackTraces.size();
        out.writeInt(stcount);
        for (String s : allStackTraces) {
            out.writeObject(s);
        }
        int eventCount = playerLogs.logsGroupedByTime.size() + chunkLogs.logsGroupedByTime.size();
        out.writeInt(eventCount);
        Map<ChunkLogCoords, ChunkLogEvent> maps[] = new Map[]{playerLogs.logsGroupedByTime, chunkLogs.logsGroupedByChunk};
        for (Map<ChunkLogCoords, ChunkLogEvent> map : maps) {
            for (Entry<ChunkLogCoords, ChunkLogEvent> entry : map.entrySet()) {
                ChunkLogCoords coords = entry.getKey();
                ChunkLogEvent event = entry.getValue();
                out.writeInt(coords.space.x);
                out.writeInt(coords.space.z);
                out.writeInt(coords.space.d);
                out.writeInt(coords.time.gametick);
                out.writeInt(coords.time.eventNumber);
                out.writeInt(event.event.ordinal());
                out.writeInt(event.stackTraceId);
            }
        }
    }

    public void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        clear();
        int stcount = in.readInt();
        for (int i = 0; i < stcount; ++i) {
            this.addStacktrace((String) in.readObject());
        }
        int eventCount = in.readInt();
        for (int i = 0; i < eventCount; ++i) {
            System.out.println(eventCount + " load i " + i);
            int x = in.readInt();
            int z = in.readInt();
            int d = in.readInt();
            int tick = in.readInt();
            int evnum = in.readInt();
            int event = in.readInt();
            int traceid = in.readInt();
            this.addData(tick, evnum, x, z, d, event, traceid);
        }
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }
}

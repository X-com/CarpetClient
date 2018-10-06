package carpetclient.coders.zerox53ee71ebe11e;

import java.util.*;
import java.util.Map.Entry;

public class Chunkdata {

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
    }

    public static class ChunkLogChunkCoords {
        public int x;
        public int z;
        public int d;

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
        public int gametick;
        public int eventNumber;

        ChunkLogTimeCoords(int gametick, int eventNumber) {
            this.gametick = gametick;
            this.eventNumber = eventNumber;
        }

        public int compareTo(ChunkLogTimeCoords other) {
            if (this.gametick != other.gametick) {
                return this.gametick > other.gametick ? 1 : -1;
            } else if (this.eventNumber > other.eventNumber) {
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
        public Event event;
        public int stackTraceId;

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
        public ChunkLogChunkCoords space;
        public ChunkLogTimeCoords time;

        ChunkLogCoords(ChunkLogChunkCoords space, ChunkLogTimeCoords time) {
            this.space = space;
            this.time = time;
        }

        ChunkLogCoords(int x, int z, int d, int gametick, int eventNumber) {
            space = new ChunkLogChunkCoords(x, z, d);
            time = new ChunkLogTimeCoords(gametick, eventNumber);
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

    static final ChunkLogTimeCoords timeMin = new ChunkLogTimeCoords(0, 0);
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
            int mv = Integer.MAX_VALUE;
            ChunkLogCoords low = new ChunkLogCoords(0, 0, 0, gametick, 0);
            ChunkLogCoords high = new ChunkLogCoords(mv, mv, mv, gametick, mv);
            return logsGroupedByTime.subMap(low, true, high, true);
        }

        public SortedMap<ChunkLogCoords, ChunkLogEvent> getLogsForChunk(ChunkLogChunkCoords coords) {
            int mv = Integer.MAX_VALUE;
            ChunkLogCoords low = new ChunkLogCoords(coords, new ChunkLogTimeCoords(0, 0));
            ChunkLogCoords high = new ChunkLogCoords(coords, new ChunkLogTimeCoords(mv, mv));
            return logsGroupedByChunk.subMap(low, true, high, true);
        }

        public SortedMap<ChunkLogCoords, ChunkLogEvent> getLogsForDisplayArea(
                int gametick,
                int dimension,
                int minx, int maxx, int minz, int maxz) {
            TreeMap<ChunkLogCoords, ChunkLogEvent> logs = new TreeMap(compareGroupTime);
            int mv = Integer.MAX_VALUE;
            TreeMap<ChunkLogCoords, ChunkLogEvent> thisGametick = new TreeMap(compareGroupChunks);
            thisGametick.putAll(getLogsForGametick(gametick));
            ChunkLogChunkCoords coords = new ChunkLogChunkCoords(0, 0, dimension);
            ChunkLogCoords minLog = new ChunkLogCoords(coords, new ChunkLogTimeCoords(gametick, 0));
            ChunkLogCoords maxLog = new ChunkLogCoords(coords, new ChunkLogTimeCoords(gametick, mv));
            for (int z = minz; z < maxz; ++z) {
                for (int x = minx; x < maxx; ++x) {
                    coords.x = x;
                    coords.z = z;
                    SortedMap chunkThisGametick = thisGametick.subMap(minLog, true, maxLog, true);
                    if (chunkThisGametick.isEmpty()) {
                        Entry<ChunkLogCoords, ChunkLogEvent> previous = logsGroupedByChunk.lowerEntry(minLog);
                        if (previous != null && previous.getKey().space.equals(coords)) {
                            logs.put(previous.getKey(), previous.getValue());
                        }
                    } else {
                        logs.putAll(chunkThisGametick);
                    }
                }
            }
            return logs;
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

    public class MapView {
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
        public void seekSpace(int dimension, int minx, int minz, int maxx, int maxz) {
            if ((minx > maxx) || (minz > maxz)) {
                throw new IllegalArgumentException("Illegal range: min>max");
            }
            // Display area changed completely, get the whole thing
            if ((dimension != this.dimension) ||
                    (minx >= this.maxx) || (minz >= this.maxz) ||
                    (maxx <= this.minx) || (maxz <= this.minz)) {
                currentMap.clear();
                currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, minz, maxx, maxz));
            }
            // Display area just slightly adjusted, just get updates
            else {
                if ((minx > this.minx) || (maxx < this.maxx) || (minz > this.minz) || (maxz < this.maxz)) {
                    currentMap.keySet().removeIf(e ->
                            (e.space.x < minx) || (e.space.x >= maxx) || (e.space.z < minz) || (e.space.z >= maxz));
                }
                if (minx < this.minx) {
                    // area that got added to the left
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, Integer.max(minz, this.minz), this.minx, Integer.min(maxz, this.maxz)));
                }
                if (maxx > this.maxx) {
                    // area that got added to the right
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, this.maxx, Integer.max(minz, this.minz), maxx, Integer.min(maxz, this.maxz)));
                }
                if (minz < this.minz) {
                    // area that got added to the top
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, minz, maxx, this.minz));
                }
                if (maxz > this.maxz) {
                    // area that got added to the buttom
                    currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, this.maxz, maxx, maxz));
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
            int mv = Integer.MAX_VALUE;
            ChunkLogCoords maxnow = new ChunkLogCoords(mv, mv, mv, this.gametick, mv);
            ChunkLogCoords playernext = playerLogs.logsGroupedByTime.higherKey(maxnow);
            ChunkLogCoords chunknext = chunkLogs.logsGroupedByTime.higherKey(maxnow);
            return (Integer.min(playernext.time.gametick, chunknext.time.gametick) == gametick);
        }

        /* 
         * Seek display area to a given gametick.
         * Should be more efficient when going forward in time to the next gametick 
         */
        public void seekTime(int gametick) {
            if (!isNextGametick(gametick)) {
                // can't update efficiently, just seek
                currentMap.clear();
                currentMap.putAll(getAllLogsForDisplayArea(gametick, dimension, minx, minz, maxx, maxz));
            } else {
                // update the set
                ChunkLogCoords minEntry = new ChunkLogCoords(null, timeMin);
                ChunkLogCoords maxEntry = new ChunkLogCoords(null, timeMax);
                ChunkLogCoords minChunk = new ChunkLogCoords(minx, 0, dimension, gametick, 0);
                ChunkLogCoords maxChunk = new ChunkLogCoords(maxx, 0, dimension, gametick, 0);
                for (int z = minz; z < maxz; ++z) {
                    minChunk.space.z = z;
                    maxChunk.space.z = z;
                    SortedMap<ChunkLogCoords, ChunkLogEvent> chunkLineNow[] = new SortedMap[2];
                    chunkLineNow[0] = chunkLogs.logsGroupedByTime.subMap(minChunk, maxChunk);
                    chunkLineNow[1] = playerLogs.logsGroupedByTime.subMap(minChunk, maxChunk);
                    // remove superseded entries
                    for (SortedMap<ChunkLogCoords, ChunkLogEvent> nowZ : chunkLineNow) {
                        for (Entry<ChunkLogCoords, ChunkLogEvent> entry : nowZ.entrySet()) {
                            ChunkLogChunkCoords coords = entry.getKey().space;
                            minEntry.space = coords;
                            maxEntry.space = coords;
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
                    }
                    // add new entries
                    currentMap.putAll(chunkLineNow[0]);
                    currentMap.putAll(chunkLineNow[1]);
                }
            }
            this.gametick = gametick;
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
    public SortedMap<ChunkLogCoords, ChunkLogEvent> getAllLogsForGametick(int gametick) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map1 = playerLogs.getLogsForGametick(gametick);
        SortedMap<ChunkLogCoords, ChunkLogEvent> map2 = chunkLogs.getLogsForGametick(gametick);
        TreeMap map = new TreeMap(compareGroupChunks);
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }

    // Returns every event that happened to a given chunk
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
    public SortedMap<ChunkLogCoords, ChunkLogEvent> getAllLogsForDisplayArea(
            int gametick,
            int dimension,
            int minx, int maxx, int minz, int maxz) {
        SortedMap<ChunkLogCoords, ChunkLogEvent> map1 = playerLogs.getLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz);
        SortedMap<ChunkLogCoords, ChunkLogEvent> map2 = chunkLogs.getLogsForDisplayArea(gametick, dimension, minx, maxx, minz, maxz);
        TreeMap map = new TreeMap(compareGroupChunks);
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
        ChunkLogCoords min = new ChunkLogCoords(0, 0, 0, gametick + 1, 0);
        ChunkLogCoords next1 = this.playerLogs.logsGroupedByTime.ceilingKey(min);
        ChunkLogCoords next2 = this.chunkLogs.logsGroupedByTime.ceilingKey(min);
        int tick1 = next1 != null ? next1.time.gametick : getLastGametick();
        int tick2 = next2 != null ? next2.time.gametick : getLastGametick();
        return Integer.min(tick1, tick2);
    }

    public int getPrevGametick(int gametick) {
        ChunkLogCoords min = new ChunkLogCoords(0, 0, 0, gametick, 0);
        ChunkLogCoords next1 = this.playerLogs.logsGroupedByTime.lowerKey(min);
        ChunkLogCoords next2 = this.chunkLogs.logsGroupedByTime.lowerKey(min);
        int tick1 = next1 != null ? next1.time.gametick : getFirstGametick();
        int tick2 = next2 != null ? next2.time.gametick : getFirstGametick();
        return Integer.max(tick1, tick2);
    }

    public void clear() {
        allChunks.clear();
        playerLogs.clear();
        chunkLogs.clear();
        allChunks.clear();
        allEvents.clear();
    }
}
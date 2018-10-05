package carpetclient.coders.zerox53ee71ebe11e;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class ChunkLogData {
    public ArrayList<TimeIndex> data;
    private TreeMap<Integer, String> stacktraces;

    public ChunkLogData() {
        data = new ArrayList<>();
        stacktraces = new TreeMap<>();
    }

    public TimeIndex addData(int time) {
        TimeIndex ti = new TimeIndex(time);
        data.add(ti);
        return ti;
    }

    public void addStackTrace(int id, String stack) {
        stacktraces.put(id, stack);
    }

    public TimeIndex timeIndexByIndex(int index) {
        if (index < 0 || data.size() <= index) {
            return null;
        }
        return data.get(index);
    }

    public int latestIndex() {
        return data.size() - 1;
    }

    public class TimeIndex {
        int time;
        Dimention[] dimentions;

        public TimeIndex(int t) {
            time = t;
            dimentions = new Dimention[]{
                    new Dimention("Overworld"),
                    new Dimention("Nether"),
                    new Dimention("End")
            };
        }

        public void addToDimention(int dimention, int x, int z, int event, int stacktrace, int index) {
            dimentions[dimention].addChunk(x, z, event, stacktrace, index);
        }

        public int getTime() {
            return time;
        }

        public int getChunkEvent(int x, int z, int dimention) {
            return dimentions[dimention].getChunkEvent(x, z);
        }
    }

    public class Dimention {
        String name;

        public Dimention(String n) {
            name = n;
        }

        HashMap<Integer, ChunkData> data = new HashMap<Integer, ChunkData>();

        private void addChunk(int x, int z, int event, int stacktrace, int index) {
            data.put(getHash(x, z), new ChunkData(x, z, event, stacktrace, index));
        }

        public int getChunkEvent(int x, int z) {
            ChunkData chunk = data.get(getHash(x, z));
            if (chunk == null) return -1;
            return chunk.getEvent();
        }
    }

    public class ChunkData {
        int x;
        int z;
        int event;
        int stacktrace;
        int index;

        public ChunkData(int x, int z, int event, int stacktrace, int index) {
            this.x = x;
            this.z = z;
            this.event = event;
            this.stacktrace = stacktrace;
            this.index = index;
        }

        public int getEvent() {
            return event;
        }
    }

    public static int getHash(int x, int z) {
        int i = 1664525 * x + 1013904223;
        int j = 1664525 * (z ^ -559038737) + 1013904223;
        return i ^ j;
    }
}

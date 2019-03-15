package carpetclient;

import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.gui.chunkgrid.ChunkGridStyle;
import carpetclient.gui.chunkgrid.GuiChunkGrid;
import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
Config class for accessing mod variables.
 */
public class Config {
    public static boolean relaxedBlockPlacement = true;
    public static boolean snapAim = false;
    public static boolean accurateBlockPlacement = true;
    public static boolean controlQCrafting = true;
    public static boolean missingTools = false;
    public static boolean boundingBoxMarkers = false;
    public static boolean villageMarkers = false;
    public static int structureBlockLimit = 32;
    public static int pushLimit = 12;
    public static float tickRate;
    public static boolean playerCollisions = true;

    public static boolean setTickRate = false;
    public static boolean bucketGhostBlockFix = true;
    public static boolean elytraFix = false;
    public static boolean clipThroughPistons = true;
    public static boolean isScoreboardHidden = false;
    public static boolean pistonVisualizer = false;
    public static boolean randomtickingChunksVisualizer = false;
    public static boolean randomtickChunkUpdates = true;
    public static boolean ignoreEntityWhenPlacing = false;

    public static void save() {
        String file = "config/carpetclient.cfg";
        JsonObject obj = new JsonObject();
        Gson gson = new Gson();

        obj.addProperty("setTickRate", setTickRate);
        obj.addProperty("bucketGhostBlockFix", bucketGhostBlockFix);
        obj.addProperty("elytraFix", elytraFix);
        obj.addProperty("clipThroughPistons", clipThroughPistons);
        obj.addProperty("isScoreboardHidden", isScoreboardHidden);
        obj.addProperty("pistonVisualizer", pistonVisualizer);

        obj.addProperty("lines", VillageMarker.lines);
        obj.addProperty("golem", VillageMarker.golem);
        obj.addProperty("population", VillageMarker.population);
        obj.addProperty("village_radius", VillageMarker.village_radius);
        obj.addProperty("door_radius", VillageMarker.door_radius);

        obj.add("boundingBoxes", gson.toJsonTree(ShowBoundingBoxes.show));

        obj.add("chunkGridStyle", gson.toJsonTree(GuiChunkGrid.style));

        try {
            FileWriter writer = new FileWriter(file);
            writer.write((new GsonBuilder().setPrettyPrinting().create()).toJson(obj));
            writer.close();
        } catch (IOException e) {
            new File("config/").mkdirs();
        }
    }

    public static void load() {
        String file = "config/carpetclient.cfg";
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        String json = "";
        try {
            json = new String(Files.readAllBytes(Paths.get(file)));
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }

        try {
            JsonElement jsonTree = jsonParser.parse(json);
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                setTickRate = jsonObject.get("setTickRate").getAsBoolean();
                bucketGhostBlockFix = jsonObject.get("bucketGhostBlockFix").getAsBoolean();
                elytraFix = jsonObject.get("elytraFix").getAsBoolean();
                clipThroughPistons = jsonObject.get("clipThroughPistons").getAsBoolean();
                isScoreboardHidden = jsonObject.get("isScoreboardHidden").getAsBoolean();
                pistonVisualizer = jsonObject.get("pistonVisualizer").getAsBoolean();

                VillageMarker.lines = jsonObject.get("lines").getAsBoolean();
                VillageMarker.golem = jsonObject.get("golem").getAsBoolean();
                VillageMarker.population = jsonObject.get("population").getAsBoolean();
                VillageMarker.village_radius = jsonObject.get("village_radius").getAsInt();
                VillageMarker.door_radius = jsonObject.get("door_radius").getAsInt();

                ShowBoundingBoxes.show = gson.fromJson(jsonObject.get("boundingBoxes"), boolean[].class);

                GuiChunkGrid.style = gson.fromJson(jsonObject.get("chunkGridStyle"), ChunkGridStyle.class);
            }
        } catch (Exception e) {
        }
    }
}
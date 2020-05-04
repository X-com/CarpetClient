package carpetclient;

import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.config.ConfigBase;
import carpetclient.config.ConfigBoolean;
import carpetclient.config.ConfigInteger;
import carpetclient.gui.chunkgrid.ChunkGridStyle;
import carpetclient.gui.chunkgrid.GuiChunkGrid;
import carpetclient.random.RandomtickDisplay;
import carpetclient.rules.TickRate;
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
    public static int pushLimit = 12;
    public static float tickRate = 20.0f;
    public static boolean playerCollisions = true;
    public static boolean ignoreEntityWhenPlacing = false;
    public static boolean movableTileEntities = false;
    public static String pistonGhostBlocksFix = "";
    public static boolean stackableShulkersPlayerInventory = false;
    public static boolean betterMiner = false;

    public static ConfigBoolean setTickRate =                     new ConfigBoolean("Tick Rate", false, "Toggles if the client should slow the game down (Forge integrated clients might want to turn this off as the tick rate slowdown is not compatible with forge yet).");
    public static ConfigBoolean bucketGhostBlockFix =             new ConfigBoolean("Liquid ghost block fix", false, "Toggles the ghost block liquid option.");
    public static ConfigBoolean elytraFix =                       new ConfigBoolean("Elytra fix", false, "Earthcomputers hacky elytra fix.");
    public static ConfigBoolean clipThroughPistons =              new ConfigBoolean("Clip through pistons fix", true, "Parcially fixes clipping through pistons, clipping can still happen but this helps.");
    public static ConfigBoolean isScoreboardHidden =              new ConfigBoolean("Hide scoreboard", false, "Removes the scoreboard on the right.");
    public static ConfigBoolean pistonVisualizer =                new ConfigBoolean("Piston visualizer", false, "Shows piston push and pull update order when right clicking with empty hand on pistons.");
    public static ConfigBoolean randomtickingChunksVisualizer =   new ConfigBoolean("Randomtickchunk Index", false, "Displays the index of randomticking chunks around the player.");
    public static ConfigBoolean creativeModeNoClip =              new ConfigBoolean("Creative No Clip", false, "Disables block collisions when in creative mode, needs server rule to be enabled to work.");
    public static ConfigBoolean fastCrafting =                    new ConfigBoolean("Fast crafting", true, "Toggles fast crafting in the recipe book window.");
    public static ConfigBoolean carefulBreak =                    new ConfigBoolean("Careful Break", false, "Toggles careful break for when enabled on server.");
    public static boolean randomtickChunkUpdates = true;

    public static ConfigBoolean villageMarkerLines =              new ConfigBoolean("Village Doors", false,"Displays a line from the village center to the door.");
    public static ConfigBoolean villageMarkerGolem =              new ConfigBoolean("Golem Cage", false,"Displays the box where the golems will spawn.");
    public static ConfigBoolean villageMarkerPopulation =         new ConfigBoolean("Population Cage", false,"Displays a box where the villagers can be found.");
    public static ConfigInteger villageMarkerVillageRadius =      new ConfigInteger("Village Sphere Type", 1, "The type of marker used to display the village radius.");
    public static ConfigInteger villageMarkerDoorRadius =         new ConfigInteger("Door Sphere Type", 0, "The type of marker used to display the door radius. The radius that doors will be added to this village.");

    public static ConfigBoolean boundingBoxOuter =                new ConfigBoolean("Enclosing bounding box", true,"The main bounding box that wraps all inner bounding boxes.");
    public static ConfigBoolean boundingBoxEndCity =              new ConfigBoolean("End City", true,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxFortress =             new ConfigBoolean("Fortress", true,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxTemple =               new ConfigBoolean("Temple", true,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxVillage =              new ConfigBoolean("Village", false,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxStronghold =           new ConfigBoolean("Stronghold", false,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxMineshaft =            new ConfigBoolean("Mineshaft", false,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxMonument =             new ConfigBoolean("Monument", false,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxMansion =              new ConfigBoolean("Mansion", false,"Displays bounding box of this type.");
    public static ConfigBoolean boundingBoxSlimeChunk =           new ConfigBoolean("Slime Chunks", false,"Displays bounding box of this type.");


    public static ConfigBase[] CLIENTSETTINGS = new ConfigBase[] {
            setTickRate,
            bucketGhostBlockFix,
            elytraFix,
            clipThroughPistons,
            isScoreboardHidden,
            pistonVisualizer,
            randomtickingChunksVisualizer,
            creativeModeNoClip,
            fastCrafting,
            carefulBreak
    };

    public static ConfigBase[] VILLAGEMARKERS = new ConfigBase[]{
            villageMarkerLines,
            villageMarkerGolem,
            villageMarkerPopulation,
            villageMarkerVillageRadius,
            villageMarkerDoorRadius
    };

    public static ConfigBase[] BOUNDINGBOXES = new ConfigBase[]{
            boundingBoxOuter,
            boundingBoxEndCity,
            boundingBoxFortress,
            boundingBoxTemple,
            boundingBoxVillage,
            boundingBoxStronghold,
            boundingBoxMineshaft,
            boundingBoxMonument,
            boundingBoxMansion,
            boundingBoxSlimeChunk
    };

    static {
        setTickRate.subscribe((option, value) -> TickRate.setTickClient());
        randomtickingChunksVisualizer.subscribe((option, value) -> RandomtickDisplay.startStopRecording(value));

        villageMarkerLines.subscribe((option, value) -> VillageMarker.lines = value);
        villageMarkerGolem.subscribe((option, value) -> VillageMarker.golem = value);
        villageMarkerPopulation.subscribe((option, value) -> VillageMarker.population = value);
        villageMarkerVillageRadius.subscribe((option, value) -> VillageMarker.village_radius = value);
        villageMarkerDoorRadius.subscribe((option, value) -> VillageMarker.door_radius = value);

        boundingBoxOuter.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.OUTER_BOUNDING_BOX] = value);
        boundingBoxEndCity.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.END_CITY] = value);
        boundingBoxFortress.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.FORTRESS] = value);
        boundingBoxTemple.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.TEMPLE] = value);
        boundingBoxVillage.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.VILLAGE] = value);
        boundingBoxStronghold.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.STRONGHOLD] = value);
        boundingBoxMineshaft.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.MINESHAFT] = value);
        boundingBoxMonument.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.MONUMENT] = value);
        boundingBoxMansion.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.MANTION] = value);
        boundingBoxSlimeChunk.subscribe((option, value) -> ShowBoundingBoxes.show[ShowBoundingBoxes.SLIME_CHUNKS] = value);
    }

    public static void save() {
        String file = "config/carpetclient.cfg";
        JsonObject obj = new JsonObject();
        Gson gson = new Gson();

        obj.addProperty("setTickRate", setTickRate.getValue());
        obj.addProperty("bucketGhostBlockFix", bucketGhostBlockFix.getValue());
        obj.addProperty("elytraFix", elytraFix.getValue());
        obj.addProperty("clipThroughPistons", clipThroughPistons.getValue());
        obj.addProperty("isScoreboardHidden", isScoreboardHidden.getValue());
        obj.addProperty("pistonVisualizer", pistonVisualizer.getValue());
        // This is not saved. Loading true does not send the necessary packet to server.
        // obj.addProperty("randomtickingChunksVisualizer", randomtickingChunksVisualizer.getValue());
        obj.addProperty("creativeModeNoClip", creativeModeNoClip.getValue());
        obj.addProperty("fastCrafting", fastCrafting.getValue());
        obj.addProperty("carefulBreak", fastCrafting.getValue());

        obj.addProperty("lines", villageMarkerLines.getValue());
        obj.addProperty("golem", villageMarkerGolem.getValue());
        obj.addProperty("population", villageMarkerPopulation.getValue());
        obj.addProperty("village_radius", villageMarkerVillageRadius.getValue());
        obj.addProperty("door_radius", villageMarkerDoorRadius.getValue());

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
                setTickRate.setValue(jsonObject.get("setTickRate").getAsBoolean());
                bucketGhostBlockFix.setValue(jsonObject.get("bucketGhostBlockFix").getAsBoolean());
                elytraFix.setValue(jsonObject.get("elytraFix").getAsBoolean());
                clipThroughPistons.setValue(jsonObject.get("clipThroughPistons").getAsBoolean());
                isScoreboardHidden.setValue(jsonObject.get("isScoreboardHidden").getAsBoolean());
                pistonVisualizer.setValue(jsonObject.get("pistonVisualizer").getAsBoolean());
                // randomtickingChunksVisualizer.setValue(jsonObject.get("randomtickingChunksVisualizer").getAsBoolean());
                creativeModeNoClip.setValue(jsonObject.get("creativeModeNoClip").getAsBoolean());
                fastCrafting.setValue(jsonObject.get("fastCrafting").getAsBoolean());
                carefulBreak.setValue(jsonObject.get("carefulBreak").getAsBoolean());

                villageMarkerLines.setValue(jsonObject.get("lines").getAsBoolean());
                villageMarkerGolem.setValue(jsonObject.get("golem").getAsBoolean());
                villageMarkerPopulation.setValue(jsonObject.get("population").getAsBoolean());
                villageMarkerVillageRadius.setValue(jsonObject.get("village_radius").getAsInt());
                villageMarkerDoorRadius.setValue(jsonObject.get("door_radius").getAsInt());

                boolean[] boundingBoxes = gson.fromJson(jsonObject.get("boundingBoxes"), boolean[].class);
                boundingBoxOuter.setValue(boundingBoxes[ShowBoundingBoxes.OUTER_BOUNDING_BOX]);
                boundingBoxEndCity.setValue(boundingBoxes[ShowBoundingBoxes.END_CITY]);
                boundingBoxFortress.setValue(boundingBoxes[ShowBoundingBoxes.FORTRESS]);
                boundingBoxTemple.setValue(boundingBoxes[ShowBoundingBoxes.TEMPLE]);
                boundingBoxVillage.setValue(boundingBoxes[ShowBoundingBoxes.VILLAGE]);
                boundingBoxStronghold.setValue(boundingBoxes[ShowBoundingBoxes.STRONGHOLD]);
                boundingBoxMineshaft.setValue(boundingBoxes[ShowBoundingBoxes.MINESHAFT]);
                boundingBoxMonument.setValue(boundingBoxes[ShowBoundingBoxes.MONUMENT]);
                boundingBoxMansion.setValue(boundingBoxes[ShowBoundingBoxes.MANTION]);
                boundingBoxSlimeChunk.setValue(boundingBoxes[ShowBoundingBoxes.SLIME_CHUNKS]);

                GuiChunkGrid.style = gson.fromJson(jsonObject.get("chunkGridStyle"), ChunkGridStyle.class);
            }
        } catch (Exception e) {
        }
    }

    public static void resetToDefaults() {
        betterMiner = false;
    }
}

package carpetclient.gui;

import carpetclient.Config;
import carpetclient.coders.EDDxample.ShowBoundingBoxes;
import carpetclient.coders.EDDxample.VillageMarker;
import carpetclient.coders.Pokechu22.ScrollGUI;
import carpetclient.rules.CarpetRules;
import carpetclient.rules.TickRate;

import java.util.ArrayList;

/*
Main class to operate GUI options in the carpet display.
 */
public class ClientGUI {
    public static ScrollGUI.GuiGameRuleList list;
    private static ScrollGUI.GuiGameRuleList ruleList;
    private ScrollGUI scrollGUI;
    private static int displayLayer;

    public static final int ROOT = 0;
    public static final int CLIENT_OPTIONS = 1;
    public static final int CARPET_SERVER_RULE_OPTIONS = 2;
    public static final int VILLAGE_MARKER_OPTIONS = 3;
    public static final int BOUNDINGBOX_OPTIONS = 4;

    public ClientGUI(ScrollGUI scrollGUI) {
        this.scrollGUI = scrollGUI;
    }

    /**
     * Static update method to update the different display options.
     */
    public static void display() {
        if (displayLayer == ROOT) {
            displayRoot();
        } else if (displayLayer == CLIENT_OPTIONS) {
            displayClientOptions();
        } else if (displayLayer == CARPET_SERVER_RULE_OPTIONS) {
            displayRuleGUI();
        } else if (displayLayer == VILLAGE_MARKER_OPTIONS) {
            displayVillageOptions();
        } else if (displayLayer == BOUNDINGBOX_OPTIONS) {
            displayBoundingBoxOptions();
        }
    }

    /**
     * Interaction event when clicking or updating a options.
     *
     * @param buttonID Button id or data.
     */
    public void buttonGUIAction(int buttonID) {
        if (displayLayer == ROOT) {
            displayLayer = buttonID;
            display();
        } else if (displayLayer == CLIENT_OPTIONS) {
            setOption(buttonID);
        } else if (displayLayer == CARPET_SERVER_RULE_OPTIONS) {
            if (buttonID == 0) {
                CarpetRules.requestUpdate();
            }
        } else if (displayLayer == VILLAGE_MARKER_OPTIONS) {
            VillageMarker.guiVillageOptions(buttonID);
        } else if (displayLayer == BOUNDINGBOX_OPTIONS) {
            ShowBoundingBoxes.guiBoudingBoxOptions(buttonID);
        }
    }

    /**
     * The display level that is being displayed.
     *
     * @return Returns the display level.
     */
    public boolean isRootElseDropTo() {
        if (displayLayer == ROOT) {
            return true;
        }
        displayLayer = ROOT;
        display();
        return false;
    }

    /**
     * Displays the root options.
     */
    private static void displayRoot() {
        list.clear();
        list.addNewButton("Carpet Client Options", CLIENT_OPTIONS);
        list.addNewButton("Carpet Server Options", CARPET_SERVER_RULE_OPTIONS);
        list.addNewButton("Village Markers Options", VILLAGE_MARKER_OPTIONS);
        list.addNewButton("Bounding Box Options", BOUNDINGBOX_OPTIONS);
    }

    /**
     * Displays the carpet server options recieved from the server.
     */
    private static void displayRuleGUI() {
        list.clear();

        ArrayList<CarpetRules.CarpetSettingEntry> rules = CarpetRules.getAllRules();
        list.addNewButton("Force Update", 0);

        for (CarpetRules.CarpetSettingEntry r : rules) {
            if (r.isNumber()) {
                list.addNewText(r.getRule(), r.getCurrentOption(), r.isDefault(), r.getRuleTip(), r.useInteger());
            } else {
                list.addNewRuleButton(r.getRule(), r.getCurrentOption(), r.isDefault(), r.getRuleTip());
            }
        }
    }

    /**
     * Displays the villager marker options.
     */
    private static void displayVillageOptions() {
        list.clear();

        list.addNewRuleButton("Village Doors", String.valueOf(VillageMarker.lines), false, "Displays a line from the village center to the door.", 0);
        list.addNewRuleButton("Golem Cage", String.valueOf(VillageMarker.golem), false, "Displays the box where the golems will spawn.", 1);
        list.addNewRuleButton("Population Cage", String.valueOf(VillageMarker.population), false, "Displays a box where the villagers can be found.", 2);
        list.addNewRuleButton("Village Sphere Type", VillageMarker.modes[VillageMarker.village_radius], false, "The type of marker used to display the village radius.", 3);
        list.addNewRuleButton("Door Sphere Type", VillageMarker.modes[VillageMarker.door_radius], false, "The type of marker used to display the door radius. The radius that doors will be added to this village.", 4);
    }

    /**
     * Displays the bounding box options.
     */
    private static void displayBoundingBoxOptions() {
        list.clear();

        list.addNewRuleButton("Enclosing bounding box", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.OUTER_BOUNDING_BOX]), false, "The main bounding box that wraps all inner bounding boxes.", ShowBoundingBoxes.OUTER_BOUNDING_BOX);
        list.addNewRuleButton("End City", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.END_CITY]), false, "Displays bounding box of this type.", ShowBoundingBoxes.END_CITY);
        list.addNewRuleButton("Fortress", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.FORTRESS]), false, "Displays bounding box of this type.", ShowBoundingBoxes.FORTRESS);
        list.addNewRuleButton("Temple", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.TEMPLE]), false, "Displays bounding box of this type.", ShowBoundingBoxes.TEMPLE);
        list.addNewRuleButton("Village", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.VILLAGE]), false, "Displays bounding box of this type.", ShowBoundingBoxes.VILLAGE);
        list.addNewRuleButton("Stronghold", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.STRONGHOLD]), false, "Displays bounding box of this type.", ShowBoundingBoxes.STRONGHOLD);
        list.addNewRuleButton("Mineshaft", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.MINESHAFT]), false, "Displays bounding box of this type.", ShowBoundingBoxes.MINESHAFT);
        list.addNewRuleButton("Monument", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.MONUMENT]), false, "Displays bounding box of this type.", ShowBoundingBoxes.MONUMENT);
        list.addNewRuleButton("Mansion", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.MANTION]), false, "Displays bounding box of this type.", ShowBoundingBoxes.MANTION);
        list.addNewRuleButton("Slime Chunks", String.valueOf(ShowBoundingBoxes.show[ShowBoundingBoxes.SLIME_CHUNKS]), false, "Displays the slime chunks where slime can spawn.", ShowBoundingBoxes.SLIME_CHUNKS);
    }

    /**
     * Displays the carpet client options.
     */
    private static void displayClientOptions() {
        list.clear();

        list.addNewRuleButton("Tick Rate", String.valueOf(Config.setTickRate), false, "Toggles if the client should slow the game down (Forge integrated clients might want to turn this off as the tick rate slowdown is not compatible with forge yet).", 0);
        list.addNewRuleButton("Liquid ghost block fix", String.valueOf(Config.bucketGhostBlockFix), false, "Toggles the ghost block liquid option.", 1);
        list.addNewRuleButton("Elytra fix", String.valueOf(Config.elytraFix), false, "Earthcomputers hacky elytra fix.", 2);
        list.addNewRuleButton("Clip through pistons fix", String.valueOf(Config.clipThroughPistons), false, "Parcially fixes clipping through pistons, clipping can still happen but this helps.", 3);
    }

    /**
     * Set carpet client options
     *
     * @param option The id of the option to be toggled.
     */
    public static void setOption(int option) {
        if (option == 0) {
            Config.setTickRate = !Config.setTickRate;
            TickRate.setTickClient();
        } else if (option == 1) {
            Config.bucketGhostBlockFix = !Config.bucketGhostBlockFix;
        } else if (option == 2) {
            Config.elytraFix = !Config.elytraFix;
        } else if (option == 3) {
            Config.clipThroughPistons = !Config.clipThroughPistons;
        }

        display();
    }

    /**
     * Returns the GUI list that is generated for rendering.
     *
     * @return The list of GUI buttons generated based on the level of display.
     */
    public ScrollGUI.GuiGameRuleList getList() {
        return list;
    }
}

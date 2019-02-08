package carpetclient;

import carpetclient.gui.chunkgrid.GuiChunkGrid;
import com.mumfrey.liteloader.core.LiteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

import carpetclient.pluginchannel.CarpetPluginChannel;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;

/*
Hotkey class to implement hotkeys for the carpet client. Changeable in the hotkey menu ingame.
 */
public class Hotkeys {

    //    private static KeyBinding toggleMainMenu = new KeyBinding("Carpet Menu", Keyboard.KEY_M, "Carpet Client");
    public static KeyBinding toggleSnapAim = new KeyBinding("Snap Aim", Keyboard.KEY_F9, "Carpet Client");
    public static KeyBinding toggleSnapAimKeyLocker = new KeyBinding("Snap Aim Keylocker", Keyboard.KEY_LMENU, "Carpet Client");
    private static KeyBinding toggleBoundingBoxMarkers = new KeyBinding("Bounding Box Markers", Keyboard.KEY_F8, "Carpet Client");
    private static KeyBinding toggleVillageMarkers = new KeyBinding("Village Markers", Keyboard.KEY_F7, "Carpet Client");
    //    public static KeyBinding toggleRBP = new KeyBinding("Relaxed Block Placement", Keyboard.KEY_P, "Carpet Client");
    public static KeyBinding toggleBlockFlip = new KeyBinding("Block Rotation Flip", Keyboard.KEY_LMENU, "Carpet Client");
    public static KeyBinding toggleBlockFacing = new KeyBinding("Block Rotation Face", Keyboard.KEY_LCONTROL, "Carpet Client");
    public static KeyBinding chunkDebug = new KeyBinding("Chunk debug", Keyboard.KEY_F6, "Carpet Client");
    public static KeyBinding randomtickChunkUpdates = new KeyBinding("Randomtick display updates", Keyboard.KEY_U, "Carpet Client");
    
    public static void init() {
//        LiteLoader.getInput().registerKeyBinding(toggleMainMenu);
        LiteLoader.getInput().registerKeyBinding(toggleSnapAim);
        LiteLoader.getInput().registerKeyBinding(toggleSnapAimKeyLocker);
        LiteLoader.getInput().registerKeyBinding(toggleBoundingBoxMarkers);
        LiteLoader.getInput().registerKeyBinding(toggleVillageMarkers);
//        LiteLoader.getInput().registerKeyBinding(toggleRBP);
        LiteLoader.getInput().registerKeyBinding(toggleBlockFlip);
        LiteLoader.getInput().registerKeyBinding(toggleBlockFacing);
        LiteLoader.getInput().registerKeyBinding(chunkDebug);
        LiteLoader.getInput().registerKeyBinding(randomtickChunkUpdates);
    }

    public static void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
//        if (toggleRBP.isPressed()) {
//            Config.relaxedBlockPlacement = !Config.relaxedBlockPlacement;
//            minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Relaxed block placement: " + (Config.relaxedBlockPlacement ? "ON" : "OFF")));
//        } else 
        if (toggleSnapAim.isPressed()) {
            Config.snapAim = !Config.snapAim;
            Util.printToChat("SnapAim: " + (Config.snapAim ? "ON" : "OFF") );
        } else if (toggleBoundingBoxMarkers.isPressed()) {
            Config.boundingBoxMarkers = !Config.boundingBoxMarkers;
            if (Config.boundingBoxMarkers) {
                PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
                sender.writeInt(CarpetPluginChannel.BOUNDINGBOX_MARKERS);

                CarpetPluginChannel.packatSender(sender);
            }
        } else if (toggleVillageMarkers.isPressed()) {
            Config.villageMarkers = !Config.villageMarkers;
            PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
            sender.writeInt(CarpetPluginChannel.VILLAGE_MARKERS);
            sender.writeBoolean(Config.villageMarkers);
            
            CarpetPluginChannel.packatSender(sender);
        } else if (toggleVillageMarkers.isPressed()) {
            Config.villageMarkers = !Config.villageMarkers;
            PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
            sender.writeInt(CarpetPluginChannel.VILLAGE_MARKERS);
            sender.writeBoolean(Config.villageMarkers);

            CarpetPluginChannel.packatSender(sender);
        } else if (chunkDebug.isPressed()) {
            minecraft.displayGuiScreen(GuiChunkGrid.instance);
        } else if (randomtickChunkUpdates.isPressed()) {
            Config.randomtickChunkUpdates = !Config.randomtickChunkUpdates;
        }
    }
    
    public static boolean isKeyDown(int code){
        try{
            return Keyboard.isKeyDown(code);
        }catch(Exception e){
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Something went wrong with the hotkey. Reset it in the menu to get it working again."));
        }
        
        return false;
    }
}

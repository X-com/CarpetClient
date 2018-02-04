package carpetclient;

import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

/*
Hotkey class to implement hotkeys for the carpet client. Changeable in the hotkey menu ingame.
 */
public class Hotkeys {
//    private static KeyBinding toggleMainMenu = new KeyBinding("Carpet Menu", Keyboard.KEY_M, "Carpet Client");
    private static KeyBinding toggleSnapAim = new KeyBinding("Snap Aim", Keyboard.KEY_F9, "Carpet Client");
//    private static KeyBinding toggleMarkers = new KeyBinding("Markers", Keyboard.KEY_B, "Carpet Client");
    private static KeyBinding toggleRBP = new KeyBinding("Relaxed Block Placement", Keyboard.KEY_P, "Carpet Client");

    public static void init(){
//        LiteLoader.getInput().registerKeyBinding(toggleMainMenu);
        LiteLoader.getInput().registerKeyBinding(toggleSnapAim);
//        LiteLoader.getInput().registerKeyBinding(toggleMarkers);
        LiteLoader.getInput().registerKeyBinding(toggleRBP);
    }

    public static void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (!minecraft.isIntegratedServerRunning()) {
            return;
        }

        if (toggleRBP.isPressed()) {
            minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("Relaxed block placement: " + (Config.relaxedBlockPlacement ? "ON" : "OFF")));
            Config.relaxedBlockPlacement = !Config.relaxedBlockPlacement;
        }else if (toggleSnapAim.isPressed()) {
            minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("SnapAim: " + (Config.snapAim ? "ON" : "OFF")));
            Config.snapAim = !Config.snapAim;
        }
    }
}

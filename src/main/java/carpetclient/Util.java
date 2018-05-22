package carpetclient;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class Util {

    /**
     * Prints a text to the chat
     * @param text The text to be printed.
     */
    public static void printToChat(String text){
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(text));
    }
}

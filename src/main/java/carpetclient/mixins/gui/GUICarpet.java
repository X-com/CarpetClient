package carpetclient.mixins.gui;

import carpetclient.gui.ScrollGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/*
Mixins override to add the main access button for Carpet Options menu.
 */
@Mixin(GuiIngameMenu.class)
public class GUICarpet extends GuiScreen {

    private static final int carpetClientID = 6000;
    GuiButton carpetButton;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInitGui(CallbackInfo ci) {
        injectButtons((GuiIngameMenu) (Object) this, buttonList);
    }

    /*
     * Action handler for the button click.
     */
    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton guibutton, CallbackInfo ci) {
        handleButtonClick((GuiIngameMenu) (Object) this, guibutton);
    }

    /*
     * Inserting the button handler when Carpet Button is clicked.
     */
    private void handleButtonClick(GuiIngameMenu guiIngameMenu, GuiButton guibutton) {
        if (!guibutton.enabled) {
            return;
        }

        if (guibutton.id == carpetClientID) {
            if (Minecraft.getMinecraft().isIntegratedServerRunning()) {
                return; // not available if in singleplayer or LAN server mode
            }

            ScrollGUI.initGUI(guiIngameMenu);
        }
    }

    /*
     * Inserting the Button to access carpet menu.
     */
    private void injectButtons(GuiIngameMenu gui, List buttonList) {
        int insertAtYPos = 0;

        for (Object obj : buttonList) {
            GuiButton btn = (GuiButton) obj;

            if (btn.id == 5) { // Button "Achievements"
                insertAtYPos = btn.y + 24;
                break;
            }
        }

        // Move other buttons down one slot (= 24 height units)
        for (Object obj : buttonList) {
            GuiButton btn = (GuiButton) obj;

            if (btn.y >= insertAtYPos) {
                btn.y += 24;
            }
        }

        // Insert carpet button in main window of escape menu.
        carpetButton = new GuiButton(carpetClientID, gui.width / 2 - 100, insertAtYPos, 200, 20, "Carpet Client");
        carpetButton.enabled = !Minecraft.getMinecraft().isIntegratedServerRunning();
        buttonList.add(carpetButton);
    }
}

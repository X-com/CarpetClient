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

@Mixin(GuiIngameMenu.class)
public class GUICarpet extends GuiScreen {

    private static final int carpetClientID = 6000;
    
    @Inject(method="initGui", at=@At("RETURN"))
    private void onInitGui(CallbackInfo ci) {
        injectWDLButtons((GuiIngameMenu) (Object) this, buttonList);
    }
    @Inject(method="actionPerformed", at=@At("HEAD"))
    private void onActionPerformed(GuiButton guibutton, CallbackInfo ci) {
        handleWDLButtonClick((GuiIngameMenu) (Object) this, guibutton);
    }

    private void handleWDLButtonClick(GuiIngameMenu guiIngameMenu, GuiButton guibutton) {
        if (!guibutton.enabled) {
            return;
        }

        if (guibutton.id == carpetClientID) {
//            if (Minecraft.getMinecraft().isIntegratedServerRunning()) {
//                return; // not available if in singleplayer or LAN server mode
//            }

            ScrollGUI.initGUI(guiIngameMenu);
        }
    }

    private void injectWDLButtons(GuiIngameMenu gui, List buttonList) {
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

        // Insert wdl buttons.
        GuiButton carpetButton = new GuiButton(carpetClientID, gui.width / 2 - 100,
                insertAtYPos, 200, 20, "Carpet Client");
        
        buttonList.add(carpetButton);
    }
}

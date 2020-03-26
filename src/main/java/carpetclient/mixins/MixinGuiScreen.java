package carpetclient.mixins;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.net.URI;

/**
 * Class to help open screenshots
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Shadow void openWebLink(URI url) { }

    /*
    Method made to help open screenshot folders instead of the image itself when clicking on it in the text area.
     */
    @Inject(method = "handleComponentClick", at = @At("HEAD"), cancellable = true)
    public void onItemRightClick(ITextComponent component, CallbackInfoReturnable<Boolean> cir) {
        if(component != null && GuiScreen.isShiftKeyDown())
        {
            ClickEvent clickevent = component.getStyle().getClickEvent();
            if(clickevent != null && clickevent.getAction() == ClickEvent.Action.OPEN_FILE){
                this.openWebLink((new File(clickevent.getValue())).getParentFile().toURI());
                cir.setReturnValue(true);
            }
        }
    }
}

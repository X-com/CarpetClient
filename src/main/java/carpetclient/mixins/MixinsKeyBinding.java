package carpetclient.mixins;


import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
A Mixins class to implement key lock when snap aim is turned on.
 */
@Mixin(KeyBinding.class)
public abstract class MixinsKeyBinding {

//    @Shadow
//    private static @Final
//    IntHashMap<KeyBinding> HASH;
//    @Shadow
//    private boolean pressed;

//    /*
//     * Override method to implement key lock when Snap aim is turned on.
//     */
//    @Overwrite
//    public static void setKeyBindState(int keyCode, boolean pressed)
//    {
//        if (keyCode != 0)
//        {
//            KeyBinding keybinding = HASH.lookup(keyCode);
//
//            if (keybinding != null)
//            {
//                keybinding.getPresesd(pressed);
//            }
//        }
//    }
    
    /*
    Inject to create a return out of the funtion that detects key release. This will create the effect of keys being pressed but not get released.
     */
    @Inject(method = "setKeyBindState", at = @At("HEAD"), cancellable = true)
    private static void setKeyBindStateInject(int keyCode, boolean pressed, CallbackInfo ci) {
        //if(GuiScreen.isAltKeyDown() && !pressed && Config.snapAim){
        // Updated keylocker to make it keybind to other keys.
        if (keyCode != Hotkeys.toggleSnapAimKeyLocker.getKeyCode() && Hotkeys.toggleSnapAimKeyLocker.isKeyDown() && !pressed && Config.snapAim) {
            ci.cancel();
        }
    }
}

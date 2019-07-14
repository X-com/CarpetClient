package carpetclient.mixins;


import carpetclient.Config;
import carpetclient.Hotkeys;
import com.google.common.collect.Maps;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
A Mixins class to implement key lock when snap aim is turned on.

Multi vanilla keybinding stolen from nessie.
 */
@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {

    private static final Map<Integer, List<MixinKeyBinding>> listOfBindings = Maps.newHashMap();
    @Shadow
    @Final
    private static Map<String, MixinKeyBinding> KEYBIND_ARRAY;
    @Shadow
    private int pressTime;
    @Shadow
    private boolean pressed;
    @Shadow
    private int keyCode;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initKeyBinding(CallbackInfo callbackInfo) {
        List<MixinKeyBinding> bindingsOnKey = listOfBindings.computeIfAbsent(this.keyCode, k -> new ArrayList<>());
        bindingsOnKey.add(this);
    }

    /*
    Inject to create a return out of the funtion that detects key release. This will create the effect of keys being pressed but not get released.
     */
    @Inject(method = "setKeyBindState", at = @At("HEAD"), cancellable = true)
    private static void setKeyBindStateInject(int keyCode, boolean pressed, CallbackInfo ci) {
        ci.cancel();
        if (keyCode != Hotkeys.toggleSnapAimKeyLocker.getKeyCode() && Hotkeys.toggleSnapAimKeyLocker.isKeyDown() && !pressed && Config.snapAim) {
            return;
        }

        if (keyCode != 0) {
            List<MixinKeyBinding> bindingsOnKey = listOfBindings.get(keyCode);
            if (bindingsOnKey != null) {
                for (MixinKeyBinding keyBinding : bindingsOnKey) {
                    keyBinding.pressed = pressed;
                }
            }
        }
    }

    @Inject(method = "resetKeyBindingArrayAndHash", at = @At("HEAD"), cancellable = true)
    private static void resetKeyBindingArrayAndHash(CallbackInfo ci) {
        ci.cancel();

        listOfBindings.clear();

        for (MixinKeyBinding keyBind : KEYBIND_ARRAY.values()) {
            List<MixinKeyBinding> bindingsOnKey = listOfBindings.computeIfAbsent(keyBind.keyCode, k -> new ArrayList<>());
            bindingsOnKey.add(keyBind);
        }
    }

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void onTick(int keyCode, CallbackInfo ci) {
        ci.cancel();

        if (keyCode != 0) {
            List<MixinKeyBinding> bindingsOnKey = listOfBindings.get(keyCode);

            if (bindingsOnKey != null) {
                for (MixinKeyBinding keyBinding : bindingsOnKey) {
                    keyBinding.pressTime++;
                }
            }
        }
    }
}

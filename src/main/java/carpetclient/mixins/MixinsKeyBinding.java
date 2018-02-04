package carpetclient.mixins;


import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/*
A Mixins class to implement key lock when snap aim is turned on.
 */
@Mixin(KeyBinding.class)
public abstract class MixinsKeyBinding implements Comparable<KeyBinding>{

    @Shadow private static @Final IntHashMap<KeyBinding> HASH;
    @Shadow private boolean pressed;

    /*
     * Override method to implement key lock when Snap aim is turned on.
     */
    @Overwrite
    public static void setKeyBindState(int keyCode, boolean pressed)
    {
        if (keyCode != 0)
        {
            KeyBinding keybinding = HASH.lookup(keyCode);

            if (keybinding != null)
            {
                keybinding.pressed = pressed;
            }
        }
    }
}

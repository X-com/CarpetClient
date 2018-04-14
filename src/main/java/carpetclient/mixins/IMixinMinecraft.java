package carpetclient.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * To access private fields in Minecraft.java
 */
@Mixin(Minecraft.class)
public interface IMixinMinecraft {
    @Accessor
    Timer getTimer();
}

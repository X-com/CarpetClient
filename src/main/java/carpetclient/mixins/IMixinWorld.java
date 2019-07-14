package carpetclient.mixins;

import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface IMixinWorld {

    @Accessor
    WorldBorder getWorldBorder();
}

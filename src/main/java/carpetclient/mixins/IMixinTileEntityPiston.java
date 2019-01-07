package carpetclient.mixins;

import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntityPiston.class)
public interface IMixinTileEntityPiston {

    @Accessor
    float getProgress();
}

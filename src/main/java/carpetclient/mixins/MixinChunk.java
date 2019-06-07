package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chunk.class)
public class MixinChunk
{
    @Redirect(method = "setBlockState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk$EnumCreateEntityType;)Lnet/minecraft/tileentity/TileEntity;",
            ordinal = 1))
    private TileEntity getTileEntityTE(Chunk chunk, BlockPos pos, Chunk.EnumCreateEntityType creationMode)
    {
        if (!Config.movableTileEntities)
            return chunk.getTileEntity(pos, creationMode);

        return chunk.getWorld().getTileEntity(pos);
    }
}

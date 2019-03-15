package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.rules.TickRate;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(World.class)
public class MixinWorld {

    /**
     * Ignoring entity's when placing  block clientside.
     * @param world
     * @param bb
     * @param entityIn
     * @param blockIn
     * @param pos
     * @param skipCollisionCheck
     * @param sidePlacedOn
     * @param placer
     * @return
     */
    @Redirect(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z"))
    private boolean ignoreEntitysWhenPlacingBlocks(World world,
                                                    AxisAlignedBB bb, @Nullable Entity entityIn, // sub vars
                                                    Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer// main vars
    ){
        return Config.ignoreEntityWhenPlacing || world.checkNoEntityCollision(bb, entityIn);
    }
}
